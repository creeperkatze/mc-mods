package dev.creeperkatze.illuminate.scene.layer;

import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Layer;
import dev.creeperkatze.illuminate.scene.Moment;
import dev.creeperkatze.illuminate.scene.Rgb;

// A calm drifting gradient for the title screen and other menus outside a world
public final class IdleLayer implements Layer {
	private static final Rgb CYAN = new Rgb(0.12F, 0.71F, 0.84F);
	private static final Rgb VIOLET = new Rgb(0.42F, 0.2F, 0.85F);

	@Override
	public void paint(Canvas canvas, GameState state, Moment moment) {
		float time = (float) moment.time();
		float breathing = 0.45F + 0.1F * (float) Math.sin(time * 0.8F);
		for (int i = 0; i < Canvas.SIZE; i++) {
			float wave = 0.5F + 0.5F * (float) Math.sin(time * 0.4F + Canvas.u(i) * 3.0F + Canvas.v(i) * 1.5F);
			canvas.set(i, CYAN.lerp(VIOLET, wave).scale(breathing));
		}
	}
}
