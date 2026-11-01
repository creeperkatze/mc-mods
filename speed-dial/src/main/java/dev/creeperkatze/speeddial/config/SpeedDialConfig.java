package dev.creeperkatze.speeddial.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.creeperkatze.speeddial.SpeedDial;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpeedDialConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public float maxSpeed = 100.0F;
	public StepMode stepMode = StepMode.PROGRESSIVE;
	public float progressiveMultiplier = 1.15F;
	public float linearStep = 1.0F;
	public SpeedDisplay speedDisplay = SpeedDisplay.MULTIPLIER;
	public boolean matchVerticalSpeed = false;
	public boolean rememberSpeed = false;
	public boolean pickBlockResetsSpeed = false;
	public float rememberedSpeed = 1.0F;

	public static SpeedDialConfig load() {
		Path path = path();
		SpeedDialConfig config = null;
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				config = GSON.fromJson(reader, SpeedDialConfig.class);
			} catch (IOException | JsonParseException e) {
				SpeedDial.LOGGER.warn("Failed to read {}, using defaults", path, e);
			}
		}
		if (config == null) {
			config = new SpeedDialConfig();
		}
		if (config.speedDisplay == null) {
			config.speedDisplay = SpeedDisplay.MULTIPLIER;
		}
		if (config.stepMode == null) {
			config.stepMode = StepMode.PROGRESSIVE;
		}
		config.save();
		return config;
	}

	public void save() {
		Path path = path();
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			SpeedDial.LOGGER.warn("Failed to write {}", path, e);
		}
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(SpeedDial.MOD_ID + ".json");
	}
}
