package dev.creeperkatze.rearrange;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;

import java.util.List;

// Implemented by every selection list through AbstractSelectionListMixin
public interface DraggableList {
	List<Object> rearrange$entries();

	// The space an entry takes up in the list, including its padding
	ScreenRectangle rearrange$bounds(Object entry);

	// Draws an entry somewhere else until the entries are repositioned
	void rearrange$move(Object entry, int x, int y);

	void rearrange$repositionEntries();

	// Draws an entry where it is, without any hover effects
	void rearrange$extractEntry(Object entry, GuiGraphicsExtractor graphics, float a);

	boolean rearrange$isOverScrollbar(double x, double y);

	@Nullable ListDrag rearrange$drag();
}
