package dev.creeperkatze.illuminate.output.hyperion;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.output.LightOutput;
import dev.creeperkatze.illuminate.output.RateCounter;
import dev.creeperkatze.illuminate.scene.Canvas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

// Sends the scene as a small image to the JSON server of Hyperion or HyperHDR, which maps it onto its LEDs
public final class HyperionOutput implements LightOutput {
	// Above the screen grabbers and below colours and effects picked by hand in Hyperion
	private static final int PRIORITY = 100;
	private static final String ORIGIN = "Illuminate";
	// Hyperion drops the image on its own if Minecraft stops sending
	private static final int DURATION_MS = 3000;
	private static final long KEEPALIVE_MS = 1000;
	private static final long REPLY_TIMEOUT_MS = 2000;
	private static final long RETRY_MS = 5000;
	private static final int CONNECT_TIMEOUT_MS = 1500;

	private final String host;
	private final int port;
	private final AtomicBoolean connecting = new AtomicBoolean();
	private volatile Socket socket;
	private volatile String status = "connecting";
	// When the unanswered command was sent, or 0 when Hyperion has replied
	private volatile long awaitingSince;
	private volatile long roundTripMillis;
	private volatile boolean rejected;
	private long retryAt;
	private boolean warned;
	private String lastImage = "";
	private long lastSentAt;
	private final RateCounter sends = new RateCounter();

	public HyperionOutput(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public boolean matches(String host, int port) {
		return this.host.equals(host) && this.port == port;
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
		Socket current = socket;
		if (current == null) {
			connectLater(now);
			return;
		}
		long waiting = awaitingSince;
		if (waiting != 0 && now - waiting < REPLY_TIMEOUT_MS) {
			return;
		}
		String image = encode(canvas);
		if (image.equals(lastImage) && now - lastSentAt < KEEPALIVE_MS) {
			return;
		}
		String command = "{\"command\":\"image\",\"priority\":" + PRIORITY + ",\"origin\":\"" + ORIGIN
				+ "\",\"duration\":" + DURATION_MS + ",\"imagewidth\":" + Canvas.WIDTH + ",\"imageheight\":"
				+ Canvas.HEIGHT + ",\"imagedata\":\"" + image + "\"}";
		if (write(current, command, now)) {
			sends.count();
			lastImage = image;
			lastSentAt = now;
		}
	}

	private boolean write(Socket current, String command, long now) {
		try {
			OutputStream out = current.getOutputStream();
			out.write((command + "\n").getBytes(StandardCharsets.UTF_8));
			out.flush();
			awaitingSince = now;
			return true;
		} catch (IOException e) {
			Illuminate.LOGGER.warn("Lost connection to Hyperion: {}", e.getMessage());
			drop(current);
			return false;
		}
	}

	// One RGB byte triple per cell, row by row
	private static String encode(Canvas canvas) {
		int[] colors = canvas.toRgb24();
		byte[] pixels = new byte[colors.length * 3];
		for (int i = 0; i < colors.length; i++) {
			pixels[i * 3] = (byte) (colors[i] >> 16);
			pixels[i * 3 + 1] = (byte) (colors[i] >> 8);
			pixels[i * 3 + 2] = (byte) colors[i];
		}
		return Base64.getEncoder().encodeToString(pixels);
	}

	private void connectLater(long now) {
		if (now < retryAt || !connecting.compareAndSet(false, true)) {
			return;
		}
		retryAt = now + RETRY_MS;
		Thread thread = new Thread(this::connect, "Illuminate Hyperion");
		thread.setDaemon(true);
		thread.start();
	}

	private void connect() {
		Socket connection = new Socket();
		try {
			connection.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
			connection.setTcpNoDelay(true);
			awaitingSince = 0;
			rejected = false;
			lastImage = "";
			socket = connection;
			status = "connected";
			warned = false;
			Thread reader = new Thread(() -> read(connection), "Illuminate Hyperion Replies");
			reader.setDaemon(true);
			reader.start();
			Illuminate.LOGGER.info("Connected to Hyperion at {}:{}", host, port);
		} catch (IOException e) {
			status = "unavailable";
			closeQuietly(connection);
			if (!warned) {
				warned = true;
				Illuminate.LOGGER.info("Hyperion is not reachable at {}:{}: {}", host, port, e.getMessage());
			}
		} finally {
			connecting.set(false);
		}
	}

	// Hyperion answers every command, and reading the answers keeps its send buffer from filling up
	private void read(Socket connection) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				long sent = awaitingSince;
				if (sent != 0) {
					roundTripMillis = System.currentTimeMillis() - sent;
				}
				awaitingSince = 0;
				if (line.contains("\"success\":false") && !rejected) {
					rejected = true;
					Illuminate.LOGGER.warn("Hyperion rejected the lighting: {}", line);
				}
			}
		} catch (IOException ignored) {
			// Closed by drop or by Hyperion
		}
		if (socket == connection) {
			Illuminate.LOGGER.warn("Lost connection to Hyperion");
			drop(connection);
		}
	}

	// Gives the LEDs back to whatever Hyperion showed before
	@Override
	public void release() {
		Socket current = socket;
		if (current != null && !lastImage.isEmpty()) {
			write(current, "{\"command\":\"clear\",\"priority\":" + PRIORITY + "}", System.currentTimeMillis());
		}
		lastImage = "";
	}

	private void drop(Socket current) {
		if (socket != current) {
			return;
		}
		socket = null;
		status = "connecting";
		retryAt = 0;
		closeQuietly(current);
	}

	private static void closeQuietly(Socket connection) {
		try {
			connection.close();
		} catch (IOException ignored) {
			// Already gone
		}
	}

	@Override
	public void close() {
		release();
		Socket current = socket;
		if (current != null) {
			drop(current);
		}
		status = "disabled";
	}
}
