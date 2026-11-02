package dev.creeperkatze.gather.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.creeperkatze.gather.LanHosting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	// The host is never kicked for being idle in their own world
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;playerIdleTimeout()I"))
	private int gather$noIdleKickForHost(int idleTimeout) {
		return LanHosting.isHost(this.player.level().getServer(), this.player.getGameProfile()) ? 0 : idleTimeout;
	}
}
