package dev.creeperkatze.illuminate.preview;

import dev.creeperkatze.illuminate.scene.Canvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

// A small drawn desk setup lit by the scene, for showing the mod without a real room
public final class VirtualRoom {
	private static final int WALL = 0xFF0B0B10;
	private static final int DESK = 0xFF1C1C22;
	private static final int FLOOR = 0xFF08080B;
	private static final int BEZEL = 0xFF050507;
	private static final int SCREEN = 0xFF101018;
	private static final int CASE = 0xFF141419;
	private static final int GLOW_LAYERS = 8;

	private VirtualRoom() {
	}

	// colors is the scene as RGB, or null while the lights are released
	public static void extract(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int @Nullable [] colors) {
		graphics.fill(x, y, x + width, y + height, WALL);
		int deskY = y + height * 70 / 100;
		graphics.fill(x, deskY, x + width, y + height, FLOOR);

		if (colors != null) {
			// The whole room picks up a little of the overall light, the ceiling more of the sky above
			graphics.fill(x, y, x + width, deskY, alpha(average(colors), 0x30));
			graphics.fillGradient(x, y, x + width, y + height * 35 / 100, alpha(sample(colors, 0.5F, 0.05F), 0x70), alpha(sample(colors, 0.5F, 0.05F), 0x00));
		}

		int monitorLeft = x + width * 30 / 100;
		int monitorRight = x + width * 70 / 100;
		int monitorTop = y + height * 24 / 100;
		int monitorBottom = y + height * 60 / 100;
		if (colors != null) {
			int spread = width / 14;
			glow(graphics, monitorLeft - spread, monitorTop, monitorLeft, monitorBottom, sample(colors, 0.1F, 0.5F), Side.LEFT);
			glow(graphics, monitorRight, monitorTop, monitorRight + spread, monitorBottom, sample(colors, 0.9F, 0.5F), Side.RIGHT);
			glow(graphics, monitorLeft, monitorTop - spread, monitorRight, monitorTop, sample(colors, 0.5F, 0.1F), Side.TOP);
			glow(graphics, monitorLeft, monitorBottom, monitorRight, monitorBottom + spread / 2, sample(colors, 0.5F, 0.6F), Side.BOTTOM);
		}
		graphics.fill(monitorLeft, monitorTop, monitorRight, monitorBottom, BEZEL);
		graphics.fill(monitorLeft + 2, monitorTop + 2, monitorRight - 2, monitorBottom - 2, SCREEN);
		int standX = (monitorLeft + monitorRight) / 2;
		graphics.fill(standX - 2, monitorBottom, standX + 2, deskY, BEZEL);

		lightBar(graphics, x + width * 9 / 100, y + height * 18 / 100, deskY, colors, 0.08F);
		lightBar(graphics, x + width * 91 / 100, y + height * 18 / 100, deskY, colors, 0.92F);

		graphics.fill(x, deskY, x + width, deskY + Math.max(height / 30, 2), DESK);
		int stripY = deskY + Math.max(height / 30, 2);
		int segments = 24;
		for (int segment = 0; segment < segments; segment++) {
			int left = x + width * segment / segments;
			int right = x + width * (segment + 1) / segments;
			int color = colors == null ? DESK : 0xFF000000 | sample(colors, (segment + 0.5F) / segments, 0.85F);
			graphics.fill(left, stripY, right, stripY + 2, color);
			if (colors != null) {
				graphics.fillGradient(left, stripY + 2, right, stripY + height / 8, alpha(color, 0x50), alpha(color, 0x00));
			}
		}

		pcCase(graphics, x + width * 76 / 100, deskY - height * 34 / 100, x + width * 86 / 100, deskY, colors);
	}

	private static void lightBar(GuiGraphicsExtractor graphics, int centerX, int top, int bottom, int @Nullable [] colors, float u) {
		int barWidth = 3;
		if (colors == null) {
			graphics.fill(centerX - barWidth / 2, top, centerX + barWidth / 2 + 1, bottom, CASE);
			return;
		}
		int upper = sample(colors, u, 0.2F);
		int lower = sample(colors, u, 0.8F);
		for (int layer = GLOW_LAYERS; layer > 0; layer--) {
			int reach = layer * 2;
			int fade = 0x18 * (GLOW_LAYERS - layer + 1) / GLOW_LAYERS;
			graphics.fillGradient(centerX - reach, top, centerX + reach + 1, bottom, alpha(upper, fade), alpha(lower, fade));
		}
		graphics.fillGradient(centerX - barWidth / 2, top, centerX + barWidth / 2 + 1, bottom, 0xFF000000 | upper, 0xFF000000 | lower);
	}

	private static void pcCase(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int @Nullable [] colors) {
		graphics.fill(left, top, right, bottom, CASE);
		int fanSize = Math.max((right - left) * 6 / 10, 4);
		int fanX = (left + right - fanSize) / 2;
		int gap = (bottom - top - 3 * fanSize) / 4;
		for (int fan = 0; fan < 3; fan++) {
			int fanY = top + gap + fan * (fanSize + gap);
			int color = colors == null ? DESK : 0xFF000000 | sample(colors, 0.8F, 0.4F + fan * 0.2F);
			graphics.fill(fanX, fanY, fanX + fanSize, fanY + fanSize, color);
			int ring = Math.max(fanSize / 5, 1);
			graphics.fill(fanX + ring, fanY + ring, fanX + fanSize - ring, fanY + fanSize - ring, CASE);
		}
	}

	// A soft halo spreading away from an edge, built from stacked see-through bands
	private static void glow(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color, Side side) {
		for (int layer = 0; layer < GLOW_LAYERS; layer++) {
			float t = (float) layer / GLOW_LAYERS;
			int fade = (int) (0x60 * (1.0F - t));
			int c = alpha(color, fade);
			switch (side) {
				case LEFT -> graphics.fill(x1 - (int) ((x1 - x0) * (t + 1.0F / GLOW_LAYERS)), y0, x1 - (int) ((x1 - x0) * t), y1, c);
				case RIGHT -> graphics.fill(x0 + (int) ((x1 - x0) * t), y0, x0 + (int) ((x1 - x0) * (t + 1.0F / GLOW_LAYERS)), y1, c);
				case TOP -> graphics.fill(x0, y1 - (int) ((y1 - y0) * (t + 1.0F / GLOW_LAYERS)), x1, y1 - (int) ((y1 - y0) * t), c);
				case BOTTOM -> graphics.fill(x0, y0 + (int) ((y1 - y0) * t), x1, y0 + (int) ((y1 - y0) * (t + 1.0F / GLOW_LAYERS)), c);
			}
		}
	}

	private static int sample(int[] colors, float u, float v) {
		int column = Math.clamp((int) (u * Canvas.WIDTH), 0, Canvas.WIDTH - 1);
		int row = Math.clamp((int) (v * Canvas.HEIGHT), 0, Canvas.HEIGHT - 1);
		return colors[row * Canvas.WIDTH + column] & 0xFFFFFF;
	}

	private static int average(int[] colors) {
		int r = 0;
		int g = 0;
		int b = 0;
		for (int color : colors) {
			r += (color >> 16) & 0xFF;
			g += (color >> 8) & 0xFF;
			b += color & 0xFF;
		}
		return ((r / colors.length) << 16) | ((g / colors.length) << 8) | (b / colors.length);
	}

	private static int alpha(int color, int alpha) {
		return (alpha << 24) | (color & 0xFFFFFF);
	}

	private enum Side {
		LEFT,
		RIGHT,
		TOP,
		BOTTOM
	}
}
