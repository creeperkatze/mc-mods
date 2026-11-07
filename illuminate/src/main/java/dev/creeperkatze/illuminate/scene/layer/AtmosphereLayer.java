package dev.creeperkatze.illuminate.scene.layer;

import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Layer;
import dev.creeperkatze.illuminate.scene.Moment;
import dev.creeperkatze.illuminate.scene.Rgb;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

// Rain drops, snowflakes, Nether embers and End motes drifting through the room
public final class AtmosphereLayer implements Layer {
	private static final int MAX_PARTICLES = 48;
	private static final Rgb RAIN = new Rgb(0.55F, 0.68F, 0.95F);
	private static final Rgb SNOW = new Rgb(0.95F, 0.97F, 1.0F);
	private static final Rgb EMBER = new Rgb(1.0F, 0.45F, 0.08F);
	private static final Rgb MOTE = new Rgb(0.8F, 0.55F, 1.0F);

	private final List<Particle> particles = new ArrayList<>();
	private final SplittableRandom random = new SplittableRandom();
	private float spawnBudget;
	private GameState.Dimension dimension = GameState.Dimension.OTHER;

	@Override
	public void paint(Canvas canvas, GameState state, Moment moment) {
		float delta = moment.delta();
		// Embers belong to the Nether and rain to the Overworld, so nothing drifts along into another dimension
		if (state.dimension() != dimension) {
			dimension = state.dimension();
			particles.clear();
		}
		if (moment.config().weather && (state.fluid() == FogType.ATMOSPHERIC || state.fluid() == FogType.NONE)) {
			spawn(state, delta);
		}

		List<Particle> alive = new ArrayList<>(particles.size());
		for (Particle particle : particles) {
			particle.age += delta;
			particle.u += particle.speedU * delta;
			particle.v += particle.speedV * delta;
			if (particle.age < particle.life && particle.v > -0.1F && particle.v < 1.1F) {
				splat(canvas, particle);
				alive.add(particle);
			}
		}
		particles.clear();
		particles.addAll(alive);
	}

	private void spawn(GameState state, float delta) {
		float rate;
		Rgb color;
		boolean rising = false;
		float speed;
		if (state.dimension() == GameState.Dimension.NETHER) {
			rate = 5.0F;
			color = EMBER;
			rising = true;
			speed = 0.25F;
		} else if (state.dimension() == GameState.Dimension.END) {
			rate = 2.0F;
			color = MOTE;
			speed = 0.05F;
		} else if (state.precipitation() == Biome.Precipitation.RAIN) {
			rate = 14.0F * state.rain() + 10.0F * state.thunder();
			color = RAIN;
			speed = 2.2F;
		} else if (state.precipitation() == Biome.Precipitation.SNOW) {
			rate = 8.0F * state.rain();
			color = SNOW;
			speed = 0.35F;
		} else {
			return;
		}

		spawnBudget += rate * delta;
		while (spawnBudget >= 1.0F && particles.size() < MAX_PARTICLES) {
			spawnBudget -= 1.0F;
			float drift = (float) random.nextDouble(-0.05, 0.05);
			float v = rising ? 1.05F : (speed < 0.1F ? (float) random.nextDouble() : -0.05F);
			float life = speed < 0.1F ? 2.5F : 4.0F;
			particles.add(new Particle((float) random.nextDouble(), v, drift, rising ? -speed : speed, life, color));
		}
		spawnBudget = Math.min(spawnBudget, 1.0F);
	}

	private static void splat(Canvas canvas, Particle particle) {
		float fade = Math.min(particle.age / 0.2F, 1.0F) * Math.min((particle.life - particle.age) / 0.4F, 1.0F);
		float x = particle.u * Canvas.WIDTH - 0.5F;
		float y = particle.v * Canvas.HEIGHT - 0.5F;
		for (int i = 0; i < Canvas.SIZE; i++) {
			float dx = i % Canvas.WIDTH - x;
			float dy = i / Canvas.WIDTH - y;
			float weight = (float) Math.exp(-(dx * dx + dy * dy) / 0.6F);
			if (weight > 0.02F) {
				canvas.mix(i, particle.color, weight * fade * 0.55F);
			}
		}
	}

	private static final class Particle {
		private float u;
		private float v;
		private final float speedU;
		private final float speedV;
		private final float life;
		private final Rgb color;
		private float age;

		private Particle(float u, float v, float speedU, float speedV, float life, Rgb color) {
			this.u = u;
			this.v = v;
			this.speedU = speedU;
			this.speedV = speedV;
			this.life = life;
			this.color = color;
		}
	}
}
