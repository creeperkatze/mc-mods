package dev.creeperkatze.accelerate;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
//? if >=1.21.9 {
import net.minecraft.resources.Identifier;
//?}

import java.util.List;

public final class AccelerateKeybinds {
	//? if >=1.21.9 {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Accelerate.MOD_ID, "main"));
	//?} else {
	/*private static final String CATEGORY = "key.category." + Accelerate.MOD_ID + ".main";
	*///?}

	public static final KeyMapping FASTER = key("faster", InputConstants.KEY_PAGEUP);
	public static final KeyMapping SLOWER = key("slower", InputConstants.KEY_PAGEDOWN);
	public static final KeyMapping RESET = key("reset", InputConstants.UNKNOWN.getValue());
	public static final KeyMapping MODIFIER = key("modifier", InputConstants.KEY_LCONTROL);

	private AccelerateKeybinds() {
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
		return new KeyMapping("key." + Accelerate.MOD_ID + "." + name, key, CATEGORY);
	}
}
