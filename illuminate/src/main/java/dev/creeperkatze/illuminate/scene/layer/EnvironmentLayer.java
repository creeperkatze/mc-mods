package dev.creeperkatze.illuminate.scene.layer;

import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.scene.BiomeFeel;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Layer;
import dev.creeperkatze.illuminate.scene.Moment;
import dev.creeperkatze.illuminate.scene.Rgb;
import dev.creeperkatze.illuminate.scene.Shapes;

// The feel of the biome you are in, the fog colours of the Nether and End, darkening into caves
public final class EnvironmentLayer implements Layer {
	private static final float DARK_FLOOR = 0.03F;
	// Cave biomes like lush caves glow a little even without torches
	private static final float UNDERGROUND_LIGHT = 0.5F;
	private static final Rgb LAVA_GLOW = new Rgb(1.0F, 0.32F, 0.04F);
	private static final Rgb END_TINT = new Rgb(0.45F, 0.2F, 0.6F);
	private static final Rgb DAWN_RED = new Rgb(1.0F, 0.18F, 0.05F);
	private static final Rgb MOON = new Rgb(0.72F, 0.8F, 1.0F);
	private static final Rgb MOONLIGHT = new Rgb(0.4F, 0.48F, 0.72F);

	@Override
	public void paint(Canvas canvas, GameState state, Moment moment) {
		if (!moment.config().environment) {
			canvas.fill(Rgb.BLACK);
			return;
		}
		switch (state.dimension()) {
			case NETHER -> paintNether(canvas, state);
			case END -> paintEnd(canvas, state);
			default -> {
				if (state.hasSky()) {
					paintOverworld(canvas, state, moment);
				} else {
					canvas.fill(state.horizon().atLeast(0.25F));
				}
			}
		}
	}

	private void paintOverworld(Canvas canvas, GameState state, Moment moment) {
		BiomeFeel feel = state.feel();
		float time = (float) moment.time();
		// Weather, dawn, dusk and the moon only reach you where you can see the sky
		float exposure = Shapes.smoothstep(0.15F, 0.85F, state.skyLight());
		// The game's sky light never drops below about a fifth at night, so it is stretched to make nights properly dark
		float day = Shapes.smoothstep(0.2F, 1.0F, state.daylight());
		float light = Math.max(day * (0.35F + 0.65F * state.skyLight()), state.blockLight() * 0.7F);
		Rgb cave = state.blockTint().scale(0.04F + 0.6F * state.blockLight()).atLeast(DARK_FLOOR);
		// The game's dawn and dusk colours are soft, so they are pushed further towards red
		Rgb dawn = state.sunGlow().saturate(1.8F).lerp(DAWN_RED, 0.35F);

		for (int i = 0; i < Canvas.SIZE; i++) {
			float u = Canvas.u(i);
			float v = Canvas.v(i);
			Rgb biome = feel.base().lerp(feel.accent(), feel.motion().amount(i, u, v, time) * feel.strength());
			Rgb outdoor = darkenForWeather(biome, state).scale(Math.max(light, 0.06F)).atLeast(DARK_FLOOR);
			// Normal caves take the warm light of torches, cave biomes keep their own feel
			Rgb indoor = feel.underground() ? biome.scale(Math.max(light, UNDERGROUND_LIGHT)) : cave.scale(0.8F + 0.2F * v);
			Rgb color = indoor.lerp(outdoor, exposure);
			// Dawn, dusk and the moon sit on a coloured biome instead of a plain sky, so they are big
			if (state.sunGlowStrength() > 0.0F) {
				float band = Shapes.gaussian(v, 0.45F, 0.25F);
				float spot = Shapes.gaussian(u, state.sunU(), 0.25F) * Shapes.gaussian(v, 0.45F, 0.2F);
				float glow = state.sunGlowStrength() * (0.07F + 0.22F * band + 0.9F * spot);
				color = color.lerp(dawn, Math.min(glow, 0.6F) * exposure);
			}
			if (state.moonlight() > 0.0F) {
				// Moonlight lifts the whole night a little, more in the sky than on the ground
				color = color.lerp(MOONLIGHT, state.moonlight() * (0.06F + 0.06F * (1.0F - v)) * exposure);
			}
			if (state.moonStrength() > 0.0F) {
				float spot = Shapes.gaussian(u, state.moonU(), 0.06F) * Shapes.gaussian(v, state.moonV(), 0.06F);
				color = color.lerp(MOON, Math.min(state.moonStrength() * 0.5F * spot, 0.5F) * exposure);
			}
			canvas.set(i, color);
		}
	}

	private void paintNether(Canvas canvas, GameState state) {
		Rgb base = state.horizon().atLeast(0.4F);
		for (int i = 0; i < Canvas.SIZE; i++) {
			float v = Canvas.v(i);
			Rgb color = base.scale(0.7F + 0.3F * v);
			canvas.set(i, color.lerp(LAVA_GLOW, Shapes.smoothstep(0.6F, 1.0F, v) * 0.3F));
		}
	}

	private void paintEnd(Canvas canvas, GameState state) {
		Rgb base = state.horizon().lerp(END_TINT, 0.5F).atLeast(0.3F);
		for (int i = 0; i < Canvas.SIZE; i++) {
			canvas.set(i, base.scale(0.75F + 0.25F * (1.0F - Canvas.v(i))));
		}
	}

	// Rain and thunder dim the sky the same way the game does
	private static Rgb darkenForWeather(Rgb color, GameState state) {
		Rgb result = color;
		if (state.rain() > 0.0F) {
			float modifier = 1.0F - state.rain() * 0.5F;
			result = new Rgb(result.r() * modifier, result.g() * modifier, result.b() * (1.0F - state.rain() * 0.4F));
		}
		if (state.thunder() > 0.0F) {
			result = result.scale(1.0F - state.thunder() * 0.5F);
		}
		return result;
	}
}
