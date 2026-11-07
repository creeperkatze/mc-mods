package dev.creeperkatze.illuminate.config;

import net.minecraft.network.chat.Component;

import java.util.Locale;

// Where a device sits in the room, which decides the part of the scene it shows
public enum Placement {
	LEFT,
	CENTER,
	RIGHT,
	ABOVE,
	BELOW,
	SURROUND,
	WHOLE_SCENE,
	OFF;

	public Component caption() {
		return Component.translatable("illuminate.config.placement." + name().toLowerCase(Locale.ROOT));
	}
}
