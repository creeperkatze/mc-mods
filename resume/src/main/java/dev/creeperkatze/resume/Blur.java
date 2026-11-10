package dev.creeperkatze.resume;

import com.mojang.blaze3d.platform.NativeImage;

// Three box blur passes, which look close to a gaussian blur
public final class Blur {
	private static final int PASSES = 3;

	private Blur() {
	}

	public static void apply(NativeImage image, int radius) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = image.getPixels();
		int[] buffer = new int[pixels.length];
		for (int pass = 0; pass < PASSES; pass++) {
			blur(pixels, buffer, width, height, 1, width, radius);
			blur(buffer, pixels, height, width, width, 1, radius);
		}
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.setPixel(x, y, pixels[y * width + x]);
			}
		}
	}

	// Blurs along lines of the given length, with a step between pixels and a stride between lines
	private static void blur(int[] in, int[] out, int lines, int length, int lineStride, int step, int radius) {
		float size = radius * 2 + 1;
		for (int line = 0; line < lines; line++) {
			int start = line * lineStride;
			int red = 0;
			int green = 0;
			int blue = 0;
			for (int i = -radius; i <= radius; i++) {
				int pixel = in[start + Math.clamp(i, 0, length - 1) * step];
				red += pixel >> 16 & 0xFF;
				green += pixel >> 8 & 0xFF;
				blue += pixel & 0xFF;
			}
			for (int i = 0; i < length; i++) {
				out[start + i * step] = 0xFF000000 | Math.round(red / size) << 16 | Math.round(green / size) << 8
						| Math.round(blue / size);
				int added = in[start + Math.min(i + radius + 1, length - 1) * step];
				int removed = in[start + Math.max(i - radius, 0) * step];
				red += (added >> 16 & 0xFF) - (removed >> 16 & 0xFF);
				green += (added >> 8 & 0xFF) - (removed >> 8 & 0xFF);
				blue += (added & 0xFF) - (removed & 0xFF);
			}
		}
	}
}
