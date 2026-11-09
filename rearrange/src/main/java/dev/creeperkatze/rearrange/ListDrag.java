package dev.creeperkatze.rearrange;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import dev.creeperkatze.rearrange.mixin.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ListDrag {
	private static final int DRAG_THRESHOLD = 4;
	private static final int SCROLL_EDGE = 20;
	private static final double SCROLL_SPEED = 0.6;
	private static final int HIDDEN_Y = -100000;
	private static final int GAP_COLOR = 0x40FFFFFF;
	private static final int OUTLINE_COLOR = 0xFFFFFFFF;
	private static final int BACKGROUND_COLOR = 0xFF000000;
	// Milliseconds for an entry to cover about two thirds of the way to its place
	private static final double ANIMATION_TIME = 40.0;

	// The drag in progress, drawn above the whole screen
	private static @Nullable ListDrag active;

	private final AbstractSelectionList<?> list;
	private final Reorder reorder;
	private @Nullable LayoutElement pressed;
	private int pressedIndex;
	private double pressX;
	private double pressY;
	private @Nullable LayoutElement dragged;
	private Reorder.@Nullable Move move;
	private int from;
	private double grabOffsetX;
	private double grabOffsetY;
	private double mouseX;
	private double mouseY;
	private long lastFrame;
	private boolean overTransfer;
	private int transferSlot;
	// Keyed by Reorder.key, so the positions survive a list that rebuilds its entries
	private Map<Object, Double> shownY = new HashMap<>();
	private @Nullable Double shownGap;
	private long lastAnimation;

	public ListDrag(AbstractSelectionList<?> list, Reorder reorder) {
		this.list = list;
		this.reorder = reorder;
	}

	public static @Nullable ListDrag of(Object widget) {
		return widget instanceof DraggableList list ? list.rearrange$drag() : null;
	}

	private static List<LayoutElement> entries(AbstractSelectionList<?> list) {
		return ((DraggableList) list).rearrange$entries();
	}

	private List<LayoutElement> entries() {
		return entries(this.list);
	}

	public void press(MouseButtonEvent event) {
		this.reset();
		if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || ((DraggableList) this.list).rearrange$isOverScrollbar(event.x(), event.y())) {
			return;
		}
		List<LayoutElement> entries = this.entries();
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getRectangle().containsPoint((int) event.x(), (int) event.y())) {
				this.pressed = entries.get(i);
				this.pressedIndex = i;
				this.pressX = event.x();
				this.pressY = event.y();
				return;
			}
		}
	}

	public boolean drag(MouseButtonEvent event) {
		if (this.dragged == null) {
			if (this.pressed == null || Math.abs(event.y() - this.pressY) < DRAG_THRESHOLD && Math.abs(event.x() - this.pressX) < DRAG_THRESHOLD) {
				return false;
			}
			if (!this.start()) {
				this.reset();
				return false;
			}
		}
		this.mouseX = event.x();
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
		this.grabOffsetX = this.pressX - this.dragged.getX();
		this.grabOffsetY = this.pressY - this.dragged.getY();
		this.lastFrame = Util.getMillis();
		this.overTransfer = false;
		active = this;
		return true;
	}

	public void release() {
		LayoutElement dragged = this.dragged;
		Reorder.Move move = this.move;
		boolean overTransfer = this.overTransfer;
		int transferSlot = this.transferSlot;
		this.reset();
		if (dragged == null || move == null) {
			return;
		}
		if (overTransfer) {
			if (transferSlot >= 0) {
				move.transfer().drop(transferSlot);
			}
			return;
		}
		int to = this.entries().indexOf(dragged);
		if (to >= 0 && to != this.from) {
			move.drop(to);
		}
	}

	// Moves an entry up or down by one, returns false if it can't move there
	public boolean step(LayoutElement entry, int direction) {
		List<LayoutElement> entries = this.entries();
		int from = entries.indexOf(entry);
		int to = from + direction;
		if (this.dragged != null || from < 0 || to < 0 || to >= entries.size()) {
			return false;
		}
		Reorder.Move move = this.reorder.pickUp(from);
		if (move == null || !move.canDrop(to)) {
			return false;
		}
		entries.remove(from);
		entries.add(to, entry);
		((DraggableList) this.list).rearrange$repositionEntries();
		move.drop(to);
		this.scrollTo(entry);
		return true;
	}

	private void scrollTo(LayoutElement entry) {
		int above = this.list.getY() - entry.getY();
		int below = entry.getY() + entry.getHeight() - this.list.getBottom();
		if (above > 0) {
			this.list.setScrollAmount(this.list.scrollAmount() - above);
		} else if (below > 0) {
			this.list.setScrollAmount(this.list.scrollAmount() + below);
		}
	}

	private void reset() {
		this.pressed = null;
		this.dragged = null;
		this.move = null;
		if (active == this) {
			active = null;
		}
	}

	public void frame(int mouseX, int mouseY) {
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
		this.mouseX = mouseX;
		this.mouseY = mouseY;

		AbstractSelectionList<?> scrolled = this.overTransfer ? this.move.transfer().list() : this.list;
		int top = scrolled.getY() + SCROLL_EDGE;
		int bottom = scrolled.getBottom() - SCROLL_EDGE;
		double depth = mouseY < top ? mouseY - top : mouseY > bottom ? mouseY - bottom : 0;
		if (depth != 0) {
			double speed = Mth.clamp(depth / SCROLL_EDGE, -1.0, 1.0) * SCROLL_SPEED;
			scrolled.setScrollAmount(scrolled.scrollAmount() + speed * elapsed);
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

		Reorder.Transfer transfer = this.move.transfer();
		this.overTransfer = transfer != null && transfer.list().isMouseOver(this.mouseX, this.mouseY);
		if (this.overTransfer) {
			this.transferSlot = this.transferSlot(transfer);
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
			((DraggableList) this.list).rearrange$repositionEntries();
		}
	}

	// Returns the allowed gap between entries of the other list closest to the mouse, or -1
	private int transferSlot(Reorder.Transfer transfer) {
		List<LayoutElement> entries = entries(transfer.list());
		int target = -1;
		double closest = Double.MAX_VALUE;
		for (int slot = 0; slot <= entries.size(); slot++) {
			double distance = Math.abs(gapY(transfer.list(), slot) - this.mouseY);
			if (distance < closest && transfer.canDrop(slot)) {
				closest = distance;
				target = slot;
			}
		}
		return target;
	}

	private static int gapY(AbstractSelectionList<?> list, int slot) {
		List<LayoutElement> entries = entries(list);
		if (slot < entries.size()) {
			return entries.get(slot).getY();
		}
		return entries.isEmpty() ? list.getY() : entries.getLast().getY() + entries.getLast().getHeight();
	}

	private double floatingY() {
		// Entries that can move to another list are free to leave their own
		boolean free = this.move.transfer() != null;
		int top = free ? 0 : this.list.getY();
		int bottom = free ? Minecraft.getInstance().getWindow().getGuiScaledHeight() : this.list.getBottom();
		return Mth.clamp(this.mouseY - this.grabOffsetY, top, bottom - this.dragged.getHeight());
	}

	private boolean targets(AbstractSelectionList<?> list) {
		return this.dragged != null && this.overTransfer && this.transferSlot >= 0 && this.move.transfer().list() == list;
	}

	// Runs before a list draws its entries, makes room for the dragged entry and eases every entry towards its place
	public static void beforeItems(AbstractSelectionList<?> list) {
		ListDrag own = of(list);
		if (own == null) {
			return;
		}
		ListDrag drag = active;
		boolean targeted = drag != null && drag.targets(list);
		Integer gapY = targeted ? gapY(list, drag.transferSlot) : null;
		// Over another list the entry has left this one, so its gap closes
		if (own.dragged != null && own.overTransfer) {
			shift(list, entries(list).indexOf(own.dragged) + 1, -own.dragged.getHeight());
		}
		if (targeted) {
			shift(list, drag.transferSlot, drag.dragged.getHeight());
		}
		own.animate(gapY);
		if (own.dragged != null) {
			own.dragged.setY(HIDDEN_Y);
		}
	}

	// Runs after a list drew its entries, puts them back and outlines the gap
	public static void afterItems(AbstractSelectionList<?> list, GuiGraphicsExtractor graphics) {
		ListDrag own = of(list);
		if (own == null) {
			return;
		}
		((DraggableList) list).rearrange$repositionEntries();
		LayoutElement dragged = own.dragged;
		if (dragged != null && !own.overTransfer) {
			graphics.outline(dragged.getX(), own.shownY(dragged), dragged.getWidth(), dragged.getHeight(), GAP_COLOR);
		}
		ListDrag drag = active;
		if (own.shownGap != null && drag != null && drag.targets(list)) {
			LayoutElement row = entries(list).getFirst();
			graphics.outline(row.getX(), own.origin() + (int) Math.round(own.shownGap), row.getWidth(), drag.dragged.getHeight(), GAP_COLOR);
		}
	}

	// Positions are kept relative to the top of the content, so scrolling doesn't animate
	private void animate(@Nullable Integer gapY) {
		long now = Util.getMillis();
		double progress = 1.0 - Math.exp(-(now - this.lastAnimation) / ANIMATION_TIME);
		this.lastAnimation = now;
		int origin = this.origin();
		Map<Object, Double> shownY = new HashMap<>();
		for (LayoutElement entry : this.entries()) {
			Object key = this.reorder.key(entry);
			double y = ease(this.shownY.get(key), entry.getY() - origin, progress);
			shownY.put(key, y);
			entry.setY(origin + (int) Math.round(y));
		}
		this.shownY = shownY;
		this.shownGap = gapY == null ? null : ease(this.shownGap, gapY - origin, progress);
	}

	private static double ease(@Nullable Double shown, double target, double progress) {
		return shown == null || Math.abs(target - shown) < 0.5 ? target : shown + (target - shown) * progress;
	}

	private int origin() {
		return this.list.getY() - (int) this.list.scrollAmount();
	}

	private int shownY(LayoutElement entry) {
		Double shown = this.shownY.get(this.reorder.key(entry));
		return shown == null ? entry.getY() : this.origin() + (int) Math.round(shown);
	}

	private static void shift(AbstractSelectionList<?> list, int from, int distance) {
		List<LayoutElement> entries = entries(list);
		for (int i = Math.max(from, 0); i < entries.size(); i++) {
			entries.get(i).setY(entries.get(i).getY() + distance);
		}
	}

	public static void extractActive(Screen screen, GuiGraphicsExtractor graphics, float a) {
		ListDrag drag = active;
		if (drag == null) {
			return;
		}
		if (!screen.children().contains(drag.list)) {
			drag.reset();
			return;
		}
		drag.extractFloating(graphics, a);
	}

	private void extractFloating(GuiGraphicsExtractor graphics, float a) {
		Reorder.Transfer transfer = this.move.transfer();
		graphics.nextStratum();
		LayoutElement dragged = this.dragged;
		int slotX = dragged.getX();
		int slotY = dragged.getY();
		int x = transfer == null ? slotX : (int) (this.mouseX - this.grabOffsetX);
		int y = (int) this.floatingY();
		int width = dragged.getWidth();
		int height = dragged.getHeight();
		dragged.setX(x);
		dragged.setY(y);
		graphics.fill(x, y, x + width, y + height, OUTLINE_COLOR);
		graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BACKGROUND_COLOR);
		// A mouse position off the entry keeps its hover buttons and tooltips hidden
		((ObjectSelectionList.Entry<?>) dragged).extractContent(graphics, -1, -1, false, a);
		dragged.setX(slotX);
		dragged.setY(slotY);
		graphics.requestCursor(transfer == null ? CursorTypes.RESIZE_NS : CursorTypes.RESIZE_ALL);
	}
}
