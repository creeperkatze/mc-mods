package dev.creeperkatze.rearrange;

import net.minecraft.client.gui.layouts.LayoutElement;
import org.jspecify.annotations.Nullable;

import java.util.List;

// Implemented by every selection list through AbstractSelectionListMixin
public interface DraggableList {
	List<LayoutElement> rearrange$entries();

	void rearrange$repositionEntries();

	boolean rearrange$isOverScrollbar(double x, double y);

	@Nullable ListDrag rearrange$drag();
}
