package dev.creeperkatze.speeddial;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class SpeedDialKeybinds {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(SpeedDial.MOD_ID, "main"));

	public static final KeyMapping FASTER = key("faster", InputConstants.KEY_PAGEUP);
	public static final KeyMapping SLOWER = key("slower", InputConstants.KEY_PAGEDOWN);
	public static final KeyMapping RESET = key("reset", InputConstants.UNKNOWN.getValue());
	public static final KeyMapping MODIFIER = key("modifier", InputConstants.KEY_LCONTROL);

	private SpeedDialKeybinds() {
	}

	public static List<KeyMapping> all() {
		return List.of(FASTER, SLOWER, RESET, MODIFIER);
	}

	public static boolean isModifierDown() {
		return MODIFIER.isDown();
	}

	public static int clicks(KeyMapping key) {
		int count = 0;
		while (key.consumeClick()) {
			count++;
		}
		return count;
	}

	private static KeyMapping key(String name, int key) {
		return new KeyMapping("key." + SpeedDial.MOD_ID + "." + name, key, CATEGORY);
	}
}
