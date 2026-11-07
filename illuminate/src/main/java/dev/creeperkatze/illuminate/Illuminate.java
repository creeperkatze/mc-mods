package dev.creeperkatze.illuminate;

import dev.creeperkatze.illuminate.config.IlluminateConfig;
import dev.creeperkatze.illuminate.engine.LightingEngine;
import dev.creeperkatze.illuminate.game.GameStateCollector;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Illuminate {
	public static final String MOD_ID = "illuminate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final LightingEngine ENGINE = new LightingEngine();

	private Illuminate() {
	}

	public static void init() {
		IlluminateConfig.HANDLER.load();
		ENGINE.start();
	}

	public static void onClientTick(Minecraft client) {
		GameStateCollector.tick(client);
	}

	public static void onClientStopping() {
		ENGINE.shutdown();
	}

	public static LightingEngine engine() {
		return ENGINE;
	}

	public static IlluminateConfig config() {
		return IlluminateConfig.HANDLER.instance();
	}
}
