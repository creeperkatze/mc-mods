package dev.creeperkatze.illuminate.scene;

import dev.creeperkatze.illuminate.config.IlluminateConfig;

// The frame being painted. Previewing is true while Live Preview is open, so effects also play outside a world.
public record Moment(double time, float delta, IlluminateConfig config, boolean previewing) {
	// How strong flashes may get, lowered by the reduce flashing setting
	public float flashStrength() {
		return config.eventIntensity * (config.reduceFlashing ? 0.35F : 1.0F);
	}
}
