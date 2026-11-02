package dev.creeperkatze.accelerate;

import dev.creeperkatze.accelerate.config.AccelerateConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Accelerate {
	public static final String MOD_ID = "accelerate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private Accelerate() {
	}

	public static void init() {
		AccelerateConfig.HANDLER.load();
	}

	public static void onClientTick(Minecraft client) {
		LocalPlayer player = client.player;
		FlightSpeed.tick(player);

		int steps = AccelerateKeybinds.clicks(AccelerateKeybinds.FASTER) - AccelerateKeybinds.clicks(AccelerateKeybinds.SLOWER);
		boolean resetPressed = AccelerateKeybinds.clicks(AccelerateKeybinds.RESET) > 0;
		if (!FlightSpeed.canChange(player)) {
			return;
		}
		if (steps != 0) {
			FlightSpeed.change(player, steps);
		}
		if (resetPressed) {
			FlightSpeed.reset(player);
		}
	}

	public static AccelerateConfig config() {
		return AccelerateConfig.HANDLER.instance();
	}
}
