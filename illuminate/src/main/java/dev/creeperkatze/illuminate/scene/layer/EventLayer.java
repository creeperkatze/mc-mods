package dev.creeperkatze.illuminate.scene.layer;

import dev.creeperkatze.illuminate.game.GameEvent;
import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Layer;
import dev.creeperkatze.illuminate.scene.Moment;
import dev.creeperkatze.illuminate.scene.Rgb;
import dev.creeperkatze.illuminate.scene.Shapes;

import java.util.ArrayList;
import java.util.List;

// Short flashes for things that happen to you or around you
public final class EventLayer implements Layer {
	private static final int MAX_ACTIVE = 16;
	// Flashes hold at full strength for most of their time, then fade out quickly
	private static final float DAMAGE_SECONDS = 0.55F;
	private static final float EXPLOSION_SECONDS = 0.7F;
	private static final float LIGHTNING_SECONDS = 0.65F;
	private static final float HOLD = 0.55F;
	private static final float FIREWORK_SECONDS = 1.5F;
	private static final Rgb LIGHTNING = new Rgb(0.85F, 0.88F, 1.0F);
	private static final Rgb EXPLOSION = new Rgb(1.0F, 0.85F, 0.55F);
	private static final Rgb EXPLOSION_END = new Rgb(1.0F, 0.3F, 0.0F);
	private static final Rgb TOTEM = new Rgb(1.0F, 0.82F, 0.2F);
	private static final Rgb TOTEM_END = new Rgb(0.35F, 0.9F, 0.3F);
	private static final Rgb LEVEL_UP = new Rgb(0.55F, 1.0F, 0.25F);

	private final List<Effect> active = new ArrayList<>();

	@Override
	public void onEvent(GameEvent event, Moment moment) {
		var config = moment.config();
		float strength = moment.flashStrength();
		boolean calm = config.reduceFlashing;
		Effect effect = switch (event) {
			case GameEvent.Damage damage when config.damage ->
					new Effect(Kind.FLASH, damageColor(damage.type()), damageColor(damage.type()), damage.u(), strength * 0.85F, DAMAGE_SECONDS);
			case GameEvent.Explosion explosion when config.worldEvents ->
					new Effect(Kind.FLASH, EXPLOSION, EXPLOSION_END, explosion.u(), strength * explosion.strength(), EXPLOSION_SECONDS);
			case GameEvent.Lightning lightning when config.worldEvents ->
					new Effect(calm ? Kind.FLASH : Kind.LIGHTNING, LIGHTNING, LIGHTNING, lightning.u(), strength * lightning.strength(), LIGHTNING_SECONDS);
			case GameEvent.Firework firework when config.fireworks -> firework(firework, strength);
			case GameEvent.Totem totem when config.worldEvents ->
					new Effect(Kind.RING, TOTEM, TOTEM_END, Float.NaN, Math.max(strength, 0.5F), 1.6F);
			case GameEvent.LevelUp levelUp when config.worldEvents ->
					new Effect(Kind.SPARKLE, LEVEL_UP, LEVEL_UP, Float.NaN, levelUp.level() % 5 == 0 ? 1.0F : 0.6F, levelUp.level() % 5 == 0 ? 1.6F : 1.0F);
			default -> null;
		};
		if (effect != null && active.size() < MAX_ACTIVE) {
			effect.start = moment.time();
			active.add(effect);
		}
	}

	private static Effect firework(GameEvent.Firework firework, float strength) {
		Effect effect = new Effect(Kind.FIREWORK, Rgb.BLACK, Rgb.BLACK, firework.u(), strength * firework.strength(), FIREWORK_SECONDS);
		effect.v = firework.v();
		effect.palette = firework.colors();
		effect.fadePalette = firework.fadeColors();
		return effect;
	}

	@Override
	public void paint(Canvas canvas, GameState state, Moment moment) {
		active.removeIf(effect -> moment.time() - effect.start >= effect.duration);
		for (Effect effect : active) {
			float age = (float) ((moment.time() - effect.start) / effect.duration);
			paint(canvas, effect, age);
		}
	}

