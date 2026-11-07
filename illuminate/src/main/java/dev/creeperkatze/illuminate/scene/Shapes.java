package dev.creeperkatze.illuminate.scene;

// Small helpers for the shapes and noise that layers paint with
public final class Shapes {
	private Shapes() {
	}

	public static float smoothstep(float edge0, float edge1, float x) {
		float t = Math.clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
		return t * t * (3.0F - 2.0F * t);
	}

	public static float gaussian(float x, float center, float width) {
		float d = x - center;
		return (float) Math.exp(-(d * d) / width);
	}

	// 0 in the middle of the room, 1 at your far left and far right
	public static float sides(float u) {
		return Math.abs(u - 0.5F) * 2.0F;
	}

	// Weight of a cell for something that happened at position u, or an even glow towards the sides when it has no direction
	public static float towards(float cellU, float u, float spread) {
		return Float.isNaN(u) ? 0.55F + 0.45F * sides(cellU) : 0.25F + 0.75F * gaussian(cellU, u, spread);
	}

	public static float hash(int x, int y) {
		int h = x * 374761393 + y * 668265263;
		h = (h ^ (h >>> 13)) * 1274126177;
		return ((h ^ (h >>> 16)) & 0xFFFFFF) / (float) 0xFFFFFF;
	}

	// Smooth value noise between 0 and 1
	public static float noise(float x, float y) {
		int x0 = (int) Math.floor(x);
		int y0 = (int) Math.floor(y);
		float fx = x - x0;
		float fy = y - y0;
		fx = fx * fx * (3.0F - 2.0F * fx);
		fy = fy * fy * (3.0F - 2.0F * fy);
		float top = hash(x0, y0) + (hash(x0 + 1, y0) - hash(x0, y0)) * fx;
		float bottom = hash(x0, y0 + 1) + (hash(x0 + 1, y0 + 1) - hash(x0, y0 + 1)) * fx;
		return top + (bottom - top) * fy;
	}

	// Moves value towards target at a rate per second without overshooting
	public static float approach(float value, float target, float rate, float delta) {
		return value + (target - value) * (1.0F - (float) Math.exp(-rate * delta));
	}
}
