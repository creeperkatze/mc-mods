package dev.creeperkatze.rearrange;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import dev.creeperkatze.rearrange.mixin.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class ListDrag {
	private static final int DRAG_THRESHOLD = 4;
	private static final int SCROLL_EDGE = 20;
	private static final double SCROLL_SPEED = 0.6;
	private static final int HIDDEN_Y = -100000;
	private static final int GAP_COLOR = 0x40FFFFFF;
	private static final int OUTLINE_COLOR = 0xFFFFFFFF;
	private static final int BACKGROUND_COLOR = 0xFF000000;

	private final AbstractSelectionList<?> list;
	private final Reorder reorder;
	private @Nullable LayoutElement pressed;
	private int pressedIndex;
	private double pressY;
	private @Nullable LayoutElement dragged;
	private Reorder.@Nullable Move move;
	private int from;
	private double grabOffset;
	private double mouseY;
	private long lastFrame;

	public ListDrag(AbstractSelectionList<?> list, Reorder reorder) {
		this.list = list;
		this.reorder = reorder;
	}

	public static @Nullable ListDrag of(Object widget) {
		return widget instanceof DraggableList list ? list.rearrange$drag() : null;
	}

	private DraggableList draggable() {
		return (DraggableList) this.list;
	}

	private List<LayoutElement> entries() {
		return this.draggable().rearrange$entries();
	}

	public void press(MouseButtonEvent event) {
		this.reset();
		if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || this.draggable().rearrange$isOverScrollbar(event.x(), event.y())) {
			return;
		}
		List<LayoutElement> entries = this.entries();
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getRectangle().containsPoint((int) event.x(), (int) event.y())) {
				this.pressed = entries.get(i);
				this.pressedIndex = i;
				this.pressY = event.y();
				return;
			}
		}
	}

	public boolean drag(MouseButtonEvent event) {
		if (this.dragged == null) {
			if (this.pressed == null || Math.abs(event.y() - this.pressY) < DRAG_THRESHOLD) {
				return false;
			}
			if (!this.start()) {
				this.reset();
				return false;
			}
		}
		this.mouseY = event.y();
		this.update();
		return true;
	}

	private boolean start() {
		// A click on the entry may have rebuilt the list
		if (this.entries().indexOf(this.pressed) != this.pressedIndex) {
			return false;
		}
		this.move = this.reorder.pickUp(this.pressedIndex);
		if (this.move == null) {
			return false;
		}
		this.dragged = this.pressed;
		this.from = this.pressedIndex;
		this.grabOffset = this.pressY - this.dragged.getY();
		this.lastFrame = Util.getMillis();
		return true;
	}

	public void release() {
		LayoutElement dragged = this.dragged;
		Reorder.Move move = this.move;
		int from = this.from;
		this.reset();
		if (dragged != null && move != null) {
			int to = this.entries().indexOf(dragged);
			if (to >= 0 && to != from) {
				move.drop(to);
			}
		}
	}

	private void reset() {
		this.pressed = null;
		this.dragged = null;
		this.move = null;
	}

	public void frame(int mouseY) {
		if (this.dragged == null) {
			return;
		}
		// The release can happen outside the list or the window
		MouseButtonInfo button = ((MouseHandlerAccessor) Minecraft.getInstance().mouseHandler).rearrange$getActiveButton();
		if (button == null || button.button() != InputConstants.MOUSE_BUTTON_LEFT) {
			this.release();
			return;
		}
		long now = Util.getMillis();
		long elapsed = now - this.lastFrame;
		this.lastFrame = now;
		this.mouseY = mouseY;

		int top = this.list.getY() + SCROLL_EDGE;
		int bottom = this.list.getBottom() - SCROLL_EDGE;
		double depth = mouseY < top ? mouseY - top : mouseY > bottom ? mouseY - bottom : 0;
		if (depth != 0) {
			double speed = Mth.clamp(depth / SCROLL_EDGE, -1.0, 1.0) * SCROLL_SPEED;
			this.list.setScrollAmount(this.list.scrollAmount() + speed * elapsed);
		}
		this.update();
	}

	private void update() {
		List<LayoutElement> entries = this.entries();
		int current = entries.indexOf(this.dragged);
		if (current < 0) {
			this.reset();
			return;
		}

		// Pick the allowed slot closest to the dragged entry, with the slots laid out as if it was taken out of the list
		LayoutElement dragged = this.dragged;
		List<LayoutElement> others = entries.stream().filter(entry -> entry != dragged).toList();
		double center = this.floatingY() + dragged.getHeight() / 2.0;
		int slotY = entries.getFirst().getY();
		int target = current;
		double closest = Double.MAX_VALUE;
		for (int slot = 0; slot <= others.size(); slot++) {
			double distance = Math.abs(slotY + dragged.getHeight() / 2.0 - center);
			if (distance < closest && this.move.canDrop(slot)) {
				closest = distance;
				target = slot;
			}
			if (slot < others.size()) {
				slotY += others.get(slot).getHeight();
			}
		}

		if (target != current) {
			entries.remove(current);
			entries.add(target, dragged);
			this.draggable().rearrange$repositionEntries();
		}
	}

	private double floatingY() {
		return Mth.clamp(this.mouseY - this.grabOffset, this.list.getY(), this.list.getBottom() - this.dragged.getHeight());
	}

	public void hideDragged() {
		if (this.dragged != null) {
			this.dragged.setY(HIDDEN_Y);
		}
	}

	public void extractDragged(GuiGraphicsExtractor graphics, float a) {
		if (this.dragged == null) {
			return;
		}
		this.draggable().rearrange$repositionEntries();
		int x = this.dragged.getX();
		int width = this.dragged.getWidth();
		int height = this.dragged.getHeight();
		int slotY = this.dragged.getY();
		graphics.outline(x, slotY, width, height, GAP_COLOR);

		graphics.nextStratum();
		int y = (int) this.floatingY();
		this.dragged.setY(y);
		graphics.fill(x, y, x + width, y + height, OUTLINE_COLOR);
		graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BACKGROUND_COLOR);
		// A mouse position off the entry keeps its hover buttons and tooltips hidden
		((ObjectSelectionList.Entry<?>) this.dragged).extractContent(graphics, -1, -1, false, a);
		this.dragged.setY(slotY);
		graphics.requestCursor(CursorTypes.RESIZE_NS);
	}
}
