package dev.creeperkatze.speeddial;

import dev.creeperkatze.speeddial.config.SpeedDialConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SpeedDial {
	public static final String MOD_ID = "speed-dial";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private SpeedDial() {
	}

	public static void init() {
		SpeedDialConfig.HANDLER.load();
	}

	public static void onClientTick(Minecraft client) {
		LocalPlayer player = client.player;
		FlightSpeed.tick(player);

		int steps = SpeedDialKeybinds.clicks(SpeedDialKeybinds.FASTER) - SpeedDialKeybinds.clicks(SpeedDialKeybinds.SLOWER);
		boolean resetPressed = SpeedDialKeybinds.clicks(SpeedDialKeybinds.RESET) > 0;
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

	public static SpeedDialConfig config() {
		return SpeedDialConfig.HANDLER.instance();
	}
}
