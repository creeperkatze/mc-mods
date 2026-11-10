package dev.creeperkatze.illuminate;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.creeperkatze.illuminate.game.GameEvents;
import dev.creeperkatze.illuminate.preview.TestEffect;
import dev.creeperkatze.illuminate.preview.TestEffect.Direction;

public final class IlluminateCommands {
	private IlluminateCommands() {
	}

	public static <S> LiteralArgumentBuilder<S> create() {
		LiteralArgumentBuilder<S> debug = LiteralArgumentBuilder.literal("debug");
		for (TestEffect effect : TestEffect.values()) {
			LiteralArgumentBuilder<S> node = LiteralArgumentBuilder.<S>literal(effect.id())
					.executes(context -> play(effect, Direction.NONE));
			if (effect.directional()) {
				for (Direction direction : Direction.values()) {
					if (direction != Direction.NONE) {
						node.then(LiteralArgumentBuilder.<S>literal(direction.id())
								.executes(context -> play(effect, direction)));
					}
				}
			}
			debug.then(node);
		}
		return LiteralArgumentBuilder.<S>literal(Illuminate.MOD_ID).then(debug);
	}

	private static int play(TestEffect effect, Direction direction) {
		GameEvents.post(effect.event(direction));
		return 1;
	}
}
