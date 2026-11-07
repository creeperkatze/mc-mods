package dev.creeperkatze.illuminate.scene.layer;

import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Layer;
import dev.creeperkatze.illuminate.scene.Moment;
import dev.creeperkatze.illuminate.scene.Rgb;
import dev.creeperkatze.illuminate.scene.Shapes;
import net.minecraft.world.level.material.FogType;

public final class VitalsLayer implements Layer {
	private static final float LOW_HEALTH = 0.3F;
	private static final Rgb HEART = new Rgb(0.95F, 0.02F, 0.02F);
	private static final Rgb FLAME = new Rgb(1.0F, 0.22F, 0.0F);
	private static final Rgb FLAME_TIP = new Rgb(1.0F, 0.72F, 0.12F);
	private static final Rgb FROST = new Rgb(0.72F, 0.9F, 1.0F);
	private static final Rgb AIR = new Rgb(0.1F, 0.35F, 1.0F);
	private static final Rgb DEATH = new Rgb(0.3F, 0.0F, 0.0F);

	private float heartPhase;
	private float drownPhase;
	private float fire;
	private float death;

	@Override
	public void paint(Canvas canvas, GameState state, Moment moment) {
		float delta = moment.delta();
		float time = (float) moment.time();
		var config = moment.config();

		fire = Shapes.approach(fire, config.hazards && state.burning() ? 1.0F : 0.0F, 6.0F, delta);
		death = Shapes.approach(death, state.dead() ? 1.0F : 0.0F, state.dead() ? 1.5F : 4.0F, delta);

		float heartbeat = 0.0F;
		if (config.lowHealth && state.health() < LOW_HEALTH && !state.dead()) {
			float severity = 1.0F - state.health() / LOW_HEALTH;
			heartPhase = (heartPhase + delta * (60.0F + 80.0F * severity) / 60.0F) % 1.0F;
			heartbeat = Math.min(beat(heartPhase, 0.0F) + beat(heartPhase, 1.0F) + 0.6F * beat(heartPhase, 0.3F), 1.0F)
					* (0.3F + 0.45F * severity);
		}

		float drowning = 0.0F;
		if (config.hazards && state.fluid() == FogType.WATER && state.air() < 0.5F) {
			float urgency = 1.0F - state.air() / 0.5F;
			drownPhase = (drownPhase + delta * (0.8F + 2.0F * urgency)) % 1.0F;
			drowning = urgency * 0.5F * (0.5F + 0.5F * (float) Math.sin(drownPhase * Math.PI * 2.0));
		}

		float frozen = config.hazards ? state.frozen() : 0.0F;
		float sleep = config.sleep ? state.sleep() : 0.0F;

		for (int i = 0; i < Canvas.SIZE; i++) {
			float u = Canvas.u(i);
			float v = Canvas.v(i);
			if (fire > 0.01F) {
				float heat = Shapes.smoothstep(0.2F, 1.0F, v);
				float flicker = Shapes.noise(u * 5.0F, time * 6.0F - v * 3.0F);
				canvas.mix(i, FLAME.lerp(FLAME_TIP, flicker * heat), fire * heat * (0.5F + 0.45F * flicker));
			}
			if (frozen > 0.01F) {
				float edge = Math.max(Shapes.sides(u), Math.abs(v - 0.5F) * 2.0F);
				float reach = Shapes.smoothstep(1.0F - frozen, 1.3F - frozen, edge);
				canvas.mix(i, FROST, reach * frozen * 0.8F);
			}
			if (drowning > 0.01F) {
				canvas.mix(i, state.air() <= 0.0F ? HEART : AIR, drowning);
			}
			if (heartbeat > 0.01F) {
				canvas.mix(i, HEART, heartbeat * (0.5F + 0.5F * Math.max(Shapes.sides(u), v)));
			}
			if (sleep > 0.0F) {
				canvas.multiply(i, 1.0F - 0.92F * sleep);
			}
			if (death > 0.01F) {
				Rgb faded = canvas.get(i).scale(0.25F).lerp(DEATH, 0.6F);
				canvas.mix(i, faded.scale(0.8F + 0.2F * (float) Math.sin(time * 1.5F)), death);
			}
		}
	}

	private static float beat(float phase, float center) {
		float pulse = Math.max(0.0F, 1.0F - Math.abs(phase - center) / 0.18F);
		return pulse * pulse;
	}
}
