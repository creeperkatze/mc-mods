package dev.creeperkatze.gather.mixin;

import dev.creeperkatze.gather.LanHosting;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Keeps changes made with /whitelist and /setidletimeout for the next time the world loads
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(method = "setUsingWhitelist", at = @At("RETURN"))
	private void gather$saveWhitelist(boolean usingWhitelist, CallbackInfo ci) {
		LanHosting.onWhitelistChanged((MinecraftServer) (Object) this, usingWhitelist);
	}

	@Inject(method = "setPlayerIdleTimeout", at = @At("RETURN"))
	private void gather$saveIdleTimeout(int playerIdleTimeout, CallbackInfo ci) {
		LanHosting.onIdleTimeoutChanged((MinecraftServer) (Object) this, playerIdleTimeout);
	}
}
