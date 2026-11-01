package dev.creeperkatze.rearrange;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
/*? if fabric {*/
import net.fabricmc.loader.api.FabricLoader;
/*?} else {*//*
import net.neoforged.fml.loading.FMLPaths;
*//*?}*/
import net.minecraft.world.level.storage.LevelSummary;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorldOrder {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static @Nullable List<String> worlds;

	private WorldOrder() {
	}

	private static Path path() {
		/*? if fabric {*/
		Path configDir = FabricLoader.getInstance().getConfigDir();
		/*?} else {*//*
		Path configDir = FMLPaths.CONFIGDIR.get();
		*//*?}*/
		return configDir.resolve(Rearrange.MOD_ID + ".json");
	}

	// Worlds missing from the saved order are new and stay on top in vanilla order
	public static List<LevelSummary> sort(List<LevelSummary> levels) {
		List<String> order = worlds();
		if (order.isEmpty()) {
			return levels;
		}
		Map<String, Integer> positions = new HashMap<>();
		for (int i = 0; i < order.size(); i++) {
			positions.put(order.get(i), i);
		}
		List<LevelSummary> sorted = new ArrayList<>(levels);
		sorted.sort(Comparator.comparingInt(level -> positions.getOrDefault(level.getLevelId(), -1)));
		return sorted;
	}

	public static void save(List<String> order) {
		worlds = List.copyOf(order);
		try {
			Files.createDirectories(path().getParent());
			Files.writeString(path(), GSON.toJson(new Data(worlds)));
		} catch (IOException e) {
			Rearrange.LOGGER.warn("Couldn't save the world order", e);
		}
	}

	public static boolean hasOrder() {
		return !worlds().isEmpty();
	}

	public static void reset() {
		worlds = List.of();
		try {
			Files.deleteIfExists(path());
		} catch (IOException e) {
			Rearrange.LOGGER.warn("Couldn't reset the world order", e);
		}
	}

	private static List<String> worlds() {
		if (worlds == null) {
			worlds = load();
		}
		return worlds;
	}

	private static List<String> load() {
		Path path = path();
		if (!Files.exists(path)) {
			return List.of();
		}
		try {
			Data data = GSON.fromJson(Files.readString(path), Data.class);
			return data != null && data.worlds() != null ? List.copyOf(data.worlds()) : List.of();
		} catch (IOException | JsonParseException e) {
			Rearrange.LOGGER.warn("Couldn't load the world order", e);
			return List.of();
		}
	}

	private record Data(List<String> worlds) {
	}
}
