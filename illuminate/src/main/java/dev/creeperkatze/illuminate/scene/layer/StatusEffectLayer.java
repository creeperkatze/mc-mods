package dev.creeperkatze.illuminate.scene.layer;

import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Layer;
import dev.creeperkatze.illuminate.scene.Moment;
import dev.creeperkatze.illuminate.scene.Rgb;
import dev.creeperkatze.illuminate.scene.Shapes;

// Tints the room in the colours of your status effects and boss bars, and handles effects that change how you see
public final class StatusEffectLayer implements Layer {
	private Rgb tint = Rgb.BLACK;
	private float tintAmount;
	private Rgb boss = Rgb.BLACK;
	private float bossAmount;

	@Override
	public void paint(Canvas canvas, GameState state, Moment moment) {
		float delta = moment.delta();
		boolean enabled = moment.config().statusEffects;
		updateTint(state, enabled, delta);

		boolean showBoss = moment.config().bossBars && state.bossColor() != null;
		if (state.bossColor() != null) {
			boss = boss.lerp(state.bossColor(), 1.0F - (float) Math.exp(-4.0F * delta));
		}
		bossAmount = Shapes.approach(bossAmount, showBoss ? 1.0F : 0.0F, 2.0F, delta);

		float time = (float) moment.time();
		float breathing = 0.8F + 0.2F * (float) Math.sin(time * 2.0F);
		float darkness = enabled ? state.darkness() * (0.55F + 0.4F * (float) Math.max(0.0, Math.cos(time * Math.PI / 1.6))) : 0.0F;
		float hueShift = enabled ? (float) Math.sin(time * 1.3F) * 80.0F * state.nausea() : 0.0F;

		for (int i = 0; i < Canvas.SIZE; i++) {
			float u = Canvas.u(i);
			float v = Canvas.v(i);
			if (tintAmount > 0.01F) {
				canvas.mix(i, tint, tintAmount * breathing * (0.4F + 0.6F * (float) Math.pow(Shapes.sides(u), 1.5)));
			}
			if (bossAmount > 0.01F) {
				canvas.mix(i, boss, bossAmount * 0.5F * Math.max(0.0F, 1.0F - v * 2.2F));
			}
			if (!enabled) {
				continue;
			}
			if (state.nightVision() > 0.0F) {
				Rgb color = canvas.get(i);
				canvas.set(i, color.lerp(color.atLeast(0.55F), state.nightVision() * 0.6F));
			}
			if (hueShift != 0.0F) {
				canvas.set(i, canvas.get(i).rotateHue(hueShift));
			}
			canvas.multiply(i, (1.0F - 0.85F * state.blindness()) * (1.0F - darkness));
		}
	}

	private void updateTint(GameState state, boolean enabled, float delta) {
		float r = 0.0F;
		float g = 0.0F;
		float b = 0.0F;
		float total = 0.0F;
		if (enabled) {
			for (GameState.ActiveEffect effect : state.effects()) {
				float weight = (effect.harmful() ? 1.3F : 1.0F) * (1.0F + effect.amplifier() * 0.5F);
				r += effect.color().r() * weight;
				g += effect.color().g() * weight;
				b += effect.color().b() * weight;
				total += weight;
			}
		}
		float target = 0.0F;
		if (total > 0.0F) {
			Rgb average = new Rgb(r / total, g / total, b / total).atLeast(0.6F);
			tint = tint.lerp(average, 1.0F - (float) Math.exp(-3.0F * delta));
			target = Math.min(0.5F, 0.3F + 0.06F * state.effects().size());
		}
		tintAmount = Shapes.approach(tintAmount, target, 2.5F, delta);
	}
}
