package dev.creeperkatze.illuminate.scene;

// A small grid of colours that stands for the room. u runs from your left to your right, v from the sky down to the ground.
public final class Canvas {
	public static final int WIDTH = 16;
	public static final int HEIGHT = 9;
	public static final int SIZE = WIDTH * HEIGHT;

	private final float[] red = new float[SIZE];
	private final float[] green = new float[SIZE];
	private final float[] blue = new float[SIZE];

	public static float u(int index) {
		return (index % WIDTH + 0.5F) / WIDTH;
	}

	public static float v(int index) {
		return (index / WIDTH + 0.5F) / HEIGHT;
	}

	public Rgb get(int index) {
		return new Rgb(red[index], green[index], blue[index]);
	}

	public void set(int index, Rgb color) {
		red[index] = color.r();
		green[index] = color.g();
		blue[index] = color.b();
	}

	public void fill(Rgb color) {
		for (int i = 0; i < SIZE; i++) {
			set(i, color);
		}
	}

	public void mix(int index, Rgb color, float alpha) {
		if (alpha <= 0.0F) {
			return;
		}
		float a = Math.min(alpha, 1.0F);
		red[index] += (color.r() - red[index]) * a;
		green[index] += (color.g() - green[index]) * a;
		blue[index] += (color.b() - blue[index]) * a;
	}

	public void add(int index, Rgb color, float amount) {
		if (amount <= 0.0F) {
			return;
		}
		red[index] += color.r() * amount;
		green[index] += color.g() * amount;
		blue[index] += color.b() * amount;
	}

	public void multiply(int index, float factor) {
		red[index] *= factor;
		green[index] *= factor;
		blue[index] *= factor;
	}

	public void copyFrom(Canvas other) {
		System.arraycopy(other.red, 0, red, 0, SIZE);
		System.arraycopy(other.green, 0, green, 0, SIZE);
		System.arraycopy(other.blue, 0, blue, 0, SIZE);
	}

	// Moves every cell part of the way towards the target, which smooths changes over time
	public void approach(Canvas target, float amount) {
		for (int i = 0; i < SIZE; i++) {
			red[i] += (target.red[i] - red[i]) * amount;
			green[i] += (target.green[i] - green[i]) * amount;
			blue[i] += (target.blue[i] - blue[i]) * amount;
		}
	}

	public Rgb sample(float u, float v) {
		float x = Math.clamp(u * WIDTH - 0.5F, 0.0F, WIDTH - 1.0F);
		float y = Math.clamp(v * HEIGHT - 0.5F, 0.0F, HEIGHT - 1.0F);
		int x0 = (int) x;
		int y0 = (int) y;
		int x1 = Math.min(x0 + 1, WIDTH - 1);
		int y1 = Math.min(y0 + 1, HEIGHT - 1);
		float fx = x - x0;
		float fy = y - y0;
		Rgb top = get(y0 * WIDTH + x0).lerp(get(y0 * WIDTH + x1), fx);
		Rgb bottom = get(y1 * WIDTH + x0).lerp(get(y1 * WIDTH + x1), fx);
		return top.lerp(bottom, fy);
	}

	public Rgb average() {
		float r = 0.0F;
		float g = 0.0F;
		float b = 0.0F;
		for (int i = 0; i < SIZE; i++) {
			r += red[i];
			g += green[i];
			b += blue[i];
		}
		return new Rgb(r / SIZE, g / SIZE, b / SIZE);
	}

	public int[] toRgb24() {
		int[] colors = new int[SIZE];
		for (int i = 0; i < SIZE; i++) {
			colors[i] = get(i).toRgb24();
		}
		return colors;
	}
}
