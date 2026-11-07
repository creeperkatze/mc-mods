package dev.creeperkatze.illuminate.scene.layer;

import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Layer;
import dev.creeperkatze.illuminate.scene.Moment;
import dev.creeperkatze.illuminate.scene.Rgb;
import dev.creeperkatze.illuminate.scene.Shapes;

// A purple swirl that builds up while you stand in a Nether portal
public final class PortalLayer implements Layer {
	private static final Rgb DEEP = new Rgb(0.2F, 0.0F, 0.5F);
	private static final Rgb BRIGHT = new Rgb(0.5F, 0.1F, 0.9F);

	private float shown;
	private float spin;
	private float speed;

	@Override
	public void paint(Canvas canvas, GameState state, Moment moment) {
		float intensity = state.portal();
		// The game's overlay takes four seconds to build up, the lights reach full strength within the first
		float target = moment.config().portals ? 0.95F * Shapes.smoothstep(0.0F, 0.25F, intensity) : 0.0F;
		// Comes in quickly but fades out gently, so arriving in the new dimension doesn't cut it off
		shown = Shapes.approach(shown, target, target > shown ? 20.0F : 2.5F, moment.delta());
		if (shown <= 0.01F) {
			return;
		}
		// Broad bands that speed up as the portal is about to take you and keep spinning while they fade in the new dimension
		float targetSpeed = 0.6F + 2.6F * intensity;
		speed = Shapes.approach(speed, targetSpeed, targetSpeed > speed ? 2.0F : 0.4F, moment.delta());
		spin += moment.delta() * speed;
		float alpha = shown;
		for (int i = 0; i < Canvas.SIZE; i++) {
			float dx = (Canvas.u(i) - 0.5F) * 1.78F;
			float dy = Canvas.v(i) - 0.5F;
			float angle = (float) Math.atan2(dy, dx);
			float distance = (float) Math.sqrt(dx * dx + dy * dy);
			float swirl = 0.5F + 0.5F * (float) Math.sin(angle * 2.0F + distance * 4.0F - spin);
			canvas.mix(i, DEEP.lerp(BRIGHT, swirl), alpha);
		}
	}
}
