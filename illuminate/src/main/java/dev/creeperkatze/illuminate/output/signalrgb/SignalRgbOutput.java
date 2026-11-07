package dev.creeperkatze.illuminate.output.signalrgb;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.output.LightOutput;
import dev.creeperkatze.illuminate.output.RateCounter;
import dev.creeperkatze.illuminate.scene.Canvas;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

// Sends the scene to the Illuminate effect through SignalRGB's canvas event API
public final class SignalRgbOutput implements LightOutput {
	private static final String SENDER = "illuminate";
	private static final String RELEASE = "release";
	private static final long RETRY_MS = 3000;
	private static final long KEEPALIVE_MS = 1000;

	private final int port;
	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(800)).build();
	private final AtomicBoolean inFlight = new AtomicBoolean();
	private volatile String status = "connecting";
	private volatile long retryAt;
	private volatile boolean reachable;
	private boolean warned;
	private boolean applied;
	private String lastPayload = "";
	private long lastSentAt;
	private final RateCounter sends = new RateCounter();
	private volatile long roundTripMillis;

	public SignalRgbOutput(int port) {
		this.port = port;
	}

	public boolean matches(int port) {
		return this.port == port;
	}

	@Override
	public String status() {
		return status;
	}

	public float sendsPerSecond() {
		return sends.perSecond();
	}

	public long roundTripMillis() {
		return roundTripMillis;
	}

	public void send(Canvas canvas, long now) {
		sends.update(now);
		if (now < retryAt) {
			return;
		}
		if (reachable && !applied) {
			applied = true;
			if (Illuminate.config().signalRgbApplyEffect && SignalRgbEffect.isInstalled()) {
				SignalRgbEffect.apply();
			}
		}
		String payload = encode(canvas);
		if (payload.equals(lastPayload) && now - lastSentAt < KEEPALIVE_MS) {
			return;
		}
		if (post(payload, now)) {
			sends.count();
			lastPayload = payload;
			lastSentAt = now;
		}
	}

	private boolean post(String payload, long now) {
		if (!inFlight.compareAndSet(false, true)) {
			return false;
		}
		dispatch(payload, now);
		return true;
	}

	private void dispatch(String payload, long now) {
		URI uri = URI.create("http://127.0.0.1:" + port + "/canvas/event?sender=" + SENDER + "&event=" + payload);
		HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(2)).POST(HttpRequest.BodyPublishers.noBody()).build();
		long sentAt = System.nanoTime();
		http.sendAsync(request, HttpResponse.BodyHandlers.discarding()).whenComplete((response, error) -> {
			inFlight.set(false);
			if (error == null && response.statusCode() == 200) {
				reachable = true;
				roundTripMillis = (System.nanoTime() - sentAt) / 1_000_000L;
				status = "connected";
				warned = false;
				return;
			}
			reachable = false;
			status = "unavailable";
			retryAt = now + RETRY_MS;
			if (!warned) {
				warned = true;
				Illuminate.LOGGER.info("SignalRGB is not reachable on port {}: {}", port, error != null ? error.getMessage() : "HTTP " + response.statusCode());
			}
		});
	}

	// Width, height and one hex colour per cell, like 16x9:ff8800...
	private static String encode(Canvas canvas) {
		StringBuilder builder = new StringBuilder(8 + Canvas.SIZE * 6);
		builder.append(Canvas.WIDTH).append('x').append(Canvas.HEIGHT).append(':');
		for (int color : canvas.toRgb24()) {
			String hex = Integer.toHexString(color);
			builder.repeat('0', 6 - hex.length()).append(hex);
		}
		return builder.toString();
	}

	@Override
	public void release() {
		if (!lastPayload.isEmpty() && reachable) {
			dispatch(RELEASE, System.currentTimeMillis());
		}
		lastPayload = "";
		applied = false;
	}

	@Override
	public void close() {
		release();
		status = "disabled";
	}
}
