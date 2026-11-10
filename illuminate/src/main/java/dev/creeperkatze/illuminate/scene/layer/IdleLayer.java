package dev.creeperkatze.illuminate.scene.layer;

import dev.creeperkatze.illuminate.config.IdleMode;
import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.game.MenuPanorama;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Layer;
import dev.creeperkatze.illuminate.scene.Moment;
import dev.creeperkatze.illuminate.scene.Rgb;

public final class IdleLayer implements Layer {
	@Override
	public void paint(Canvas canvas, GameState state, Moment moment) {
		var config = moment.config();
		if (config.idleMode == IdleMode.COLOR) {
			canvas.fill(Rgb.of(config.idleColor.getRGB() & 0xFFFFFF));
			return;
		}
		for (int i = 0; i < Canvas.SIZE; i++) {
			Rgb panorama = MenuPanorama.sample(Canvas.u(i), Canvas.v(i));
			canvas.set(i, panorama != null ? panorama : Rgb.WHITE);
		}
	}
}
