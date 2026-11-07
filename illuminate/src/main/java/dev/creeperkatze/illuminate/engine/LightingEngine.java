package dev.creeperkatze.illuminate.engine;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.config.IdleMode;
import dev.creeperkatze.illuminate.config.IlluminateConfig;
import dev.creeperkatze.illuminate.game.GameEvent;
import dev.creeperkatze.illuminate.game.GameEvents;
import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.game.GameStateCollector;
import dev.creeperkatze.illuminate.output.openrgb.OpenRgbOutput;
import dev.creeperkatze.illuminate.output.signalrgb.SignalRgbEffect;
import dev.creeperkatze.illuminate.output.signalrgb.SignalRgbOutput;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Moment;
import dev.creeperkatze.illuminate.scene.Scene;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class LightingEngine {
	private static final long ERROR_LOG_INTERVAL_MS = 30_000;
	private static final String CONNECTED = "connected";
	private static final SystemToast.SystemToastId OVERLAP_TOAST = new SystemToast.SystemToastId(10_000L);

	private final Scene scene = new Scene();
	private volatile boolean running;
	private @Nullable Thread thread;
	private volatile int @Nullable [] preview;
	private volatile @Nullable SignalRgbOutput signalRgb;
	private volatile @Nullable OpenRgbOutput openRgb;
	private volatile float framesPerSecond;
	private volatile float frameMillis;
	private volatile long previewingUntil;
	private volatile int eventCount;
	private volatile @Nullable GameEvent lastEvent;
	private volatile long lastEventAt;
	private boolean released = true;
	private boolean overlapShown;
	private long lastErrorAt;

	public void start() {
		running = true;
		Thread engine = new Thread(this::run, "Illuminate Lighting");
		engine.setDaemon(true);
		engine.start();
		thread = engine;

		Thread update = new Thread(SignalRgbEffect::updateIfInstalled, "Illuminate Effect Update");
		update.setDaemon(true);
		update.start();
	}

	public void shutdown() {
		running = false;
		Thread engine = thread;
		if (engine != null) {
			try {
				engine.join(2000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	// The last painted scene as RGB colours, or null while nothing is shown
	public int @Nullable [] preview() {
		return preview;
	}

	// Called while the live preview is drawn, it keeps effects playing outside a world for a moment after
	public void markPreviewing() {
		previewingUntil = System.currentTimeMillis() + 500L;
	}

	public boolean previewing() {
		return System.currentTimeMillis() < previewingUntil;
	}

	public float framesPerSecond() {
		return framesPerSecond;
	}

	// How long painting and sending one frame takes
	public float frameMillis() {
		return frameMillis;
	}

	public int eventCount() {
		return eventCount;
	}

	public @Nullable SignalRgbOutput signalRgb() {
		return signalRgb;
	}

	public @Nullable OpenRgbOutput openRgb() {
		return openRgb;
	}

	public @Nullable GameEvent lastEvent() {
		return lastEvent;
	}

	public long lastEventAt() {
		return lastEventAt;
	}

	public String signalRgbStatus() {
		SignalRgbOutput output = signalRgb;
		return output == null ? "disabled" : output.status();
	}

	public String openRgbStatus() {
		OpenRgbOutput output = openRgb;
		return output == null ? "disabled" : output.status();
	}

	public List<String> openRgbDevices() {
		OpenRgbOutput output = openRgb;
		return output == null ? List.of() : output.deviceNames();
	}

	private void run() {
		long last = System.nanoTime();
		double time = 0.0;
		try {
			while (running) {
				IlluminateConfig config = Illuminate.config();
				long now = System.nanoTime();
				float delta = Math.min((now - last) / 1.0E9F, 0.25F);
				last = now;
				time += delta;
				if (delta > 0.0F) {
					framesPerSecond += (1.0F / delta - framesPerSecond) * 0.1F;
				}
				try {
					long start = System.nanoTime();
					frame(config, time, delta);
					frameMillis += ((System.nanoTime() - start) / 1.0E6F - frameMillis) * 0.1F;
				} catch (RuntimeException e) {
					logError(e);
				}
				sleep(now, config.frameRate);
			}
		} finally {
			closeOutputs();
		}
	}

	private void frame(IlluminateConfig config, double time, float delta) {
		syncOutputs(config);
		GameState state = GameStateCollector.current();
		List<GameEvent> events = GameEvents.drain();
		for (GameEvent event : events) {
			eventCount++;
			lastEvent = event;
			lastEventAt = System.currentTimeMillis();
			if (config.logEvents) {
				Illuminate.LOGGER.info("Event: {}", event);
			}
		}
		boolean release = !config.enabled || (!state.inWorld() && config.idleMode == IdleMode.RELEASE && !previewing());
		if (release) {
			if (!released) {
				releaseOutputs();
				released = true;
			}
			preview = null;
			return;
		}
		released = false;

		Canvas canvas = scene.render(state, events, new Moment(time, delta, config, previewing()));
		preview = canvas.toRgb24();
		long millis = System.currentTimeMillis();
		SignalRgbOutput signal = signalRgb;
		if (signal != null) {
			signal.send(canvas, millis);
		}
		OpenRgbOutput open = openRgb;
		if (open != null) {
			open.send(canvas, millis, config.openRgbPlacements);
		}
		if (!overlapShown && signal != null && open != null && CONNECTED.equals(signal.status())
				&& CONNECTED.equals(open.status())) {
			overlapShown = true;
			showOverlapToast();
		}
	}

	// Both outputs may drive the same device, which makes it flicker, so point it
	// out once per launch
	private static void showOverlapToast() {
		Minecraft client = Minecraft.getInstance();
		client.execute(() -> SystemToast.add(client.gui.toastManager(), OVERLAP_TOAST,
				Component.translatable("illuminate.toast.overlap.title"),
				Component.translatable("illuminate.toast.overlap")));
	}

	private void syncOutputs(IlluminateConfig config) {
		SignalRgbOutput signal = signalRgb;
		if (signal != null && (!config.signalRgb || !signal.matches(config.signalRgbPort))) {
			signal.close();
			signalRgb = null;
		}
		if (config.signalRgb && signalRgb == null) {
			signalRgb = new SignalRgbOutput(config.signalRgbPort);
		}

		OpenRgbOutput open = openRgb;
		if (open != null && (!config.openRgb || !open.matches(config.openRgbHost, config.openRgbPort))) {
			open.close();
			openRgb = null;
		}
		if (config.openRgb && openRgb == null) {
			openRgb = new OpenRgbOutput(config.openRgbHost, config.openRgbPort);
		}
	}

	private void releaseOutputs() {
		SignalRgbOutput signal = signalRgb;
		if (signal != null) {
			signal.release();
		}
		OpenRgbOutput open = openRgb;
		if (open != null) {
			open.release();
		}
	}

	private void closeOutputs() {
		SignalRgbOutput signal = signalRgb;
		if (signal != null) {
			signal.close();
		}
		OpenRgbOutput open = openRgb;
		if (open != null) {
			open.close();
		}
	}

	private static void sleep(long frameStart, int frameRate) {
		long frameNanos = 1_000_000_000L / Math.clamp(frameRate, 10, 60);
		long remaining = frameStart + frameNanos - System.nanoTime();
		if (remaining > 0) {
			try {
				Thread.sleep(remaining / 1_000_000L, (int) (remaining % 1_000_000L));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void logError(RuntimeException e) {
		long now = System.currentTimeMillis();
		if (now - lastErrorAt > ERROR_LOG_INTERVAL_MS) {
			lastErrorAt = now;
			Illuminate.LOGGER.error("Illuminate failed to paint a frame", e);
		}
	}
}
