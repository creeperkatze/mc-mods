package dev.creeperkatze.speeddial.config;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/** What the action bar shows after changing the flight speed. */
public enum SpeedDisplay {
	OFF,
	MULTIPLIER,
	BLOCKS_PER_SECOND,
	KILOMETERS_PER_HOUR,
	MACH,
	CHUNKS_PER_SECOND,
	/** The jolt of the change, since g-force is zero at a steady speed. */
	G_FORCE;

	public String key() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	public Component caption() {
		return Component.translatable("speed-dial.config.speed_display." + this.key());
	}
}
