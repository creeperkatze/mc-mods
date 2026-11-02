package dev.creeperkatze.accelerate.config;

import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum StepMode {
	PROGRESSIVE,
	LINEAR;

	public Component caption() {
		return Component.translatable("accelerate.config.step_mode." + this.name().toLowerCase(Locale.ROOT));
	}
}
