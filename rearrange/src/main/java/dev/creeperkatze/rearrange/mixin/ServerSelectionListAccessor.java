package dev.creeperkatze.rearrange.mixin;

import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ServerSelectionList.class)
public interface ServerSelectionListAccessor {
	@Accessor("screen")
	JoinMultiplayerScreen rearrange$getScreen();

	@Accessor("onlineServers")
	List<ServerSelectionList.OnlineServerEntry> rearrange$getOnlineServers();
}
