package dev.creeperkatze.speeddial.mixin;

import dev.creeperkatze.speeddial.FlightSpeed;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "handlePlayerAbilities", at = @At("TAIL"))
	private void speedDial$restoreSpeed(ClientboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
		FlightSpeed.onAbilitiesUpdated();
	}
}
