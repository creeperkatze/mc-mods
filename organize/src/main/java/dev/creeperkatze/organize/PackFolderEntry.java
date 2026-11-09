package dev.creeperkatze.organize;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if <26.1 {
/*import net.minecraft.client.gui.components.AbstractScrollArea;
*///?}
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;

// A folder of resource packs, or the way back up, in the available pack list
public class PackFolderEntry extends TransferableSelectionList.Entry {
	private static final Identifier DEFAULT_ICON = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "pack_folder");
	private static final Identifier BACK_ICON = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "pack_folder_back");
	private static final Identifier OPEN_SPRITE = Identifier.withDefaultNamespace("transferable_list/select");
	private static final Identifier OPEN_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("transferable_list/select_highlighted");
	private static final Identifier BACK_SPRITE = Identifier.withDefaultNamespace("transferable_list/unselect");
	private static final Identifier BACK_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("transferable_list/unselect_highlighted");
	private static final Identifier SELECT_ALL_SPRITE = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "select_all");
	private static final Identifier SELECT_ALL_HIGHLIGHTED_SPRITE = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "select_all_highlighted");
	private static final Component SELECT_ALL_HINT = Component.translatable("organize.select_all");
	private static final int ICON_SIZE = 32;
	private static final int BACK_ARROW_OFFSET = 6;
	private static final int TEXT_WIDTH = 157;
	private static final int HOVER_COLOR = 0xA0909090;
	private static final int DESCRIPTION_COLOR = 0xFF808080;

	private final TransferableSelectionList list;
	private final PackSelectionScreen screen;
	private final String folder;
	private final @Nullable Identifier icon;
	// Every pack below the folder, empty for the way back
	private final List<PackSelectionModel.Entry> packs;
	private final Component name;
	private final StringWidget nameWidget;
	private final StringWidget descriptionWidget;

	private PackFolderEntry(TransferableSelectionList list, PackSelectionScreen screen, String folder, @Nullable Identifier icon, List<PackSelectionModel.Entry> packs, Component name, Component description) {
		list.super();
		this.list = list;
		this.screen = screen;
		this.folder = folder;
		this.icon = icon;
		this.packs = List.copyOf(packs);
		this.name = name;
		Minecraft minecraft = Minecraft.getInstance();
		this.nameWidget = new StringWidget(name, minecraft.font);
		this.descriptionWidget = new StringWidget(description.copy().withColor(DESCRIPTION_COLOR), minecraft.font);
	}

	public static PackFolderEntry folder(TransferableSelectionList list, PackSelectionScreen screen, String folder, String name, @Nullable Identifier icon, List<PackSelectionModel.Entry> packs) {
		Component description = packs.size() == 1
				? Component.translatable("organize.one_pack")
				: Component.translatable("organize.packs", packs.size());
		return new PackFolderEntry(list, screen, folder, icon, packs, Component.literal(name), description);
	}

	public static PackFolderEntry back(TransferableSelectionList list, PackSelectionScreen screen, String parent) {
		Component description = parent.isEmpty()
				? Component.translatable("organize.all_packs")
				: Component.literal(parent.substring(parent.lastIndexOf('/') + 1));
		return new PackFolderEntry(list, screen, parent, null, List.of(), Component.translatable("organize.back"), description);
	}

	private boolean isBack() {
		return this.packs.isEmpty();
	}

	private boolean isOverIcon(double mouseX, double mouseY) {
		int x = this.getContentX();
		int y = this.getContentY();
		return mouseX >= x && mouseX < x + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE;
	}

	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
		int x = this.getContentX();
		int y = this.getContentY();
		if (this.icon != null) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, this.icon, x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
		} else {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.isBack() ? BACK_ICON : DEFAULT_ICON, x, y, ICON_SIZE, ICON_SIZE);
		}
		if (hovered || this.list.getSelected() == this && this.list.isFocused()) {
			graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, HOVER_COLOR);
			boolean overIcon = this.isOverIcon(mouseX, mouseY);
			Identifier sprite;
			if (this.isBack()) {
				sprite = overIcon ? BACK_HIGHLIGHTED_SPRITE : BACK_SPRITE;
			} else if (Minecraft.getInstance().hasShiftDown()) {
				sprite = overIcon ? SELECT_ALL_HIGHLIGHTED_SPRITE : SELECT_ALL_SPRITE;
			} else {
				sprite = overIcon ? OPEN_HIGHLIGHTED_SPRITE : OPEN_SPRITE;
			}
			// Vanilla draws the back arrow on the left half of the icon, this mirrors the open arrow
			int spriteX = this.isBack() ? x + BACK_ARROW_OFFSET : x;
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, spriteX, y, ICON_SIZE, ICON_SIZE);
			if (overIcon && !this.isBack()) {
				graphics.setTooltipForNextFrame(SELECT_ALL_HINT, mouseX, mouseY);
			}
		}
		//? if >=26.1 {
		int textWidth = TEXT_WIDTH - (this.list.maxScrollAmount() > 0 ? this.list.scrollbarWidth() : 0);
		//?} else {
		/*int textWidth = TEXT_WIDTH - (this.list.maxScrollAmount() > 0 ? AbstractScrollArea.SCROLLBAR_WIDTH : 0);
		*///?}
		this.nameWidget.setMaxWidth(textWidth);
		this.nameWidget.setPosition(x + ICON_SIZE + 2, y + 1);
		//? if >=26.1 {
		this.nameWidget.extractRenderState(graphics, mouseX, mouseY, a);
		//?} else {
		/*this.nameWidget.render(graphics, mouseX, mouseY, a);
		*///?}
		this.descriptionWidget.setMaxWidth(textWidth);
		this.descriptionWidget.setPosition(x + ICON_SIZE + 2, y + 12);
		//? if >=26.1 {
		this.descriptionWidget.extractRenderState(graphics, mouseX, mouseY, a);
		//?} else {
		/*this.descriptionWidget.render(graphics, mouseX, mouseY, a);
		*///?}
	}

	// A click opens the folder, a shift click selects all of its packs
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!this.isBack() && event.hasShiftDown()) {
			PackFolders.selectAll(this.packs);
		} else {
			PackFolders.open(this.screen, this.folder);
		}
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.isConfirmation()) {
			PackFolders.open(this.screen, this.folder);
			return true;
		}
		return super.keyPressed(event);
	}

	// Opening or selecting rebuilds the list, so this entry is gone afterwards
	@Override
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}

	@Override
	public Component getNarration() {
		return Component.translatable("narrator.select", this.name);
	}

	@Override
	public String getPackId() {
		return "";
	}
}
