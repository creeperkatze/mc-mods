package dev.creeperkatze.illuminate.mixin;

import dev.creeperkatze.illuminate.game.GameEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	private static final byte TOTEM_EVENT = 35;

	@Inject(method = "handleDamageEvent", at = @At("TAIL"))
	private void illuminate$onDamage(ClientboundDamageEventPacket packet, CallbackInfo ci) {
		GameEvents.onDamage(packet);
	}

	@Inject(method = "handleExplosion", at = @At("TAIL"))
	private void illuminate$onExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
		GameEvents.onExplosion(packet.center(), packet.radius());
	}

	@Inject(method = "handleEntityEvent", at = @At("TAIL"))
	private void illuminate$onEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (packet.getEventId() == TOTEM_EVENT && client.level != null && packet.getEntity(client.level) == client.player) {
			GameEvents.onTotem();
		}
	}
}
