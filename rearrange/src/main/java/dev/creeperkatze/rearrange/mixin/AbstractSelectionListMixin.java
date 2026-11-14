package dev.creeperkatze.rearrange.mixin;

import dev.creeperkatze.rearrange.DraggableList;
import dev.creeperkatze.rearrange.ListDrag;
import dev.creeperkatze.rearrange.Reorder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.ScreenRectangle;
//? if >=1.21.9 {
import net.minecraft.client.gui.layouts.LayoutElement;
//?} else {
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
*///?}
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin extends AbstractContainerWidget implements DraggableList {
	@Shadow
	@Final
	private List<Object> children;

	@Unique
	private boolean rearrange$dragCreated;
	@Unique
	private @Nullable ListDrag rearrange$drag;

	private AbstractSelectionListMixin(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	@Override
	public List<Object> rearrange$entries() {
		return this.children;
	}

	//? if >=1.21.9 {
	@Shadow
	private void repositionEntries() {
		throw new AssertionError();
	}

	@Override
	public ScreenRectangle rearrange$bounds(Object entry) {
		return ((LayoutElement) entry).getRectangle();
	}

	@Override
	public void rearrange$move(Object entry, int x, int y) {
		((LayoutElement) entry).setX(x);
		((LayoutElement) entry).setY(y);
	}

	@Override
	public void rearrange$repositionEntries() {
		this.repositionEntries();
	}

	@Override
	public void rearrange$extractEntry(Object entry, GuiGraphicsExtractor graphics, float a) {
		// A mouse position off the entry keeps its hover buttons and tooltips hidden
		((ObjectSelectionList.Entry<?>) entry).extractContent(graphics, -1, -1, false, a);
	}

	@Override
	public boolean rearrange$isOverScrollbar(double x, double y) {
		return this.isOverScrollbar(x, y);
	}
	//?} else {
	/*// Entries don't have a position yet, every entry is drawn at the top of its row
	@Unique
	private final Map<Object, ScreenRectangle> rearrange$moved = new HashMap<>();

	@Shadow
	@Final
	protected int itemHeight;

	@Shadow
	public abstract int getRowLeft();

	@Shadow
	public abstract int getRowWidth();

	@Shadow
	protected abstract int getRowTop(int index);

	@Shadow
	protected abstract boolean scrollbarVisible();

	@Shadow
	protected abstract int getScrollbarPosition();

	// Rows are as high as the list says, the entry inside leaves 2 pixels on every side
	@Override
	public ScreenRectangle rearrange$bounds(Object entry) {
		ScreenRectangle moved = this.rearrange$moved.get(entry);
		if (moved != null) {
			return moved;
		}
		return new ScreenRectangle(this.getRowLeft() - 2, this.getRowTop(this.children.indexOf(entry)) - 2, this.getRowWidth(), this.itemHeight);
	}

	@Override
	public void rearrange$move(Object entry, int x, int y) {
		this.rearrange$moved.put(entry, new ScreenRectangle(x, y, this.getRowWidth(), this.itemHeight));
	}

	@Override
	public void rearrange$repositionEntries() {
		this.rearrange$moved.clear();
	}

	@Override
	public void rearrange$extractEntry(Object entry, GuiGraphicsExtractor graphics, float a) {
		ScreenRectangle bounds = this.rearrange$bounds(entry);
		// A mouse position off the entry keeps its hover buttons and tooltips hidden
		((ObjectSelectionList.Entry<?>) entry).render(graphics, this.children.indexOf(entry), bounds.top() + 2, bounds.left() + 2, this.getRowWidth(), this.itemHeight - 4, -1, -1, false, a);
	}

	@Override
	public boolean rearrange$isOverScrollbar(double x, double y) {
		return this.scrollbarVisible() && x >= this.getScrollbarPosition() && x < this.getScrollbarPosition() + 6;
	}

	@Inject(method = "getRowTop", at = @At("HEAD"), cancellable = true)
	private void rearrange$movedRowTop(int index, CallbackInfoReturnable<Integer> cir) {
		ScreenRectangle moved = index >= 0 && index < this.children.size() ? this.rearrange$moved.get(this.children.get(index)) : null;
		if (moved != null) {
			cir.setReturnValue(moved.top() + 2);
		}
	}

	// The list handles clicks itself instead of passing them on to AbstractContainerWidget
	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void rearrange$press(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
		ListDrag drag = this.rearrange$drag();
		if (drag != null) {
			drag.press(x, y, button);
		}
	}

	@Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
	private void rearrange$drag(double x, double y, int button, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
		ListDrag drag = this.rearrange$drag();
		if (drag != null && drag.drag(x, y)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"))
	private void rearrange$release(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
		ListDrag drag = this.rearrange$drag();
		if (drag != null) {
			drag.release();
		}
	}
	*///?}

	@Override
	public @Nullable ListDrag rearrange$drag() {
		if (!this.rearrange$dragCreated) {
			this.rearrange$dragCreated = true;
			AbstractSelectionList<?> list = (AbstractSelectionList<?>) (Object) this;
			Reorder reorder = Reorder.of(list);
			this.rearrange$drag = reorder == null ? null : new ListDrag(list, reorder);
		}
		return this.rearrange$drag;
	}

	@Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
	private void rearrange$frame(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		ListDrag drag = this.rearrange$drag();
		if (drag != null) {
			drag.frame(mouseX, mouseY);
		}
	}

	@Inject(method = "extractListItems", at = @At("HEAD"))
	private void rearrange$openGap(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		ListDrag.beforeItems((AbstractSelectionList<?>) (Object) this);
	}

	@Inject(method = "extractListItems", at = @At("TAIL"))
	private void rearrange$closeGap(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		ListDrag.afterItems((AbstractSelectionList<?>) (Object) this, graphics);
	}
}
