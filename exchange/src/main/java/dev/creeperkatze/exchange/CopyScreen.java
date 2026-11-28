package dev.creeperkatze.exchange;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

// Laid out like vanilla's world loading screen, with the areas to copy in place of its chunk map
final class CopyScreen extends Screen {
	private static final int BAR_BACKGROUND = -16777216;
	private static final int BAR_COLOR = -16711936;
	private static final int TEXT_COLOR = -6250336;
	private static final int GRID_BACKGROUND = -16777216;
	private static final int CELL_SIZE = 2;
	// Always laid out for the biggest grid, so nothing moves when a grid shows up or changes size
	private static final int GRID_SIZE = ProgressGrid.MAX_CELLS * CELL_SIZE;

	private final LodCopy job;
	private final Consumer<LodCopy> onFinished;
	private boolean finished;
	private float smoothedProgress;

	CopyScreen(LodCopy job, Consumer<LodCopy> onFinished) {
		super(Component.translatable("exchange.progress.title", job.target().displayName()));
		this.job = job;
		this.onFinished = onFinished;
	}

	@Override
	protected void init() {
		Button stop = Button.builder(Component.translatable("exchange.progress.stop"), button -> {
			this.job.stop();
			button.active = false;
		}).bounds((this.width - 200) / 2, this.height / 2 + GRID_SIZE / 2 + 26, 200, 20).build();
		stop.active = !this.job.isStopped();
		this.addRenderableWidget(stop);
	}

	@Override
	public void tick() {
		long total = this.job.total();
		float progress = total > 0 ? (float) Math.min(this.job.done(), total) / total : 0;
		this.smoothedProgress += (progress - this.smoothedProgress) * 0.2F;
		if (!this.finished && this.job.state() != LodCopy.State.RUNNING) {
			this.finished = true;
			// Swapping the screen during its own tick breaks Fabric's screen events
			this.minecraft.schedule(() -> this.onFinished.accept(this.job));
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		ProgressGrid grid = this.job.grid();
		if (grid != null) {
			extractGrid(graphics, xCenter, yCenter, grid);
		}
		int textTop = yCenter - GRID_SIZE / 2 - this.font.lineHeight * 3;
		graphics.centeredText(this.font, this.title, xCenter, textTop, -1);
		int barTop = textTop + this.font.lineHeight + 3;
		graphics.fill(xCenter - 100, barTop, xCenter + 100, barTop + 2, BAR_BACKGROUND);
		graphics.fill(xCenter - 100, barTop, xCenter - 100 + Math.round(this.smoothedProgress * 200), barTop + 2, BAR_COLOR);
		LodWorld.Dimension dimension = this.job.dimension();
		long total = this.job.total();
		if (dimension != null && total > 0) {
			int percentage = (int) (Math.min(this.job.done(), total) * 100 / total);
			Component label = Component.translatable("exchange.progress.dimension", dimension.level().identifier().toString(), percentage);
			graphics.centeredText(this.font, label, xCenter, yCenter + GRID_SIZE / 2 + 8, TEXT_COLOR);
		}
	}

	private static void extractGrid(GuiGraphicsExtractor graphics, int xCenter, int yCenter, ProgressGrid grid) {
		int xStart = xCenter - grid.width() * CELL_SIZE / 2;
		int yStart = yCenter - grid.height() * CELL_SIZE / 2;
		graphics.fill(xStart, yStart, xStart + grid.width() * CELL_SIZE, yStart + grid.height() * CELL_SIZE, GRID_BACKGROUND);
		for (int x = 0; x < grid.width(); x++) {
			for (int z = 0; z < grid.height(); z++) {
				if (grid.hasLods(x, z)) {
					int xCell = xStart + x * CELL_SIZE;
					int yCell = yStart + z * CELL_SIZE;
					graphics.fill(xCell, yCell, xCell + CELL_SIZE, yCell + CELL_SIZE, color(grid.stage(x, z)));
				}
			}
		}
	}

	// Starts at the gray vanilla gives chunks that haven't started and brightens up to the mod color
	private static int color(ProgressGrid.Stage stage) {
		return switch (stage) {
			case WAITING -> 0xFF545454;
			case QUEUED -> 0xFF1F4645;
			case READING -> 0xFF1F6764;
			case WRITING -> 0xFF238E89;
			case DONE -> 0xFF2BBAB4;
		};
	}
}
