package dev.creeperkatze.rearrange.reorder;

import dev.creeperkatze.rearrange.Reorder;
import dev.creeperkatze.rearrange.mixin.EntryBaseInvoker;
import dev.creeperkatze.rearrange.mixin.PackEntryAccessor;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.server.packs.repository.Pack;
import org.jspecify.annotations.Nullable;

import java.util.List;

// The first entry of a pack list is its header, so pack i is at index i + 1
public record PackReorder(TransferableSelectionList list) implements Reorder {
	@Override
	public @Nullable Move pickUp(int index) {
		if (!(this.list.children().get(index) instanceof TransferableSelectionList.PackEntry entry)
				|| !(((PackEntryAccessor) entry).rearrange$getPack() instanceof PackSelectionModel.EntryBase pack)
				|| !pack.isSelected()
				|| pack.isFixedPosition()) {
			return null;
		}
		List<Pack> packs = ((EntryBaseInvoker) pack).rearrange$getSelfList();
		// The search box hides packs from the list
		if (this.list.children().size() - 1 != packs.size()) {
			return null;
		}
		return new PackMove(pack, packs, index);
	}

	private record PackMove(PackSelectionModel.EntryBase pack, List<Pack> packs, int from) implements Move {
		// Packs with a fixed position can't be moved past
		@Override
		public boolean canDrop(int index) {
			if (index < 1) {
				return false;
			}
			for (int i = Math.min(this.from, index); i <= Math.max(this.from, index); i++) {
				if (i != this.from && this.packs.get(i - 1).isFixedPosition()) {
					return false;
				}
			}
			return true;
		}

		@Override
		public void drop(int index) {
			((EntryBaseInvoker) this.pack).rearrange$move(index - this.from);
		}
	}
}
