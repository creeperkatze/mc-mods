package dev.creeperkatze.gather.mixin;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.server.LanServerPinger;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IntegratedServer.class)
public interface IntegratedServerAccessor {
	@Accessor("lanPinger")
	@Nullable LanServerPinger gather$getLanPinger();

	@Accessor("lanPinger")
	void gather$setLanPinger(@Nullable LanServerPinger pinger);
}
