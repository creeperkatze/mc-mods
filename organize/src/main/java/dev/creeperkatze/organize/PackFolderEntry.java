package dev.creeperkatze.organize;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=1.21.9 && <26.1 {
/*import net.minecraft.client.gui.components.AbstractScrollArea;
*///?}
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?} else {
/*import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
*///?}
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;

// A folder of resource packs, or the way back up, in the available pack list
//? if >=1.21.9 {
public class PackFolderEntry extends TransferableSelectionList.Entry {
//?} else {
/*public class PackFolderEntry extends TransferableSelectionList.PackEntry {
*///?}
	private static final Identifier DEFAULT_ICON = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "pack_folder");
	private static final Identifier BACK_ICON = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "pack_folder_back");
	private static final Identifier OPEN_SPRITE = Identifier.withDefaultNamespace("transferable_list/select");
	private static final Identifier OPEN_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("transferable_list/select_highlighted");
	private static final Identifier BACK_SPRITE = Identifier.withDefaultNamespace("transferable_list/unselect");
	private static final Identifier BACK_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("transferable_list/unselect_highlighted");
	private static final Identifier SELECT_ALL_SPRITE = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "select_all");
	private static final Identifier SELECT_ALL_HIGHLIGHTED_SPRITE = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "select_all_highlighted");
	private static final Component SELECT_ALL_HINT = Component.translatable("organize.select_all");
	private static final Identifier DESELECT_ALL_SPRITE = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "deselect_all");
	private static final Identifier DESELECT_ALL_HIGHLIGHTED_SPRITE = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "deselect_all_highlighted");
	private static final Component DESELECT_ALL_HINT = Component.translatable("organize.deselect_all");
	private static final int ICON_SIZE = 32;
	private static final int BACK_ARROW_OFFSET = 6;
	private static final int TEXT_WIDTH = 157;
	private static final int HOVER_COLOR = 0xA0909090;
	private static final int DESCRIPTION_COLOR = 0xFF808080;

	private final TransferableSelectionList list;
	private final PackSelectionScreen screen;
	private final String folder;
	private final @Nullable Identifier icon;
	// Every pack below the folder, selected or not, empty for the way back
	private final List<PackSelectionModel.Entry> packs;
	private final Component name;
	private final StringWidget nameWidget;
	private final StringWidget descriptionWidget;

	private PackFolderEntry(TransferableSelectionList list, PackSelectionScreen screen, String folder, @Nullable Identifier icon, List<PackSelectionModel.Entry> packs, Component name, Component description) {
		//? if >=1.21.9 {
		list.super();
		//?} else {
		/*super(Minecraft.getInstance(), list, new FolderPack(name, description));
		*///?}
		this.list = list;
		this.screen = screen;
		this.folder = folder;
		this.icon = icon;
		this.packs = List.copyOf(packs);
		this.name = name;
		Minecraft minecraft = Minecraft.getInstance();
		this.nameWidget = new StringWidget(name, minecraft.font);
		this.descriptionWidget = new StringWidget(description.copy().withColor(DESCRIPTION_COLOR), minecraft.font);
		//? if <1.21.9 {
		/*this.nameWidget.alignLeft();
		this.descriptionWidget.alignLeft();
		*///?}
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

	// Selects the folder while some of its packs can still be selected, deselects it after that
	private boolean selects() {
		return PackFolders.canSelectAny(this.packs);
	}

	private static boolean isOverIcon(int x, int y, double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE;
	}

	private static boolean hasShiftDown() {
		//? if >=1.21.9 {
		return Minecraft.getInstance().hasShiftDown();
		//?} else {
		/*return Screen.hasShiftDown();
		*///?}
	}

	//? if >=1.21.9 {
	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
		this.extract(graphics, this.getContentX(), this.getContentY(), mouseX, mouseY, hovered, a);
	}
	//?} else {
	/*@Override
	public void render(GuiGraphicsExtractor graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float a) {
		this.extract(graphics, x, y, mouseX, mouseY, hovered, a);
	}
	*///?}

	private void extract(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, boolean hovered, float a) {
		if (this.icon != null) {
			//? if >=1.21.6 {
			graphics.blit(RenderPipelines.GUI_TEXTURED, this.icon, x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
			//?} else {
			/*graphics.blit(this.icon, x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
			*///?}
		} else {
			this.blitSprite(graphics, this.isBack() ? BACK_ICON : DEFAULT_ICON, x, y);
		}
		if (hovered || this.list.getSelected() == this && this.list.isFocused()) {
			graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, HOVER_COLOR);
			boolean overIcon = isOverIcon(x, y, mouseX, mouseY);
			Identifier sprite;
			if (this.isBack()) {
				sprite = overIcon ? BACK_HIGHLIGHTED_SPRITE : BACK_SPRITE;
			} else if (hasShiftDown() && this.selects()) {
				sprite = overIcon ? SELECT_ALL_HIGHLIGHTED_SPRITE : SELECT_ALL_SPRITE;
			} else if (hasShiftDown()) {
				sprite = overIcon ? DESELECT_ALL_HIGHLIGHTED_SPRITE : DESELECT_ALL_SPRITE;
			} else {
				sprite = overIcon ? OPEN_HIGHLIGHTED_SPRITE : OPEN_SPRITE;
			}
			// Vanilla draws the back arrow on the left half of the icon, this mirrors the open arrow
			int spriteX = this.isBack() ? x + BACK_ARROW_OFFSET : x;
			this.blitSprite(graphics, sprite, spriteX, y);
			if (overIcon && !this.isBack()) {
				Component hint = this.selects() ? SELECT_ALL_HINT : DESELECT_ALL_HINT;
				//? if >=1.21.6 {
				graphics.setTooltipForNextFrame(hint, mouseX, mouseY);
				//?} else {
				/*this.screen.setTooltipForNextRenderPass(hint);
				*///?}
			}
		}
		//? if >=26.1 {
		int textWidth = TEXT_WIDTH - (this.list.maxScrollAmount() > 0 ? this.list.scrollbarWidth() : 0);
		//?} else if >=1.21.9 {
		/*int textWidth = TEXT_WIDTH - (this.list.maxScrollAmount() > 0 ? AbstractScrollArea.SCROLLBAR_WIDTH : 0);
		*///?} else {
		/*int textWidth = TEXT_WIDTH - (this.list.getMaxScroll() > 0 ? 6 : 0);
		*///?}
		this.extractText(this.nameWidget, graphics, textWidth, x + ICON_SIZE + 2, y + 1, mouseX, mouseY, a);
		this.extractText(this.descriptionWidget, graphics, textWidth, x + ICON_SIZE + 2, y + 12, mouseX, mouseY, a);
	}

	private void blitSprite(GuiGraphicsExtractor graphics, Identifier sprite, int x, int y) {
		//? if >=1.21.6 {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, ICON_SIZE, ICON_SIZE);
		//?} else {
		/*graphics.blitSprite(sprite, x, y, ICON_SIZE, ICON_SIZE);
		*///?}
	}

	private void extractText(StringWidget widget, GuiGraphicsExtractor graphics, int width, int x, int y, int mouseX, int mouseY, float a) {
		//? if >=1.21.9 {
		widget.setMaxWidth(width);
		//?} else {
		/*widget.setWidth(width);
		*///?}
		widget.setPosition(x, y);
		//? if >=26.1 {
		widget.extractRenderState(graphics, mouseX, mouseY, a);
		//?} else {
		/*widget.render(graphics, mouseX, mouseY, a);
		*///?}
	}

	// A click opens the folder, a shift click selects or deselects all of its packs
	private void click(boolean shift) {
		if (!this.isBack() && shift && this.selects()) {
			PackFolders.selectAll(this.packs);
		} else if (!this.isBack() && shift) {
			PackFolders.deselectAll(this.packs);
		} else {
			PackFolders.open(this.screen, this.folder);
		}
	}

	//? if >=1.21.9 {
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		this.click(event.hasShiftDown());
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
	//?} else {
	/*@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.click(Screen.hasShiftDown());
		return true;
	}

	// Enter and space in the pack list
	@Override
	public void keyboardSelection() {
		PackFolders.open(this.screen, this.folder);
	}
	*///?}

	@Override
	public Component getNarration() {
		return Component.translatable("narrator.select", this.name);
	}

	@Override
	public String getPackId() {
		return "";
	}

	//? if <1.21.9 {
	/*// The vanilla pack entry needs a pack, this one can't be selected or moved
	private record FolderPack(Component getTitle, Component getDescription) implements PackSelectionModel.Entry {
		@Override
		public Identifier getIconTexture() {
			return DEFAULT_ICON;
		}

		@Override
		public PackCompatibility getCompatibility() {
			return PackCompatibility.COMPATIBLE;
		}

		@Override
		public String getId() {
			return "";
		}

		@Override
		public PackSource getPackSource() {
			return PackSource.DEFAULT;
		}

		@Override
		public boolean isFixedPosition() {
			return true;
		}

		@Override
		public boolean isRequired() {
			return true;
		}

		@Override
		public void select() {
		}

		@Override
		public void unselect() {
		}

		@Override
		public void moveUp() {
		}

		@Override
		public void moveDown() {
		}

		@Override
		public boolean isSelected() {
			return false;
		}

		@Override
		public boolean canSelect() {
			return false;
		}

		@Override
		public boolean canMoveUp() {
			return false;
		}

		@Override
		public boolean canMoveDown() {
			return false;
		}
	}
	*///?}
}
