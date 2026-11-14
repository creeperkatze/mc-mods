package dev.creeperkatze.rearrange;

import com.mojang.blaze3d.platform.InputConstants;
//? if >=1.21.9 {
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.input.MouseButtonInfo;
//?}
import dev.creeperkatze.rearrange.mixin.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
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
	private @Nullable Object pressed;
	private int pressedIndex;
	private double pressX;
	private double pressY;
	private @Nullable Object dragged;
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

	private static List<Object> entries(AbstractSelectionList<?> list) {
		return ((DraggableList) list).rearrange$entries();
	}

	private static ScreenRectangle bounds(AbstractSelectionList<?> list, Object entry) {
		return ((DraggableList) list).rearrange$bounds(entry);
	}

	private static void setY(AbstractSelectionList<?> list, Object entry, int y) {
		((DraggableList) list).rearrange$move(entry, bounds(list, entry).left(), y);
	}

	private List<Object> entries() {
		return entries(this.list);
	}

	private ScreenRectangle bounds(Object entry) {
		return bounds(this.list, entry);
	}

	public void press(double x, double y, int button) {
		this.reset();
		if (button != InputConstants.MOUSE_BUTTON_LEFT || ((DraggableList) this.list).rearrange$isOverScrollbar(x, y)) {
			return;
		}
		List<Object> entries = this.entries();
		for (int i = 0; i < entries.size(); i++) {
			if (this.bounds(entries.get(i)).containsPoint((int) x, (int) y)) {
				this.pressed = entries.get(i);
				this.pressedIndex = i;
				this.pressX = x;
				this.pressY = y;
				return;
			}
		}
	}

	public boolean drag(double x, double y) {
		if (this.dragged == null) {
			if (this.pressed == null || Math.abs(y - this.pressY) < DRAG_THRESHOLD && Math.abs(x - this.pressX) < DRAG_THRESHOLD) {
				return false;
			}
			if (!this.start()) {
				this.reset();
				return false;
			}
		}
		this.mouseX = x;
		this.mouseY = y;
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
		ScreenRectangle bounds = this.bounds(this.dragged);
		this.grabOffsetX = this.pressX - bounds.left();
		this.grabOffsetY = this.pressY - bounds.top();
		this.lastFrame = Util.getMillis();
		this.overTransfer = false;
		active = this;
		return true;
	}

	public void release() {
		Object dragged = this.dragged;
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
	public boolean step(Object entry, int direction) {
		List<Object> entries = this.entries();
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

	private void scrollTo(Object entry) {
		ScreenRectangle bounds = this.bounds(entry);
		int above = this.list.getY() - bounds.top();
		int below = bounds.bottom() - this.list.getBottom();
		if (above > 0) {
			this.list.setScrollAmount(this.scrollAmount() - above);
		} else if (below > 0) {
			this.list.setScrollAmount(this.scrollAmount() + below);
		}
	}

	private double scrollAmount() {
		return scrollAmount(this.list);
	}

	private static double scrollAmount(AbstractSelectionList<?> list) {
		//? if >=1.21.9 {
		return list.scrollAmount();
		//?} else {
		/*return list.getScrollAmount();
		*///?}
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
		//? if >=1.21.9 {
		MouseButtonInfo button = ((MouseHandlerAccessor) Minecraft.getInstance().mouseHandler).rearrange$getActiveButton();
		boolean leftDown = button != null && button.button() == InputConstants.MOUSE_BUTTON_LEFT;
		//?} else {
		/*boolean leftDown = ((MouseHandlerAccessor) Minecraft.getInstance().mouseHandler).rearrange$getActiveButton() == InputConstants.MOUSE_BUTTON_LEFT;
		*///?}
		if (!leftDown) {
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
			scrolled.setScrollAmount(scrollAmount(scrolled) + speed * elapsed);
		}
		this.update();
	}

	private void update() {
		List<Object> entries = this.entries();
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
		Object dragged = this.dragged;
		int height = this.bounds(dragged).height();
		List<Object> others = entries.stream().filter(entry -> entry != dragged).toList();
		double center = this.floatingY() + height / 2.0;
		int slotY = this.bounds(entries.getFirst()).top();
		int target = current;
		double closest = Double.MAX_VALUE;
		for (int slot = 0; slot <= others.size(); slot++) {
			double distance = Math.abs(slotY + height / 2.0 - center);
			if (distance < closest && this.move.canDrop(slot)) {
				closest = distance;
				target = slot;
			}
			if (slot < others.size()) {
				slotY += this.bounds(others.get(slot)).height();
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
		List<Object> entries = entries(transfer.list());
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
		List<Object> entries = entries(list);
		if (slot < entries.size()) {
			return bounds(list, entries.get(slot)).top();
		}
		return entries.isEmpty() ? list.getY() : bounds(list, entries.getLast()).bottom();
	}

	private double floatingY() {
		// Entries that can move to another list are free to leave their own
		boolean free = this.move.transfer() != null;
		int top = free ? 0 : this.list.getY();
		int bottom = free ? Minecraft.getInstance().getWindow().getGuiScaledHeight() : this.list.getBottom();
		return Mth.clamp(this.mouseY - this.grabOffsetY, top, bottom - this.bounds(this.dragged).height());
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
			shift(list, entries(list).indexOf(own.dragged) + 1, -own.bounds(own.dragged).height());
		}
		if (targeted) {
			shift(list, drag.transferSlot, drag.bounds(drag.dragged).height());
		}
		own.animate(gapY);
		if (own.dragged != null) {
			setY(list, own.dragged, HIDDEN_Y);
		}
	}

	// Runs after a list drew its entries, puts them back and outlines the gap
	public static void afterItems(AbstractSelectionList<?> list, GuiGraphicsExtractor graphics) {
		ListDrag own = of(list);
		if (own == null) {
			return;
		}
		((DraggableList) list).rearrange$repositionEntries();
		Object dragged = own.dragged;
		if (dragged != null && !own.overTransfer) {
			ScreenRectangle bounds = own.bounds(dragged);
			graphics.outline(bounds.left(), own.shownY(dragged), bounds.width(), bounds.height(), GAP_COLOR);
		}
		ListDrag drag = active;
		if (own.shownGap != null && drag != null && drag.targets(list)) {
			ScreenRectangle row = bounds(list, entries(list).getFirst());
			graphics.outline(row.left(), own.origin() + (int) Math.round(own.shownGap), row.width(), drag.bounds(drag.dragged).height(), GAP_COLOR);
		}
	}

	// Positions are kept relative to the top of the content, so scrolling doesn't animate
	private void animate(@Nullable Integer gapY) {
		long now = Util.getMillis();
		double progress = 1.0 - Math.exp(-(now - this.lastAnimation) / ANIMATION_TIME);
		this.lastAnimation = now;
		int origin = this.origin();
		Map<Object, Double> shownY = new HashMap<>();
		for (Object entry : this.entries()) {
			Object key = this.reorder.key(entry);
			double y = ease(this.shownY.get(key), this.bounds(entry).top() - origin, progress);
			shownY.put(key, y);
			setY(this.list, entry, origin + (int) Math.round(y));
		}
		this.shownY = shownY;
		this.shownGap = gapY == null ? null : ease(this.shownGap, gapY - origin, progress);
	}

	private static double ease(@Nullable Double shown, double target, double progress) {
		return shown == null || Math.abs(target - shown) < 0.5 ? target : shown + (target - shown) * progress;
	}

	private int origin() {
		return this.list.getY() - (int) this.scrollAmount();
	}

	private int shownY(Object entry) {
		Double shown = this.shownY.get(this.reorder.key(entry));
		return shown == null ? this.bounds(entry).top() : this.origin() + (int) Math.round(shown);
	}

	private static void shift(AbstractSelectionList<?> list, int from, int distance) {
		List<Object> entries = entries(list);
		for (int i = Math.max(from, 0); i < entries.size(); i++) {
			setY(list, entries.get(i), bounds(list, entries.get(i)).top() + distance);
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
		//? if >=1.21.6 {
		graphics.nextStratum();
		//?}
		ScreenRectangle slot = this.bounds(this.dragged);
		int x = transfer == null ? slot.left() : (int) (this.mouseX - this.grabOffsetX);
		int y = (int) this.floatingY();
		int width = slot.width();
		int height = slot.height();
		graphics.fill(x, y, x + width, y + height, OUTLINE_COLOR);
		graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BACKGROUND_COLOR);
		DraggableList list = (DraggableList) this.list;
		list.rearrange$move(this.dragged, x, y);
		list.rearrange$extractEntry(this.dragged, graphics, a);
		list.rearrange$repositionEntries();
		//? if >=1.21.9 {
		graphics.requestCursor(transfer == null ? CursorTypes.RESIZE_NS : CursorTypes.RESIZE_ALL);
		//?}
	}
}
