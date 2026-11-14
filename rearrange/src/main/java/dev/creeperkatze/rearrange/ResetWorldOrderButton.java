package dev.creeperkatze.rearrange;

//? if <1.21.9 {
/*import dev.creeperkatze.rearrange.mixin.WorldSelectionListAccessor;
import net.minecraft.client.gui.Font;
*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

// Sits next to the search box of the world list and is only active while there is an order to reset
//? if >=1.21.9 {
public class ResetWorldOrderButton extends Button.Plain {
//?} else {
/*public class ResetWorldOrderButton extends Button {
*///?}
	private static final int SIZE = 20;
	private static final int SPACING = 4;
	private static final Identifier ICON = Identifier.fromNamespaceAndPath(Rearrange.MOD_ID, "icon/reset");
	private static final int ICON_WIDTH = 13;
	private static final int ICON_HEIGHT = 15;
	private static final int DISABLED_COLOR = 0xFFA0A0A0;
	private static final Component MESSAGE = Component.translatable("rearrange.reset_world_order");
	private static final Component CONFIRM_TITLE = Component.translatable("rearrange.reset_world_order.title");
	private static final Component CONFIRM_MESSAGE = Component.translatable("rearrange.reset_world_order.message");

	public ResetWorldOrderButton(Screen screen, Supplier<@Nullable WorldSelectionList> list) {
		super(0, 0, SIZE, SIZE, MESSAGE, button -> confirm(screen, list), DEFAULT_NARRATION);
		this.setTooltip(Tooltip.create(MESSAGE));
	}

	private static void confirm(Screen screen, Supplier<@Nullable WorldSelectionList> list) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.gui.setScreen(new ConfirmScreen(confirmed -> {
			if (confirmed) {
				WorldOrder.reset();
				WorldSelectionList worlds = list.get();
				if (worlds != null) {
					//? if >=1.21.9 {
					worlds.reloadWorldList();
					//?} else {
					/*((WorldSelectionListAccessor) worlds).rearrange$reloadWorldList();
					*///?}
				}
			}
			minecraft.gui.setScreen(screen);
		}, CONFIRM_TITLE, CONFIRM_MESSAGE));
	}

	public void position(@Nullable EditBox searchBox) {
		if (searchBox != null) {
			this.setPosition(searchBox.getRight() + SPACING, searchBox.getY());
		}
	}

	//? if >=1.21.9 {
	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		this.active = WorldOrder.hasOrder();
		this.extractDefaultSprite(graphics);
		this.extractIcon(graphics);
	}
	//?} else {
	/*@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		this.active = WorldOrder.hasOrder();
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
		this.extractIcon(graphics);
	}

	// The icon replaces the text
	@Override
	public void renderString(GuiGraphicsExtractor graphics, Font font, int color) {
	}
	*///?}

	private void extractIcon(GuiGraphicsExtractor graphics) {
		int x = this.getX() + (this.getWidth() - ICON_WIDTH) / 2;
		int y = this.getY() + (this.getHeight() - ICON_HEIGHT) / 2 + 1;
		//? if >=1.21.6 {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON, x, y, ICON_WIDTH, ICON_HEIGHT, this.active ? -1 : DISABLED_COLOR);
		//?} else {
		/*float shade = this.active ? 1.0F : (DISABLED_COLOR & 0xFF) / 255.0F;
		graphics.setColor(shade, shade, shade, 1.0F);
		graphics.blitSprite(ICON, x, y, ICON_WIDTH, ICON_HEIGHT);
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		*///?}
	}
}
