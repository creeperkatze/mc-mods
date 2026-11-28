package dev.creeperkatze.exchange;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

// Laid out like vanilla's Optimize World screen
final class CopyScreen extends Screen {
	private static final int TEXT_COLOR = 0xFFA0A0A0;
	private static final int BAR_BACKGROUND = 0xFF000000;
	private static final int BAR_COLOR = 0xFF2BBAB4;
	private static final int BAR_HALF_WIDTH = 150;

	private final LodCopy job;
	private final Consumer<LodCopy> onFinished;
	private boolean finished;

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
		}).bounds(this.width / 2 - 100, this.height / 4 + 150, 200, 20).build();
		stop.active = !this.job.isStopped();
		this.addRenderableWidget(stop);
	}

	@Override
	public void tick() {
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
		graphics.centeredText(this.font, this.title, this.width / 2, 20, -1);
		int left = this.width / 2 - BAR_HALF_WIDTH;
		int right = this.width / 2 + BAR_HALF_WIDTH;
		int top = this.height / 4 + 100;
		int bottom = top + 10;
		graphics.centeredText(this.font, this.job.status(), this.width / 2, top - this.font.lineHeight - 2, TEXT_COLOR);
		long total = this.job.total();
		if (total <= 0) {
			return;
		}
		long done = Math.min(this.job.done(), total);
		float progress = (float) done / total;
		graphics.fill(left - 1, top - 1, right + 1, bottom + 1, BAR_BACKGROUND);
		graphics.fill(left, top, left + Mth.floor(progress * (right - left)), bottom, BAR_COLOR);
		Component percentage = Component.translatable("optimizeWorld.progress.percentage", Mth.floor(progress * 100));
		graphics.centeredText(this.font, percentage, this.width / 2, top + (bottom - top) / 2 - this.font.lineHeight / 2, TEXT_COLOR);
		Component counter = Component.translatable("optimizeWorld.progress.counter", done, total);
		graphics.centeredText(this.font, counter, this.width / 2, bottom + this.font.lineHeight, TEXT_COLOR);
	}
}
