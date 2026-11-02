package dev.creeperkatze.gather.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.creeperkatze.gather.LanHosting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserWhiteList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	// Keeps changes made with /whitelist and /setidletimeout for the next time the world loads
	@Inject(method = "setUsingWhitelist", at = @At("RETURN"))
	private void gather$saveWhitelist(boolean usingWhitelist, CallbackInfo ci) {
		LanHosting.onWhitelistChanged((MinecraftServer) (Object) this, usingWhitelist);
	}

	@Inject(method = "setPlayerIdleTimeout", at = @At("RETURN"))
	private void gather$saveIdleTimeout(int playerIdleTimeout, CallbackInfo ci) {
		LanHosting.onIdleTimeoutChanged((MinecraftServer) (Object) this, playerIdleTimeout);
	}

	// Turning the whitelist on checks the list itself, which never has the host on it
	@WrapOperation(method = "kickUnlistedPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/UserWhiteList;isWhiteListed(Lnet/minecraft/server/players/NameAndId;)Z"))
	private boolean gather$keepHost(UserWhiteList whitelist, NameAndId player, Operation<Boolean> original) {
		return original.call(whitelist, player) || LanHosting.isHost((MinecraftServer) (Object) this, player);
	}
}
