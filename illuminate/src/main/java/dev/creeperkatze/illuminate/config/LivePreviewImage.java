package dev.creeperkatze.illuminate.config;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.engine.LightingEngine;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

// Draws what the lights show, live, in the description of the test effect buttons
final class LivePreviewImage implements ImageRenderer {
	private static final int TEXT = 0xFFFFFFFF;
	private static final int MUTED = 0xFFA0A0A0;

	@Override
	public int render(GuiGraphicsExtractor graphics, int x, int y, int renderWidth, float tickDelta) {
		LightingEngine engine = Illuminate.engine();
		// Keeps test effects playing and the lights undimmed while the preview is on screen
		engine.markPreviewing();
		Font font = Minecraft.getInstance().font;
		int width = renderWidth;
		int height = width * Canvas.HEIGHT / Canvas.WIDTH;

		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF000000);
		int[] colors = engine.preview();
		if (colors == null) {
			graphics.fill(x, y, x + width, y + height, 0xFF101010);
			graphics.centeredText(font, Component.translatable("illuminate.preview.off"), x + width / 2, y + height / 2 - 4, MUTED);
		} else {
			for (int i = 0; i < colors.length; i++) {
				int column = i % Canvas.WIDTH;
				int row = i / Canvas.WIDTH;
				graphics.fill(x + column * width / Canvas.WIDTH, y + row * height / Canvas.HEIGHT,
						x + (column + 1) * width / Canvas.WIDTH, y + (row + 1) * height / Canvas.HEIGHT, 0xFF000000 | colors[i]);
			}
		}
		graphics.centeredText(font, Component.translatable("illuminate.preview.sky"), x + width / 2, y + 3, TEXT);
		graphics.centeredText(font, Component.translatable("illuminate.preview.ground"), x + width / 2, y + height - 11, TEXT);
		graphics.text(font, Component.translatable("illuminate.preview.left"), x + 3, y + height / 2 - 4, TEXT);
		Component right = Component.translatable("illuminate.preview.right");
		graphics.text(font, right, x + width - 3 - font.width(right), y + height / 2 - 4, TEXT);

		int statusY = y + height + 5;
		graphics.text(font, Component.translatable("illuminate.preview.signalrgb", status(engine.signalRgbStatus())), x, statusY, TEXT);
		graphics.text(font, Component.translatable("illuminate.preview.openrgb", status(engine.openRgbStatus())), x, statusY + 11, TEXT);
		return height + 5 + 22;
	}

	private static Component status(String status) {
		return Component.translatable("illuminate.config.status." + status);
	}

	@Override
	public void close() {
	}
}
