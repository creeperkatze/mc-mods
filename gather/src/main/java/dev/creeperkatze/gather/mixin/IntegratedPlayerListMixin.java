package dev.creeperkatze.gather.mixin;

import dev.creeperkatze.gather.LanHosting;
import net.minecraft.client.server.IntegratedPlayerList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(IntegratedPlayerList.class)
public class IntegratedPlayerListMixin {
	// The host always gets into their own world, unless they are in it already
	@Inject(method = "canPlayerLogin", at = @At("HEAD"), cancellable = true)
	private void gather$letHostIn(SocketAddress address, NameAndId player, CallbackInfoReturnable<Component> cir) {
		IntegratedPlayerList list = (IntegratedPlayerList) (Object) this;
		if (LanHosting.isHost(list.getServer(), player) && list.getPlayerByName(player.name()) == null) {
			cir.setReturnValue(null);
		}
	}
}
