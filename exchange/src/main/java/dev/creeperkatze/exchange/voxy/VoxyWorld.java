package dev.creeperkatze.exchange.voxy;

import dev.creeperkatze.exchange.LodWorld;
import dev.creeperkatze.exchange.Exchange;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.cortex.voxy.common.StorageConfigUtil;
import me.cortex.voxy.common.config.ConfigBuildCtx;
import me.cortex.voxy.common.config.section.SectionStorage;
import me.cortex.voxy.common.config.section.SectionStorageConfig;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.common.world.WorldSection;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.biome.BiomeManager;

import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

// The Voxy storage of one dimension, opened without a running Voxy instance
public final class VoxyWorld implements AutoCloseable {
	private static final int SAVE_THREADS = 2;
	// Every queued section holds 256 KiB until it is written
	private static final int MAX_QUEUED_SAVES = 256;
	private static final ThreadLocal<Boolean> SAVER_THREAD = ThreadLocal.withInitial(() -> false);

	private final WorldEngine engine;
	private final ThreadPoolExecutor saver = new ThreadPoolExecutor(
		SAVE_THREADS, SAVE_THREADS, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), runnable -> {
			Thread thread = new Thread(() -> {
				SAVER_THREAD.set(true);
				runnable.run();
			}, "Exchange Voxy Saver");
			thread.setDaemon(true);
			return thread;
		}
	);

	private VoxyWorld(SectionStorage storage) {
		this.engine = new WorldEngine(storage);
		this.engine.setSaveCallback(this::queueSave);
	}

	public static String worldId(LodWorld.Dimension dimension, long seed) {
		return new WorldIdentifier(dimension.level(), BiomeManager.obfuscateSeed(seed), dimension.type()).getWorldId();
	}

	public static VoxyWorld open(Path basePath, String worldId) {
		StorageFile config = StorageConfigUtil.getCreateStorageConfig(
			StorageFile.class, file -> file.version == 1 && file.sectionStorageConfig != null, StorageFile::create, basePath
		);
		ConfigBuildCtx context = new ConfigBuildCtx();
		context.setProperty(ConfigBuildCtx.BASE_SAVE_PATH, basePath.toString());
		context.setProperty(ConfigBuildCtx.WORLD_IDENTIFIER, worldId);
		context.setProperty(ConfigBuildCtx.PLAYER_UUID, Minecraft.getInstance().getUser().getProfileId().toString().replace(':', '-'));
		context.pushPath(ConfigBuildCtx.DEFAULT_STORAGE_PATH);
		return new VoxyWorld(config.sectionStorageConfig.build(context));
	}

	public WorldEngine engine() {
		return this.engine;
	}

	public LongSet sections(int level) {
		LongSet sections = new LongOpenHashSet();
		this.engine.storage.iteratePositions(level, sections::add);
		return sections;
	}

	// Does what Voxy's own saving service does, on threads of its own
	private boolean queueSave(WorldEngine engine, WorldSection section, boolean nonBlocking, boolean alreadyAcquired) {
		if (!section.exchangeIsInSaveQueue(true)) {
			return false;
		}
		if (!alreadyAcquired) {
			section.acquire();
		}
		// Savers queue sections again when they change while being written, so only they may never wait
		while (!nonBlocking && !SAVER_THREAD.get() && this.saver.getQueue().size() > MAX_QUEUED_SAVES) {
			LockSupport.parkNanos(100_000);
		}
		try {
			this.saver.execute(() -> this.save(section));
		} catch (RejectedExecutionException e) {
			this.save(section);
		}
		return true;
	}

	private void save(WorldSection section) {
		try {
			if (section.exchangeIsInSaveQueue(false)) {
				section.setNotDirty();
				this.engine.storage.saveSection(section);
			} else {
				section.setNotDirty();
			}
		} catch (Exception e) {
			Exchange.LOGGER.error("Failed to save a Voxy section", e);
		}
		section.release();
	}

	@Override
	public void close() throws InterruptedException {
		this.saver.shutdown();
		this.saver.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		try {
			this.engine.free();
		} catch (IllegalStateException e) {
			// Free refuses while sections are still loaded, but the storage has to close either way
			Exchange.LOGGER.warn("Voxy sections were still loaded after copying", e);
			this.engine.storage.flush();
			this.engine.storage.close();
		}
	}

	// Same fields as the config.json Voxy writes next to its storage
	public static final class StorageFile {
		public int version = 1;
		public boolean disabled = false;
		public SectionStorageConfig sectionStorageConfig;

		private static StorageFile create() {
			StorageFile file = new StorageFile();
			file.sectionStorageConfig = StorageConfigUtil.createDefaultSerializer();
			return file;
		}
	}
}
