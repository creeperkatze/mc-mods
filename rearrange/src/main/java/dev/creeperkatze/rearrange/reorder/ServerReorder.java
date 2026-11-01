package dev.creeperkatze.rearrange.reorder;

import dev.creeperkatze.rearrange.Reorder;
import dev.creeperkatze.rearrange.mixin.ServerListAccessor;
import dev.creeperkatze.rearrange.mixin.ServerSelectionListAccessor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerList;
import org.jspecify.annotations.Nullable;

public record ServerReorder(ServerSelectionList list) implements Reorder {
	@Override
	public @Nullable Move pickUp(int index) {
		if (!(this.list.children().get(index) instanceof ServerSelectionList.OnlineServerEntry)) {
			return null;
		}
		return new ServerMove(this.list, index);
	}

	private record ServerMove(ServerSelectionList list, int from) implements Move {
		private ServerList servers() {
			return ((ServerSelectionListAccessor) this.list).rearrange$getScreen().getServers();
		}

		// Saved servers come first, followed by the LAN servers
		@Override
		public boolean canDrop(int index) {
			return index < this.servers().size();
		}

		@Override
		public void drop(int index) {
			ServerList servers = this.servers();
			Reorder.move(((ServerListAccessor) servers).rearrange$getServerList(), this.from, index);
			servers.save();
			Reorder.move(((ServerSelectionListAccessor) this.list).rearrange$getOnlineServers(), this.from, index);
		}
	}
}
