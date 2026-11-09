package dev.creeperkatze.rearrange;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

// Sits next to the search box of the world list and is only active while there is an order to reset
public class ResetWorldOrderButton extends Button.Plain {
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
		setScreen(minecraft, new ConfirmScreen(confirmed -> {
			if (confirmed) {
				WorldOrder.reset();
				WorldSelectionList worlds = list.get();
				if (worlds != null) {
					worlds.reloadWorldList();
				}
			}
			setScreen(minecraft, screen);
		}, CONFIRM_TITLE, CONFIRM_MESSAGE));
	}

	private static void setScreen(Minecraft minecraft, Screen screen) {
		//? if >=26.1 {
		minecraft.gui.setScreen(screen);
		//?} else {
		/*minecraft.setScreen(screen);
		*///?}
	}

	public void position(@Nullable EditBox searchBox) {
		if (searchBox != null) {
			this.setPosition(searchBox.getRight() + SPACING, searchBox.getY());
		}
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		this.active = WorldOrder.hasOrder();
		this.extractDefaultSprite(graphics);
		int x = this.getX() + (this.getWidth() - ICON_WIDTH) / 2;
		int y = this.getY() + (this.getHeight() - ICON_HEIGHT) / 2 + 1;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON, x, y, ICON_WIDTH, ICON_HEIGHT, this.active ? -1 : DISABLED_COLOR);
	}
}
