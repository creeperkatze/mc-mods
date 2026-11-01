package dev.creeperkatze.speeddial.config;

import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum StepMode {
	PROGRESSIVE,
	LINEAR;

	public Component caption() {
		return Component.translatable("speed-dial.config.step_mode." + this.name().toLowerCase(Locale.ROOT));
	}
}
