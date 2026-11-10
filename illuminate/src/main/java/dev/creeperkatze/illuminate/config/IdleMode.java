package dev.creeperkatze.illuminate.config;

import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum IdleMode {
	PANORAMA,
	COLOR,
	OFF;

	public Component caption() {
		return Component.translatable("illuminate.config.idle_mode." + name().toLowerCase(Locale.ROOT));
	}
}
