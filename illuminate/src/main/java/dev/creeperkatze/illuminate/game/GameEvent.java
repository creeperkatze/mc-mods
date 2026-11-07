package dev.creeperkatze.illuminate.game;

import dev.creeperkatze.illuminate.scene.Rgb;

import java.util.List;

// One time moments. u is where it happened from your left (0) to your right (1), NaN when it has no direction.
public sealed interface GameEvent {
	record Damage(String type, float u) implements GameEvent {
	}

	record Explosion(float strength, float u) implements GameEvent {
	}

	record Lightning(float strength, float u) implements GameEvent {
	}

	record Firework(List<Rgb> colors, List<Rgb> fadeColors, float strength, float u, float v) implements GameEvent {
	}

	record Totem() implements GameEvent {
	}

	record LevelUp(int level) implements GameEvent {
	}

	record Arrival() implements GameEvent {
	}
}
