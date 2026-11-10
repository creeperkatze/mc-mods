package dev.creeperkatze.illuminate.output.openrgb;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.config.Placement;
import dev.creeperkatze.illuminate.output.LightOutput;
import dev.creeperkatze.illuminate.output.RateCounter;
import dev.creeperkatze.illuminate.scene.Canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

// Drives every device of an OpenRGB SDK server in direct mode
public final class OpenRgbOutput implements LightOutput {
	private static final long RETRY_MS = 5000;
	private static final long RESEND_MS = 2000;
	private static final int MAX_DEVICES = 256;

	private final String host;
	private final int port;
	private final AtomicBoolean connecting = new AtomicBoolean();
	private volatile OpenRgbClient client;
	private volatile List<OpenRgbDevice> devices = List.of();
	private volatile String status = "connecting";
	private long retryAt;
	private boolean acquired;
	private boolean warned;
	private final RateCounter writes = new RateCounter();

	public OpenRgbOutput(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public boolean matches(String host, int port) {
		return this.host.equals(host) && this.port == port;
	}

	public int ledCount() {
		return devices.stream().mapToInt(device -> device.ledCount).sum();
	}

	public float writesPerSecond() {
		return writes.perSecond();
	}

	public List<String> deviceNames() {
		return devices.stream().map(device -> device.name).distinct().toList();
	}

	@Override
	public String status() {
		return status;
	}

	public void send(Canvas canvas, long now, Map<String, Placement> placements) {
		writes.update(now);
		OpenRgbClient current = client;
		if (current == null) {
			connectLater(now);
			return;
		}
		try {
			if (current.deviceListChanged()) {
				Illuminate.LOGGER.info("OpenRGB devices changed, reconnecting");
				drop();
				return;
			}
			for (OpenRgbDevice device : devices) {
				Placement placement = placements.getOrDefault(device.name, Placement.CENTER);
				if (placement == Placement.OFF || device.ledCount == 0) {
					continue;
				}
				if (!acquired) {
					current.setCustomMode(device.index);
				}
				int[] colors = device.colors(canvas, placement);
				if (!Arrays.equals(colors, device.lastSent) || now - device.lastSentAt > RESEND_MS) {
					current.updateLeds(device.index, colors);
					writes.count();
					device.lastSent = colors;
					device.lastSentAt = now;
				}
			}
			acquired = true;
		} catch (IOException e) {
			Illuminate.LOGGER.warn("Lost connection to OpenRGB: {}", e.getMessage());
			drop();
		}
	}

	private void connectLater(long now) {
		if (now < retryAt || !connecting.compareAndSet(false, true)) {
			return;
		}
		retryAt = now + RETRY_MS;
		Thread thread = new Thread(this::connect, "Illuminate OpenRGB");
		thread.setDaemon(true);
		thread.start();
	}

	private void connect() {
		OpenRgbClient connection = null;
		try {
			connection = OpenRgbClient.connect(host, port, "Illuminate");
			int count = connection.controllerCount();
			if (count < 0 || count > MAX_DEVICES) {
				throw new IOException("Unexpected OpenRGB device count " + count);
			}
			List<OpenRgbDevice> found = new ArrayList<>(count);
			for (int index = 0; index < count; index++) {
				found.add(OpenRgbDevice.parse(index, connection.controllerData(index), connection.protocol()));
			}
			devices = List.copyOf(found);
			acquired = false;
			client = connection;
			status = "connected";
			warned = false;
			Illuminate.LOGGER.info("Connected to OpenRGB at {}:{} with {} devices (protocol {})", host, port, count, connection.protocol());
		} catch (IOException | RuntimeException e) {
			status = "unavailable";
			if (connection != null) {
				try {
					connection.close();
				} catch (IOException ignored) {
				}
			}
			if (!warned) {
				Illuminate.LOGGER.info("OpenRGB is not reachable at {}:{}: {}", host, port, e.getMessage());
				warned = true;
			}
		} finally {
			connecting.set(false);
		}
	}

	// Gives every device its own mode and colours back
	@Override
	public void release() {
		OpenRgbClient current = client;
		if (current == null || !acquired) {
			return;
		}
		try {
			for (OpenRgbDevice device : devices) {
				if (device.originalModeData.length > 0) {
					current.updateMode(device.index, device.originalMode, device.originalModeData);
				}
				if (device.originalModeDirect && device.originalColors.length == device.ledCount && device.ledCount > 0) {
					current.updateLeds(device.index, device.originalColors);
				}
				device.lastSent = null;
			}
		} catch (IOException e) {
			Illuminate.LOGGER.warn("Could not restore OpenRGB devices: {}", e.getMessage());
		}
		acquired = false;
	}

	private void drop() {
		OpenRgbClient current = client;
		client = null;
		devices = List.of();
		status = "connecting";
		retryAt = 0;
		if (current != null) {
			try {
				current.close();
			} catch (IOException ignored) {
				// Already gone
			}
		}
	}

	@Override
	public void close() {
		release();
		drop();
		status = "disabled";
	}
}
