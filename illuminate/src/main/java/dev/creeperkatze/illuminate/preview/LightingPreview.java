package dev.creeperkatze.illuminate.preview;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.scene.Canvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;

// Draws what the lights show in the corner of the screen, for tuning without hardware
public final class LightingPreview {
	private static final int CELL = 5;
	private static final int MARGIN = 4;
	private static final int BORDER = 0xFF000000;

	private LightingPreview() {
	}

	public static void extract(GuiGraphicsExtractor graphics) {
		int[] colors = Illuminate.engine().preview();
		if (!Illuminate.config().showPreview || colors == null) {
			return;
		}
		int width = Canvas.WIDTH * CELL;
		int height = Canvas.HEIGHT * CELL;
		graphics.fill(MARGIN - 1, MARGIN - 1, MARGIN + width + 1, MARGIN + height + 1, BORDER);
		for (int i = 0; i < colors.length; i++) {
			int x = MARGIN + i % Canvas.WIDTH * CELL;
			int y = MARGIN + i / Canvas.WIDTH * CELL;
			graphics.fill(x, y, x + CELL, y + CELL, 0xFF000000 | colors[i]);
		}
	}
}
