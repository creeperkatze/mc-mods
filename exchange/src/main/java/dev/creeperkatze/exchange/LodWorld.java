package dev.creeperkatze.exchange;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.creeperkatze.exchange.dh.DhDatabase;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class LodWorld {
	// Both mods create small empty databases on the first world load
	private static final long MIN_LOD_BYTES = 1 << 20;
	private static final long MIN_GROWTH_BYTES = 16 << 20;
	private static final String REMEMBERED_FILE = "exchange.json";

	private final Path root;
	private final long seed;
	private final RegistryAccess registries;
	private final List<Dimension> dimensions;

	public record Dimension(ResourceKey<Level> level, int minY, int height, Path dhDatabase) {
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
		return this.lodBytes(mod) >= MIN_LOD_BYTES;
	}

	private long lodBytes(LodMod mod) {
		if (mod == LodMod.VOXY) {
			return size(this.voxyPath());
		}
		long bytes = 0;
		for (Dimension dimension : this.dimensions) {
			Path database = dimension.dhDatabase();
			bytes += size(database) + size(database.resolveSibling(database.getFileName() + "-wal"));
		}
		return bytes;
	}

	// Offers a target again once its source grew noticeably since it was copied or skipped
	public boolean shouldOffer(LodMod target) {
		long bytes = this.lodBytes(target.other());
		if (bytes < MIN_LOD_BYTES) {
			return false;
		}
		Long remembered = this.remembered().get(target);
		// Voxy's database shrinks and grows a bit on its own when it compacts
		return remembered == null || bytes >= remembered + Math.max(MIN_GROWTH_BYTES, remembered / 4);
	}

	// Remembers how big each target's source is right now
	public void remember(Collection<LodMod> targets) {
		Map<LodMod, Long> remembered = this.remembered();
		for (LodMod target : targets) {
			remembered.put(target, this.lodBytes(target.other()));
		}
		JsonObject json = new JsonObject();
		remembered.forEach((target, bytes) -> json.addProperty(target.key(), bytes));
		Path file = this.root.resolve(REMEMBERED_FILE);
		try {
			Files.writeString(file, json.toString());
		} catch (IOException e) {
			Exchange.LOGGER.warn("Failed to write {}", file, e);
		}
	}

	private Map<LodMod, Long> remembered() {
		Map<LodMod, Long> remembered = new EnumMap<>(LodMod.class);
		Path file = this.root.resolve(REMEMBERED_FILE);
		if (!Files.isRegularFile(file)) {
			return remembered;
		}
		try {
			JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
				LodMod target = LodMod.byKey(entry.getKey());
				if (target != null && entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
					remembered.put(target, entry.getValue().getAsLong());
				}
			}
		} catch (Exception e) {
			Exchange.LOGGER.warn("Failed to read {}", file, e);
		}
		return remembered;
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
