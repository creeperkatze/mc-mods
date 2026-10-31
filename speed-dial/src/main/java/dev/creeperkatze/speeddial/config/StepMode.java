package dev.creeperkatze.speeddial.config;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/** How each scroll step changes the speed. */
public enum StepMode {
	/** Vanilla's small steps up to 4x, then a percentage faster per step. */
	PROGRESSIVE,
	/** Every step adds the same amount. */
	LINEAR;

	public Component caption() {
		return Component.translatable("speed-dial.config.step_mode." + this.name().toLowerCase(Locale.ROOT));
	}
}