	private static void paint(Canvas canvas, Effect effect, float age) {
		Rgb color = effect.from.lerp(effect.to, age);
		for (int i = 0; i < Canvas.SIZE; i++) {
			float u = Canvas.u(i);
			float v = Canvas.v(i);
			switch (effect.kind) {
				case FLASH -> canvas.mix(i, color, effect.strength * fade(age) * Shapes.towards(u, effect.u, 0.06F));
				case LIGHTNING -> {
					// A short dip makes it flicker like the strike in game
					float flicker = age > 0.15F && age < 0.3F ? 0.25F : fade(age);
					float sky = 0.6F + 0.4F * (1.0F - v);
					canvas.mix(i, color, effect.strength * flicker * sky * Shapes.towards(u, effect.u, 0.15F));
				}
				case RING -> {
					float dx = (u - 0.5F) * 1.6F;
					float dy = v - 0.5F;
					float distance = (float) Math.sqrt(dx * dx + dy * dy);
					float ring = Shapes.gaussian(distance, age * 1.2F, 0.01F);
					canvas.mix(i, color, effect.strength * (1.0F - age) * (ring * 0.9F + 0.25F));
				}
				case SPARKLE -> {
					if (Shapes.hash(i, effect.seed) < 0.3F) {
						float twinkle = (float) Math.sin(Math.PI * Math.min(age * 1.4F + Shapes.hash(effect.seed, i) * 0.3F, 1.0F));
						canvas.mix(i, color, effect.strength * 0.8F * twinkle);
					}
				}
				case FIREWORK -> paintFirework(canvas, effect, i, u, v, age);
			}
		}
	}

	// An expanding shell of sparks in the firework's colours that turn into its fade colours
	private static void paintFirework(Canvas canvas, Effect effect, int cell, float u, float v, float age) {
		float dx = (u - effect.u) * 1.78F;
		float dy = v - effect.v;
		float distance = (float) Math.sqrt(dx * dx + dy * dy);
		float shell = Math.max(0.0F, 1.0F - Math.abs(distance - (0.1F + age * 0.7F)) / 0.2F);
		float twinkle = 0.75F + 0.25F * Shapes.hash(cell, effect.seed + (int) (age * 24.0F));
		int pick = (int) (Shapes.hash(cell, effect.seed) * effect.palette.size()) % effect.palette.size();
		Rgb spark = effect.palette.get(pick);
		Rgb fade = effect.fadePalette.isEmpty() ? spark.scale(0.5F) : effect.fadePalette.get(pick % effect.fadePalette.size());
		float glow = 0.25F * (1.0F - age);
		canvas.mix(cell, spark.lerp(fade, Shapes.smoothstep(0.3F, 0.9F, age)), effect.strength * (1.0F - age) * Math.min(shell * twinkle + glow, 1.0F));
	}

	private static float fade(float age) {
		if (age < HOLD) {
			return 1.0F;
		}
		float remaining = 1.0F - (age - HOLD) / (1.0F - HOLD);
		return remaining * remaining * (3.0F - 2.0F * remaining);
	}

	private static Rgb damageColor(String type) {
		return switch (type) {
			case "in_fire", "on_fire", "campfire", "lava", "hot_floor", "fireball", "unattributed_fireball" -> new Rgb(1.0F, 0.42F, 0.0F);
			case "freeze" -> new Rgb(0.62F, 0.9F, 1.0F);
			case "magic", "indirect_magic", "dragon_breath", "sonic_boom", "thorns" -> new Rgb(0.7F, 0.25F, 1.0F);
			case "wither", "wither_skull" -> new Rgb(0.35F, 0.15F, 0.4F);
			case "drown" -> new Rgb(0.12F, 0.35F, 1.0F);
			case "lightning_bolt" -> LIGHTNING;
			case "starve" -> new Rgb(0.8F, 0.5F, 0.12F);
			case "cactus", "sweet_berry_bush", "sting" -> new Rgb(0.45F, 0.85F, 0.2F);
			case "explosion", "player_explosion", "fireworks" -> EXPLOSION;
			case "out_of_world", "outside_border" -> new Rgb(0.4F, 0.0F, 0.6F);
			default -> new Rgb(1.0F, 0.05F, 0.05F);
		};
	}

	private enum Kind {
		FLASH,
		LIGHTNING,
		RING,
		SPARKLE,
		FIREWORK
	}

	private static final class Effect {
		private static int nextSeed;

		private final Kind kind;
		private final Rgb from;
		private final Rgb to;
		private final float u;
		private final float strength;
		private final float duration;
		private final int seed = nextSeed++;
		private double start;
		private float v = 0.5F;
		private List<Rgb> palette = List.of();
		private List<Rgb> fadePalette = List.of();

		private Effect(Kind kind, Rgb from, Rgb to, float u, float strength, float duration) {
			this.kind = kind;
			this.from = from;
			this.to = to;
			this.u = u;
			this.strength = strength;
			this.duration = duration;
		}
	}
}
