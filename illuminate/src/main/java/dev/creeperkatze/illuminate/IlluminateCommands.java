package dev.creeperkatze.illuminate;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.creeperkatze.illuminate.game.GameEvents;
import dev.creeperkatze.illuminate.preview.TestEffect;
import dev.creeperkatze.illuminate.preview.TestEffect.Side;

public final class IlluminateCommands {
	private IlluminateCommands() {
	}

	public static <S> LiteralArgumentBuilder<S> create() {
		LiteralArgumentBuilder<S> debug = LiteralArgumentBuilder.literal("debug");
		for (TestEffect effect : TestEffect.values()) {
			LiteralArgumentBuilder<S> node = LiteralArgumentBuilder.<S>literal(effect.id())
					.executes(context -> play(effect, Side.NONE));
			if (effect.directional()) {
				for (Side side : Side.values()) {
					if (side != Side.NONE) {
						node.then(LiteralArgumentBuilder.<S>literal(side.id())
								.executes(context -> play(effect, side)));
					}
				}
			}
			debug.then(node);
		}
		return LiteralArgumentBuilder.<S>literal(Illuminate.MOD_ID).then(debug);
	}

	private static int play(TestEffect effect, Side side) {
		GameEvents.post(effect.event(side));
		return 1;
	}
}
