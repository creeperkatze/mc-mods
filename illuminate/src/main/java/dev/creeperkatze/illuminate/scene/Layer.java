package dev.creeperkatze.illuminate.scene;

import dev.creeperkatze.illuminate.game.GameEvent;
import dev.creeperkatze.illuminate.game.GameState;

public interface Layer {
	void paint(Canvas canvas, GameState state, Moment moment);

	default void onEvent(GameEvent event, Moment moment) {
	}
}
