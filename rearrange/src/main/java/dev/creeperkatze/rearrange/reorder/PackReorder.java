package dev.creeperkatze.rearrange.reorder;

import dev.creeperkatze.rearrange.Reorder;
import dev.creeperkatze.rearrange.mixin.EntryBaseInvoker;
import dev.creeperkatze.rearrange.mixin.PackEntryAccessor;
import dev.creeperkatze.rearrange.mixin.PackSelectionScreenAccessor;
import dev.creeperkatze.rearrange.mixin.TransferableSelectionListAccessor;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.server.packs.repository.Pack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record PackReorder(TransferableSelectionList list) implements Reorder {
	//? if >=1.21.9 {
	// The first entry of a pack list is its header, so pack i is at index i + 1
	private static final int FIRST_PACK = 1;
	//?} else {
	/*private static final int FIRST_PACK = 0;
	*///?}

	// Vanilla rebuilds the pack lists after every change, so packs are told apart by id
	@Override
	public Object key(Object entry) {
		//? if >=1.21.9 {
		return entry instanceof TransferableSelectionList.Entry pack && !pack.getPackId().isEmpty() ? pack.getPackId() : entry;
		//?} else {
		/*return entry instanceof TransferableSelectionList.PackEntry pack && !pack.getPackId().isEmpty() ? pack.getPackId() : entry;
		*///?}
	}

	@Override
	public @Nullable Move pickUp(int index) {
		if (!(this.list.children().get(index) instanceof TransferableSelectionList.PackEntry entry)
				|| !(((PackEntryAccessor) entry).rearrange$getPack() instanceof PackSelectionModel.Entry pack && pack instanceof EntryBaseInvoker)) {
			return null;
		}
		TransferableSelectionList other = this.otherList();
		if (!pack.isSelected()) {
			return other == null ? null : new AvailableMove(new Select(other, entry, pack));
		}
		if (pack.isFixedPosition() && !pack.canUnselect()) {
			return null;
		}
		List<Pack> packs = ((EntryBaseInvoker) pack).rearrange$getSelfList();
		Transfer unselect = other == null || !pack.canUnselect() ? null : Unselect.of(other, entry, pack);
		return new SelectedMove(pack, packs, index, matchesModel(this.list, packs), unselect);
	}

	private @Nullable TransferableSelectionList otherList() {
		PackSelectionScreenAccessor screen = (PackSelectionScreenAccessor) ((TransferableSelectionListAccessor) this.list).rearrange$getScreen();
		TransferableSelectionList available = screen.rearrange$getAvailablePackList();
		return available == this.list ? screen.rearrange$getSelectedPackList() : available;
	}

	// The search box hides packs from the list, then its entries no longer match the model
	private static boolean matchesModel(TransferableSelectionList list, List<Pack> packs) {
		return list.children().size() - FIRST_PACK == packs.size();
	}

	private record SelectedMove(PackSelectionModel.Entry pack, List<Pack> packs, int from, boolean unfiltered, @Nullable Transfer transfer) implements Move {
		// Packs with a fixed position can't be moved past
		@Override
		public boolean canDrop(int index) {
			if (!this.unfiltered || this.pack.isFixedPosition() || index < FIRST_PACK) {
				return false;
			}
			for (int i = Math.min(this.from, index); i <= Math.max(this.from, index); i++) {
				if (i != this.from && this.packs.get(i - FIRST_PACK).isFixedPosition()) {
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

	private record AvailableMove(Transfer transfer) implements Move {
		@Override
		public boolean canDrop(int index) {
			return false;
		}

		@Override
		public void drop(int index) {
		}
	}

	private record Select(TransferableSelectionList list, TransferableSelectionList.PackEntry entry, PackSelectionModel.Entry pack) implements Transfer {
		// A new pack goes between the packs fixed to the top and bottom of the selected list
		@Override
		public boolean canDrop(int index) {
			List<Pack> selected = ((EntryBaseInvoker) this.pack).rearrange$getOtherList();
			if (index < FIRST_PACK || !matchesModel(this.list, selected)) {
				return false;
			}
			int top = 0;
			while (top < selected.size() && selected.get(top).isFixedPosition()) {
				top++;
			}
			int bottom = selected.size();
			while (bottom > top && selected.get(bottom - 1).isFixedPosition()) {
				bottom--;
			}
			return index - FIRST_PACK >= top && index - FIRST_PACK <= bottom;
		}

		// Vanilla selects the pack at its default position, or asks first for an incompatible pack
		@Override
		public void drop(int index) {
			this.entry.keyboardSelection();
			// The lists were rebuilt, so the pack has a new entry at index i
			//? if >=1.21.9 {
			List<TransferableSelectionList.Entry> children = this.list.children();
			//?} else {
			/*List<TransferableSelectionList.PackEntry> children = this.list.children();
			*///?}
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i) instanceof TransferableSelectionList.PackEntry selected
						&& selected.getPackId().equals(this.pack.getId())
						&& ((PackEntryAccessor) selected).rearrange$getPack() instanceof EntryBaseInvoker selectedPack) {
					selectedPack.rearrange$move(index - i);
					return;
				}
			}
		}
	}

	// Unselected packs have no order of their own, so the pack can only land where vanilla puts it
	private record Unselect(TransferableSelectionList list, TransferableSelectionList.PackEntry entry, int landing) implements Transfer {
		private static @Nullable Unselect of(TransferableSelectionList list, TransferableSelectionList.PackEntry entry, PackSelectionModel.Entry pack) {
			EntryBaseInvoker invoker = (EntryBaseInvoker) pack;
			Pack self = invoker.rearrange$getSelfList().stream().filter(p -> p.getId().equals(pack.getId())).findFirst().orElse(null);
			if (self == null) {
				return null;
			}
			List<Pack> available = new ArrayList<>(invoker.rearrange$getOtherList());
			int index = self.getDefaultPosition().insert(available, self, Pack::selectionConfig, true);
			// The search box and folders can hide packs above it, and the header and folders come first
			//? if >=1.21.9 {
			Set<String> visible = list.children().stream().map(TransferableSelectionList.Entry::getPackId).collect(Collectors.toSet());
			int first = (int) list.children().stream().filter(row -> !(row instanceof TransferableSelectionList.PackEntry)).count();
			//?} else {
			/*// Rows without a pack id aren't packs, like the folders of Organize
			Set<String> visible = list.children().stream().map(TransferableSelectionList.PackEntry::getPackId).collect(Collectors.toSet());
			int first = (int) list.children().stream().filter(row -> row.getPackId().isEmpty()).count();
			*///?}
			int above = (int) available.subList(0, index).stream().filter(p -> visible.contains(p.getId())).count();
			return new Unselect(list, entry, first + above);
		}

		@Override
		public boolean canDrop(int index) {
			return index == this.landing;
		}

		@Override
		public void drop(int index) {
			this.entry.keyboardSelection();
		}
	}
}
