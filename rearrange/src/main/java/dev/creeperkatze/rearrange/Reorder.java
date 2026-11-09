package dev.creeperkatze.rearrange;

import dev.creeperkatze.rearrange.reorder.PackReorder;
import dev.creeperkatze.rearrange.reorder.ServerReorder;
import dev.creeperkatze.rearrange.reorder.WorldReorder;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface Reorder {
	// Returns how the entry at this index can move, or null if it can't be picked up
	@Nullable Move pickUp(int index);

	// Identifies an entry for the animation, also after the list rebuilt its entries
	default Object key(LayoutElement entry) {
		return entry;
	}

	interface Move {
		boolean canDrop(int index);

		void drop(int index);

		// Another list the entry can be dropped into
		default @Nullable Transfer transfer() {
			return null;
		}
	}

	interface Transfer {
		AbstractSelectionList<?> list();

		boolean canDrop(int index);

		void drop(int index);
	}

	static @Nullable Reorder of(AbstractSelectionList<?> list) {
		if (list instanceof ServerSelectionList servers) {
			return new ServerReorder(servers);
		}
		if (list instanceof WorldSelectionList worlds) {
			return new WorldReorder(worlds);
		}
		if (list instanceof TransferableSelectionList packs) {
			return new PackReorder(packs);
		}
		return null;
	}

	static <T> void move(List<T> list, int from, int to) {
		list.add(to, list.remove(from));
	}
}
