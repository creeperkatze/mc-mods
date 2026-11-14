package dev.creeperkatze.illuminate.preview;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.engine.LightingEngine;
import dev.creeperkatze.illuminate.game.GameEvent;
import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.game.GameStateCollector;
import dev.creeperkatze.illuminate.output.openrgb.OpenRgbOutput;
import dev.creeperkatze.illuminate.output.signalrgb.SignalRgbOutput;
import dev.creeperkatze.illuminate.scene.Rgb;
//? if >=1.21.9 {
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
//?}
import net.minecraft.world.level.material.FogType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Illuminate's lines on the F3 screen, turned on in the game's debug options
// Before 1.21.9 they are always shown, see DebugScreenOverlayMixin
//? if >=1.21.9 {
public final class IlluminateDebugEntry implements DebugScreenEntry {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(Illuminate.MOD_ID, Illuminate.MOD_ID);

	@Override
	public void display(DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
		displayer.addToGroup(ID, lines());
	}

	@Override
	public boolean isAllowed(boolean reducedDebugInfo) {
		// Nothing here gives away the world, so it shows with reduced debug info too
		return true;
	}
//?} else {
/*public final class IlluminateDebugEntry {
*///?}
	public static List<String> lines() {
		LightingEngine engine = Illuminate.engine();
		GameState state = GameStateCollector.current();
		List<String> lines = new ArrayList<>();
		lines.add(format("Illuminate: %.0f/%d FPS, %.2f ms", engine.framesPerSecond(), Illuminate.config().frameRate, engine.frameMillis()));
		lines.add(signalRgb(engine.signalRgb()) + ", " + openRgb(engine.openRgb()));
		lines.add(output(engine.preview()));
		if (state.inWorld()) {
			lines.add(format("Biome feel: %s, %s, fog #%06x", state.feel().name(), state.feel().motion().name().toLowerCase(Locale.ROOT), state.horizon().toRgb24()));
			lines.add(format("Dawn %.2f, moon %.2f, moonlight %.2f", state.sunGlowStrength(), state.moonStrength(), state.moonlight()));
			String active = active(state);
			if (!active.isEmpty()) {
				lines.add("Active: " + active);
			}
		}
		GameEvent event = engine.lastEvent();
		if (event != null) {
			lines.add(format("Last event: %s, %.1fs ago", describe(event), (System.currentTimeMillis() - engine.lastEventAt()) / 1000.0F));
		}
		return lines;
	}

	private static String signalRgb(@Nullable SignalRgbOutput output) {
		if (output == null || !"connected".equals(output.status())) {
			return "SignalRGB " + (output == null ? "off" : output.status());
		}
		return format("SignalRGB %.0f/s %d ms", output.sendsPerSecond(), output.roundTripMillis());
	}

	private static String openRgb(@Nullable OpenRgbOutput output) {
		if (output == null || !"connected".equals(output.status())) {
			return "OpenRGB " + (output == null ? "off" : output.status());
		}
		return format("OpenRGB %d devices %d LEDs %.0f/s", output.deviceNames().size(), output.ledCount(), output.writesPerSecond());
	}

	private static String output(int @Nullable [] colors) {
		if (colors == null) {
			return "Output: released";
		}
		float r = 0.0F;
		float g = 0.0F;
		float b = 0.0F;
		for (int color : colors) {
			r += ((color >> 16) & 0xFF) / 255.0F;
			g += ((color >> 8) & 0xFF) / 255.0F;
			b += (color & 0xFF) / 255.0F;
		}
		Rgb average = new Rgb(r / colors.length, g / colors.length, b / colors.length);
		return format("Output: #%06x, %d%% bright", average.toRgb24(), Math.round(average.max() * 100.0F));
	}

	// The effects that are changing the lights right now
	private static String active(GameState state) {
		List<String> active = new ArrayList<>();
		if (state.fluid() == FogType.WATER) {
			active.add(state.air() < 0.5F ? "drowning" : "underwater");
		} else if (state.fluid() == FogType.LAVA) {
			active.add("lava");
		} else if (state.fluid() == FogType.POWDER_SNOW) {
			active.add("powder snow");
		}
		if (state.portal() > 0.0F) {
			active.add("portal");
		}
		if (state.burning()) {
			active.add("burning");
		}
		if (state.frozen() > 0.0F) {
			active.add("freezing");
		}
		if (state.health() < 0.3F && !state.dead()) {
			active.add("heartbeat");
		}
		if (state.sleep() > 0.0F) {
			active.add("sleeping");
		}
		if (state.dead()) {
			active.add("dead");
		}
		if (state.deepDark()) {
			active.add("deep dark");
		}
		if (!state.effects().isEmpty()) {
			active.add(state.effects().size() + " effects");
		}
		if (state.bossColor() != null) {
			active.add("boss bar");
		}
		if (state.menuOpen()) {
			active.add("menu");
		}
		return String.join(", ", active);
	}

	private static String describe(GameEvent event) {
		return switch (event) {
			case GameEvent.Damage damage -> "damage " + damage.type() + side(damage.u());
			case GameEvent.Explosion explosion -> "explosion" + side(explosion.u());
			case GameEvent.Lightning lightning -> "lightning" + side(lightning.u());
			case GameEvent.Firework firework -> "firework" + side(firework.u());
			case GameEvent.Totem totem -> "totem";
			case GameEvent.LevelUp levelUp -> "level up";
			case GameEvent.Arrival arrival -> "arrival";
		};
	}

	private static String side(float u) {
		if (Float.isNaN(u)) {
			return "";
		}
		return u < 0.4F ? " left" : u > 0.6F ? " right" : " center";
	}

	private static String format(String pattern, Object... arguments) {
		return String.format(Locale.ROOT, pattern, arguments);
	}
}
