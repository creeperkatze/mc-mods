package dev.creeperkatze.speeddial;

import com.mojang.blaze3d.platform.InputConstants;
import dev.creeperkatze.speeddial.config.SpeedDialConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpeedDial implements ClientModInitializer {
	public static final String MOD_ID = "speed-dial";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

	private static SpeedDialConfig config = new SpeedDialConfig();
	private static KeyMapping modifier;

	@Override
	public void onInitializeClient() {
		config = SpeedDialConfig.load();

		KeyMapping faster = register("faster", InputConstants.KEY_PAGEUP);
		KeyMapping slower = register("slower", InputConstants.KEY_PAGEDOWN);
		KeyMapping reset = register("reset", InputConstants.UNKNOWN.getValue());
		modifier = register("modifier", InputConstants.KEY_LCONTROL);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			FlightSpeed.tick(client.player);
			boolean active = FlightSpeed.canChange(client.player);
			while (faster.consumeClick()) {
				if (active) {
					FlightSpeed.change(client.player, 1);
				}
			}
			while (slower.consumeClick()) {
				if (active) {
					FlightSpeed.change(client.player, -1);
				}
			}
			while (reset.consumeClick()) {
				if (active) {
					FlightSpeed.reset(client.player);
				}
			}
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FlightSpeed.saveRememberedSpeed());
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> FlightSpeed.saveRememberedSpeed());
	}

	private static KeyMapping register(String name, int key) {
		return KeyMappingHelper.registerKeyMapping(new KeyMapping("key." + MOD_ID + "." + name, key, KEY_CATEGORY));
	}

	/** Held while scrolling in creative to change the flight speed instead of the hotbar slot. */
	public static boolean isModifierDown() {
		return modifier != null && modifier.isDown();
	}

	public static SpeedDialConfig config() {
		return config;
	}
}
