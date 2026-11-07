package dev.creeperkatze.illuminate.scene.layer;

import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Layer;
import dev.creeperkatze.illuminate.scene.Moment;
import dev.creeperkatze.illuminate.scene.Rgb;
import dev.creeperkatze.illuminate.scene.Shapes;
import net.minecraft.world.level.material.FogType;

// Shimmering water, glowing lava and powder snow when your head is inside them
public final class SubmergedLayer implements Layer {
	private static final Rgb LAVA = new Rgb(1.0F, 0.28F, 0.02F);
	private static final Rgb LAVA_BRIGHT = new Rgb(1.0F, 0.62F, 0.1F);
	private static final Rgb POWDER_SNOW = new Rgb(0.82F, 0.9F, 1.0F);

	private FogType fluid = FogType.NONE;
	private float amount;

	@Override
	public void paint(Canvas canvas, GameState state, Moment moment) {
		boolean submerged = moment.config().submerged && state.fluid() != FogType.NONE && state.fluid() != FogType.ATMOSPHERIC;
		if (submerged) {
			fluid = state.fluid();
		}
		amount = Shapes.approach(amount, submerged ? 1.0F : 0.0F, 5.0F, moment.delta());
		if (amount < 0.01F) {
			return;
		}

		float time = (float) moment.time();
		for (int i = 0; i < Canvas.SIZE; i++) {
			float u = Canvas.u(i);
			float v = Canvas.v(i);
			Rgb color = switch (fluid) {
				case WATER -> water(state, u, v, time);
				case LAVA -> LAVA.lerp(LAVA_BRIGHT, Shapes.noise(u * 4.0F + time * 0.7F, v * 3.0F - time * 1.3F));
				default -> POWDER_SNOW.scale(0.8F + 0.2F * Shapes.noise(u * 6.0F, v * 4.0F + time * 0.5F));
			};
			canvas.mix(i, color, amount * 0.9F);
		}
	}

	private static Rgb water(GameState state, float u, float v, float time) {
		Rgb base = state.waterFog().atLeast(0.3F).scale(0.35F + 0.65F * state.skyLight());
		float caustics = (float) (Math.sin(u * 9.0F + time * 1.7F) * Math.sin(v * 7.0F - time * 1.3F + u * 3.0F));
		return base.scale(0.75F + 0.25F * caustics + 0.15F * (1.0F - v));
	}
}
