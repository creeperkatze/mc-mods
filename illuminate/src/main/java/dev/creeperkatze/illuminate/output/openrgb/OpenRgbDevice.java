package dev.creeperkatze.illuminate.output.openrgb;

import dev.creeperkatze.illuminate.config.Placement;
import dev.creeperkatze.illuminate.scene.Canvas;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// One OpenRGB controller with what is needed to drive it and to restore it afterwards
final class OpenRgbDevice {
	private static final int ZONE_MATRIX = 2;
	private static final int NO_LED = -1;

	final int index;
	final String name;
	final int ledCount;
	final int originalMode;
	final byte[] originalModeData;
	final boolean originalModeDirect;
	final int[] originalColors;
	private final List<Zone> zones;

	private Placement layoutPlacement;
	private float[] layout = new float[0];
	int[] lastSent;
	long lastSentAt;

	private OpenRgbDevice(int index, String name, int originalMode, byte[] originalModeData, boolean originalModeDirect, int[] originalColors, List<Zone> zones, int ledCount) {
		this.index = index;
		this.name = name;
		this.originalMode = originalMode;
		this.originalModeData = originalModeData;
		this.originalModeDirect = originalModeDirect;
		this.originalColors = originalColors;
		this.zones = zones;
		this.ledCount = ledCount;
	}

	static OpenRgbDevice parse(int index, ByteBuffer data, int protocol) {
		data.getInt();
		data.getInt();
		String name = string(data);
		if (protocol >= 1) {
			string(data);
		}
		for (int i = 0; i < 4; i++) {
			string(data);
		}

		int modeCount = unsignedShort(data);
		int activeMode = data.getInt();
		byte[] activeModeData = new byte[0];
		boolean activeModeDirect = false;
		for (int mode = 0; mode < modeCount; mode++) {
			int start = data.position();
			String modeName = string(data);
			// Value, flags, speed min and max
			skip(data, 16);
			if (protocol >= 3) {
				skip(data, 8);
			}
			// Colours min and max, speed
			skip(data, 12);
			if (protocol >= 3) {
				skip(data, 4);
			}
			// Direction and colour mode
			skip(data, 8);
			skip(data, unsignedShort(data) * 4);
			if (mode == activeMode) {
				activeModeData = Arrays.copyOfRange(data.array(), data.arrayOffset() + start, data.arrayOffset() + data.position());
				activeModeDirect = modeName.equalsIgnoreCase("direct");
			}
		}

		int zoneCount = unsignedShort(data);
		List<Zone> zones = new ArrayList<>(zoneCount);
		int ledStart = 0;
		for (int zone = 0; zone < zoneCount; zone++) {
			string(data);
			int type = data.getInt();
			skip(data, 8);
			int leds = data.getInt();
			int matrixLength = unsignedShort(data);
			int width = 0;
			int height = 0;
			int[] matrix = new int[0];
			if (matrixLength > 0) {
				height = data.getInt();
				width = data.getInt();
				if (width < 0 || height < 0 || (long) width * height > data.remaining() / 4) {
					throw new IllegalArgumentException("Invalid OpenRGB matrix size " + width + "x" + height);
				}
				matrix = new int[width * height];
				for (int i = 0; i < matrix.length; i++) {
					matrix[i] = data.getInt();
				}
			}
			if (protocol >= 4) {
				int segments = unsignedShort(data);
				for (int segment = 0; segment < segments; segment++) {
					string(data);
					skip(data, 12);
				}
			}
			zones.add(new Zone(ledStart, leds, type == ZONE_MATRIX ? width : 0, type == ZONE_MATRIX ? height : 0, matrix));
			ledStart += leds;
		}

		int ledCount = unsignedShort(data);
		for (int led = 0; led < ledCount; led++) {
			string(data);
			data.getInt();
		}
		int colorCount = unsignedShort(data);
		int[] colors = new int[colorCount];
		for (int i = 0; i < colorCount; i++) {
			int raw = data.getInt();
			colors[i] = ((raw & 0xFF) << 16) | (raw & 0xFF00) | ((raw >> 16) & 0xFF);
		}
		return new OpenRgbDevice(index, name, activeMode, activeModeData, activeModeDirect, colors, List.copyOf(zones), ledCount);
	}

	// The colour of every LED for the given scene and placement
	int[] colors(Canvas canvas, Placement placement) {
		int[] colors = new int[ledCount];
		if (placement == Placement.WHOLE_SCENE) {
			Arrays.fill(colors, canvas.average().toRgb24());
			return colors;
		}
		if (placement != layoutPlacement) {
			layout = layout(placement);
			layoutPlacement = placement;
		}
		for (int led = 0; led < ledCount; led++) {
			colors[led] = canvas.sample(layout[led * 2], layout[led * 2 + 1]).toRgb24();
		}
		return colors;
	}

	// Where each LED sits on the canvas. Strips run along the placement, matrices keep their shape inside it.
	private float[] layout(Placement placement) {
		float[] positions = new float[ledCount * 2];
		for (int led = 0; led < ledCount; led++) {
			float along = (led + 0.5F) / ledCount;
			setLinear(positions, led, placement, along);
		}
		for (Zone zone : zones) {
			if (zone.width == 0 || zone.height == 0) {
				continue;
			}
			for (int cell = 0; cell < zone.matrix.length; cell++) {
				int led = zone.matrix[cell];
				if (led == NO_LED || led < 0 || zone.start + led >= ledCount) {
					continue;
				}
				float x = (cell % zone.width + 0.5F) / zone.width;
				float y = (cell / zone.width + 0.5F) / zone.height;
				setMatrix(positions, zone.start + led, placement, x, y);
			}
		}
		return positions;
	}

	private static void setLinear(float[] positions, int led, Placement placement, float along) {
		switch (placement) {
			case LEFT -> set(positions, led, 0.08F, along);
			case RIGHT -> set(positions, led, 0.92F, along);
			case ABOVE -> set(positions, led, along, 0.08F);
			case BELOW -> set(positions, led, along, 0.92F);
			case SURROUND -> set(positions, led, along, 0.5F);
			default -> set(positions, led, 0.5F, along);
		}
	}

	private static void setMatrix(float[] positions, int led, Placement placement, float x, float y) {
		switch (placement) {
			case LEFT -> set(positions, led, x * 0.2F, y);
			case RIGHT -> set(positions, led, 0.8F + x * 0.2F, y);
			case ABOVE -> set(positions, led, x, y * 0.25F);
			case BELOW -> set(positions, led, x, 0.75F + y * 0.25F);
			case SURROUND -> set(positions, led, x, y);
			default -> set(positions, led, 0.4F + x * 0.2F, y);
		}
	}

	private static void set(float[] positions, int led, float u, float v) {
		positions[led * 2] = u;
		positions[led * 2 + 1] = v;
	}

	private static String string(ByteBuffer data) {
		int length = unsignedShort(data);
		byte[] bytes = new byte[length];
		data.get(bytes);
		int end = length;
		while (end > 0 && bytes[end - 1] == 0) {
			end--;
		}
		return new String(bytes, 0, end, StandardCharsets.UTF_8);
	}

	private static int unsignedShort(ByteBuffer data) {
		return data.getShort() & 0xFFFF;
	}

	private static void skip(ByteBuffer data, int bytes) {
		data.position(data.position() + bytes);
	}

	private record Zone(int start, int leds, int width, int height, int[] matrix) {
	}
}
