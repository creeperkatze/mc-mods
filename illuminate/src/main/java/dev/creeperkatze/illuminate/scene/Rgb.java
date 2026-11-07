package dev.creeperkatze.illuminate.scene;

import org.joml.Vector3fc;

public record Rgb(float r, float g, float b) {
	public static final Rgb BLACK = new Rgb(0.0F, 0.0F, 0.0F);
	public static final Rgb WHITE = new Rgb(1.0F, 1.0F, 1.0F);

	public static Rgb of(int rgb) {
		return new Rgb(((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F);
	}

	public static Rgb of(Vector3fc vector) {
		return new Rgb(vector.x(), vector.y(), vector.z());
	}

	public Rgb lerp(Rgb to, float t) {
		return new Rgb(r + (to.r - r) * t, g + (to.g - g) * t, b + (to.b - b) * t);
	}

	public Rgb scale(float factor) {
		return new Rgb(r * factor, g * factor, b * factor);
	}

	public Rgb multiply(Rgb other) {
		return new Rgb(r * other.r, g * other.g, b * other.b);
	}

	public float max() {
		return Math.max(r, Math.max(g, b));
	}

	public float luminance() {
		return 0.2126F * r + 0.7152F * g + 0.0722F * b;
	}

	public Rgb saturate(float amount) {
		float grey = luminance();
		return new Rgb(grey + (r - grey) * amount, grey + (g - grey) * amount, grey + (b - grey) * amount);
	}

	public Rgb rotateHue(float degrees) {
		double angle = Math.toRadians(degrees);
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		float i = 0.596F * r - 0.274F * g - 0.322F * b;
		float q = 0.211F * r - 0.523F * g + 0.312F * b;
		float y = 0.299F * r + 0.587F * g + 0.114F * b;
		float i2 = i * cos - q * sin;
		float q2 = i * sin + q * cos;
		return new Rgb(y + 0.956F * i2 + 0.621F * q2, y - 0.272F * i2 - 0.647F * q2, y - 1.106F * i2 + 1.703F * q2);
	}

	// Raises the brightest channel to at least the given level without changing the hue
	public Rgb atLeast(float level) {
		float max = max();
		if (max >= level) {
			return this;
		}
		return max <= 0.0001F ? new Rgb(level, level, level) : scale(level / max);
	}

	public int toRgb24() {
		return (channel(r) << 16) | (channel(g) << 8) | channel(b);
	}

	private static int channel(float value) {
		return Math.round(Math.clamp(value, 0.0F, 1.0F) * 255.0F);
	}
}
