package dev.creeperkatze.illuminate.config;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.engine.LightingEngine;
import dev.creeperkatze.illuminate.game.GameEvent;
import dev.creeperkatze.illuminate.game.GameEvents;
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Rgb;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public final class LightingScreen extends Screen {
	private static final int MARGIN = 16;
	private static final int TOP = 34;
	private static final int TEXT = 0xFFFFFFFF;
	private static final int MUTED = 0xFFA0A0A0;

	private final Screen parent;
	private int mapX;
	private int mapY;
	private int mapWidth;
	private int mapHeight;

	public LightingScreen(Screen parent) {
		super(Component.translatable("illuminate.preview.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int availableWidth = width - 2 * MARGIN;
		int availableHeight = height - 60 - TOP - 24;
		mapWidth = Math.max(Canvas.WIDTH * 4, Math.min(availableWidth, availableHeight * Canvas.WIDTH / Canvas.HEIGHT));
		mapHeight = mapWidth * Canvas.HEIGHT / Canvas.WIDTH;
		mapX = (width - mapWidth) / 2;
		mapY = TOP;

		List<Test> tests = List.of(
				new Test("damage_left", () -> new GameEvent.Damage("generic", 0.1F)),
				new Test("damage_right", () -> new GameEvent.Damage("generic", 0.9F)),
				new Test("explosion", () -> new GameEvent.Explosion(1.0F, 0.5F)),
				new Test("lightning", () -> new GameEvent.Lightning(1.0F, 0.5F)),
				new Test("firework",
						() -> new GameEvent.Firework(List.of(Rgb.of(0xFF3355), Rgb.of(0x33AAFF), Rgb.of(0xFFDD33)),
								List.of(Rgb.of(0xFFFFFF)), 1.0F, 0.5F, 0.3F)),
				new Test("totem", GameEvent.Totem::new),
				new Test("level_up", () -> new GameEvent.LevelUp(5)));
		int gap = 4;
		int testWidth = Math.min(80, (width - 2 * MARGIN - (tests.size() - 1) * gap) / tests.size());
		int x = (width - tests.size() * testWidth - (tests.size() - 1) * gap) / 2;
		for (Test test : tests) {
			addRenderableWidget(Button
					.builder(Component.translatable("illuminate.preview.test." + test.key),
							button -> GameEvents.post(test.event.get()))
					.bounds(x, height - 52, testWidth, 20)
					.build());
			x += testWidth + gap;
		}
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
				.bounds(width / 2 - 100, height - 27, 200, 20).build());
	}

	@Override
	public void added() {
		Illuminate.engine().setPreviewing(true);
	}

	@Override
	public void removed() {
		Illuminate.engine().setPreviewing(false);
	}

	@Override
	public void onClose() {
		minecraft.gui.setScreen(parent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(font, title, width / 2, 12, TEXT);
		extractMap(graphics);
		LightingEngine engine = Illuminate.engine();
		int y = mapY + mapHeight + 6;
		graphics.text(font, Component.translatable("illuminate.preview.signalrgb", status(engine.signalRgbStatus())),
				mapX, y, TEXT);
		graphics.text(font, Component.translatable("illuminate.preview.openrgb", status(engine.openRgbStatus())), mapX,
				y + 11, TEXT);
	}

	private void extractMap(GuiGraphicsExtractor graphics) {
		graphics.fill(mapX - 1, mapY - 1, mapX + mapWidth + 1, mapY + mapHeight + 1, 0xFF000000);
		int[] colors = Illuminate.engine().preview();
		if (colors == null) {
			graphics.fill(mapX, mapY, mapX + mapWidth, mapY + mapHeight, 0xFF101010);
			graphics.centeredText(font, Component.translatable("illuminate.preview.off"), mapX + mapWidth / 2,
					mapY + mapHeight / 2 - 4, MUTED);
		} else {
			for (int i = 0; i < colors.length; i++) {
				int column = i % Canvas.WIDTH;
				int row = i / Canvas.WIDTH;
				graphics.fill(mapX + column * mapWidth / Canvas.WIDTH, mapY + row * mapHeight / Canvas.HEIGHT,
						mapX + (column + 1) * mapWidth / Canvas.WIDTH, mapY + (row + 1) * mapHeight / Canvas.HEIGHT,
						0xFF000000 | colors[i]);
			}
		}
		graphics.centeredText(font, Component.translatable("illuminate.preview.sky"), mapX + mapWidth / 2, mapY + 3,
				TEXT);
		graphics.centeredText(font, Component.translatable("illuminate.preview.ground"), mapX + mapWidth / 2,
				mapY + mapHeight - 11, TEXT);
		graphics.text(font, Component.translatable("illuminate.preview.left"), mapX + 3, mapY + mapHeight / 2 - 4,
				TEXT);
		Component right = Component.translatable("illuminate.preview.right");
		graphics.text(font, right, mapX + mapWidth - 3 - font.width(right), mapY + mapHeight / 2 - 4, TEXT);
	}

	private static Component status(String status) {
		return Component.translatable("illuminate.config.status." + status);
	}

	private record Test(String key, Supplier<GameEvent> event) {
	}
}
