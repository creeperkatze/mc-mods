package dev.creeperkatze.illuminate.preview;

import dev.creeperkatze.illuminate.game.GameEvent;
import dev.creeperkatze.illuminate.scene.Rgb;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

// Effects the debug command and the preview settings can play without anything happening in the world
public enum TestEffect {
	DAMAGE(Kind.DAMAGE, true, u -> damage("generic", u)),
	FIRE_DAMAGE(Kind.DAMAGE, true, u -> damage("in_fire", u)),
	FREEZE_DAMAGE(Kind.DAMAGE, true, u -> damage("freeze", u)),
	MAGIC_DAMAGE(Kind.DAMAGE, true, u -> damage("magic", u)),
	WITHER_DAMAGE(Kind.DAMAGE, true, u -> damage("wither", u)),
	DROWN_DAMAGE(Kind.DAMAGE, true, u -> damage("drown", u)),
	LIGHTNING_DAMAGE(Kind.DAMAGE, true, u -> damage("lightning_bolt", u)),
	STARVE_DAMAGE(Kind.DAMAGE, true, u -> damage("starve", u)),
	CACTUS_DAMAGE(Kind.DAMAGE, true, u -> damage("cactus", u)),
	EXPLOSION_DAMAGE(Kind.DAMAGE, true, u -> damage("explosion", u)),
	VOID_DAMAGE(Kind.DAMAGE, true, u -> damage("out_of_world", u)),
	EXPLOSION(Kind.WORLD, true, u -> new GameEvent.Explosion(1.0F, u)),
	LIGHTNING(Kind.WORLD, true, u -> new GameEvent.Lightning(1.0F, u)),
	FIREWORK(Kind.WORLD, true,
			u -> new GameEvent.Firework(List.of(Rgb.of(0xFF3355), Rgb.of(0x33AAFF), Rgb.of(0xFFDD33)),
					List.of(Rgb.of(0xFFFFFF)), 1.0F, Float.isNaN(u) ? 0.5F : u, 0.3F)),
	TOTEM(Kind.PLAYER, false, u -> new GameEvent.Totem()),
	LEVEL_UP(Kind.PLAYER, false, u -> new GameEvent.LevelUp(1)),
	LEVEL_UP_5(Kind.PLAYER, false, u -> new GameEvent.LevelUp(5));

	private final Kind kind;
	private final boolean directional;
	private final Function<Float, GameEvent> event;

	TestEffect(Kind kind, boolean directional, Function<Float, GameEvent> event) {
		this.kind = kind;
		this.directional = directional;
		this.event = event;
	}

	public Kind kind() {
		return kind;
	}

	public boolean directional() {
		return directional;
	}

	public GameEvent event(Side side) {
		return event.apply(directional ? side.u : Float.NaN);
	}

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public Component caption() {
		return Component.translatable("illuminate.preview.test." + id());
	}

	private static GameEvent damage(String type, float u) {
		return new GameEvent.Damage(type, u);
	}

	public enum Kind {
		DAMAGE,
		WORLD,
		PLAYER;

		public Component caption() {
			return Component.translatable("illuminate.preview.kind." + name().toLowerCase(Locale.ROOT));
		}
	}

	public enum Side {
		NONE(Float.NaN),
		LEFT(0.1F),
		CENTER(0.5F),
		RIGHT(0.9F);

		private final float u;

		Side(float u) {
			this.u = u;
		}

		public String id() {
			return name().toLowerCase(Locale.ROOT);
		}

		public Component caption() {
			return Component.translatable("illuminate.preview.side." + id());
		}
	}
}
