package dev.creeperkatze.illuminate;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.creeperkatze.illuminate.game.GameEvent;
import dev.creeperkatze.illuminate.game.GameEvents;
import dev.creeperkatze.illuminate.scene.Rgb;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class IlluminateCommands {
	private IlluminateCommands() {
	}

	public static <S> LiteralArgumentBuilder<S> create() {
		LiteralArgumentBuilder<S> debug = LiteralArgumentBuilder.literal("debug");
		for (TestEffect effect : TestEffect.values()) {
			LiteralArgumentBuilder<S> node = LiteralArgumentBuilder.<S>literal(effect.name().toLowerCase(Locale.ROOT))
					.executes(context -> play(effect, Float.NaN));
			if (effect.directional) {
				for (Side side : Side.values()) {
					node.then(LiteralArgumentBuilder.<S>literal(side.name().toLowerCase(Locale.ROOT))
							.executes(context -> play(effect, side.u)));
				}
			}
			debug.then(node);
		}
		return LiteralArgumentBuilder.<S>literal(Illuminate.MOD_ID).then(debug);
	}

	private static int play(TestEffect effect, float u) {
		GameEvents.post(effect.event.apply(u));
		return 1;
	}

	private enum TestEffect {
		DAMAGE(true, u -> new GameEvent.Damage("generic", u)),
		FIRE_DAMAGE(true, u -> new GameEvent.Damage("in_fire", u)),
		FREEZE_DAMAGE(true, u -> new GameEvent.Damage("freeze", u)),
		MAGIC_DAMAGE(true, u -> new GameEvent.Damage("magic", u)),
		DROWN_DAMAGE(true, u -> new GameEvent.Damage("drown", u)),
		EXPLOSION(true, u -> new GameEvent.Explosion(1.0F, u)),
		LIGHTNING(true, u -> new GameEvent.Lightning(1.0F, u)),
		FIREWORK(true,
				u -> new GameEvent.Firework(List.of(Rgb.of(0xFF3355), Rgb.of(0x33AAFF), Rgb.of(0xFFDD33)),
						List.of(Rgb.of(0xFFFFFF)), 1.0F,
						Float.isNaN(u) ? 0.5F : u, 0.3F)),
		TOTEM(false, u -> new GameEvent.Totem()),
		LEVEL_UP(false, u -> new GameEvent.LevelUp(1)),
		LEVEL_UP_5(false, u -> new GameEvent.LevelUp(5));

		private final boolean directional;
		private final Function<Float, GameEvent> event;

		TestEffect(boolean directional, Function<Float, GameEvent> event) {
			this.directional = directional;
			this.event = event;
		}
	}

	private enum Side {
		LEFT(0.1F),
		CENTER(0.5F),
		RIGHT(0.9F);

		private final float u;

		Side(float u) {
			this.u = u;
		}
	}
}
