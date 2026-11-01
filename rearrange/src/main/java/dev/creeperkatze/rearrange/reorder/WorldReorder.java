package dev.creeperkatze.rearrange.reorder;

import dev.creeperkatze.rearrange.Reorder;
import dev.creeperkatze.rearrange.WorldOrder;
import dev.creeperkatze.rearrange.mixin.WorldSelectionListAccessor;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.jspecify.annotations.Nullable;

public record WorldReorder(WorldSelectionList list) implements Reorder {
	@Override
	public @Nullable Move pickUp(int index) {
		// The order is only saved from the full singleplayer list, not a filtered one
		WorldSelectionListAccessor accessor = (WorldSelectionListAccessor) this.list;
		if (accessor.rearrange$getEntryType() != WorldSelectionList.EntryType.SINGLEPLAYER || !accessor.rearrange$getFilter().isEmpty()) {
			return null;
		}
		if (!this.list.children().stream().allMatch(WorldSelectionList.WorldListEntry.class::isInstance)) {
			return null;
		}
		return new WorldMove(this.list);
	}

	private record WorldMove(WorldSelectionList list) implements Move {
		@Override
		public boolean canDrop(int index) {
			return true;
		}

		@Override
		public void drop(int index) {
			WorldOrder.save(this.list.children().stream().map(entry -> entry.getLevelSummary().getLevelId()).toList());
		}
	}
}
