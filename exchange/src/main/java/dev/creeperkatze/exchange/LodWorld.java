package dev.creeperkatze.exchange;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.creeperkatze.exchange.dh.DhDatabase;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class LodWorld {
	// Both mods create small empty databases on the first world load
	private static final long MIN_LOD_BYTES = 1 << 20;
	private static final String ANSWERS_FILE = "exchange.json";

	private final Path root;
	private final long seed;
	private final RegistryAccess registries;
	private final List<Dimension> dimensions;

	public record Dimension(ResourceKey<Level> level, int minY, int height, Path dhDatabase) {
		public Component name() {
			return Component.literal(this.level.identifier().toString());
		}
	}

	private LodWorld(Path root, long seed, RegistryAccess registries, List<Dimension> dimensions) {
		this.root = root;
		this.seed = seed;
		this.registries = registries;
		this.dimensions = dimensions;
	}

	public static LodWorld of(LevelStorageSource.LevelStorageAccess access, WorldStem stem) {
		RegistryAccess registries = stem.registries().compositeAccess();
		List<Dimension> dimensions = new ArrayList<>();
		registries.lookupOrThrow(Registries.LEVEL_STEM).listElements().forEach(levelStem -> {
			ResourceKey<Level> level = Registries.levelStemToLevel(levelStem.key());
			DimensionType type = levelStem.value().type().value();
			Path database = access.getDimensionPath(level).resolve("data").resolve(DhDatabase.FILE_NAME);
			dimensions.add(new Dimension(level, type.minY(), type.height(), database));
		});
		long seed = stem.worldDataAndGenSettings().genSettings().options().seed();
		Path root = access.getLevelPath(LevelResource.ROOT).toAbsolutePath().normalize();
		return new LodWorld(root, seed, registries, dimensions);
	}

	public long seed() {
		return this.seed;
	}

	public RegistryAccess registries() {
		return this.registries;
	}

	public List<Dimension> dimensions() {
		return this.dimensions;
	}

	public Path voxyPath() {
		return this.root.resolve("voxy");
	}

	public boolean hasLods(LodMod mod) {
		if (mod == LodMod.VOXY) {
			return size(this.voxyPath()) >= MIN_LOD_BYTES;
		}
		long bytes = 0;
		for (Dimension dimension : this.dimensions) {
			Path database = dimension.dhDatabase();
			bytes += size(database) + size(database.resolveSibling(database.getFileName() + "-wal"));
		}
		return bytes >= MIN_LOD_BYTES;
	}

	// Targets that were copied to or skipped
	public Set<LodMod> answered() {
		Set<LodMod> answered = EnumSet.noneOf(LodMod.class);
		Path file = this.root.resolve(ANSWERS_FILE);
		if (!Files.isRegularFile(file)) {
			return answered;
		}
		try {
			JsonElement json = JsonParser.parseString(Files.readString(file));
			for (JsonElement entry : json.getAsJsonObject().getAsJsonArray("answered")) {
				LodMod mod = LodMod.byKey(entry.getAsString());
				if (mod != null) {
					answered.add(mod);
				}
			}
		} catch (Exception e) {
			Exchange.LOGGER.warn("Failed to read {}", file, e);
		}
		return answered;
	}

	public void markAnswered(Collection<LodMod> mods) {
		Set<LodMod> answered = this.answered();
		answered.addAll(mods);
		JsonArray array = new JsonArray();
		answered.forEach(mod -> array.add(mod.key()));
		JsonObject json = new JsonObject();
		json.add("answered", array);
		Path file = this.root.resolve(ANSWERS_FILE);
		try {
			Files.writeString(file, json.toString());
		} catch (IOException e) {
			Exchange.LOGGER.warn("Failed to write {}", file, e);
		}
	}

	private static long size(Path path) {
		if (Files.isRegularFile(path)) {
			try {
				return Files.size(path);
			} catch (IOException e) {
				return 0;
			}
		}
		if (!Files.isDirectory(path)) {
			return 0;
		}
		try (Stream<Path> files = Files.walk(path)) {
			return files.filter(Files::isRegularFile).mapToLong(LodWorld::size).sum();
		} catch (IOException e) {
			return 0;
		}
	}
}
