package dev.creeperkatze.gather.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import dev.creeperkatze.gather.LanHosting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {
	@Shadow
	@Final
	private MinecraftServer server;
	@Shadow
	@Final
	private Connection connection;

	@Shadow
	public abstract void disconnect(Component component);

	@Inject(method = "handleHello", at = @At("HEAD"), cancellable = true)
	private void gather$protectHostName(ServerboundHelloPacket packet, CallbackInfo ci) {
		if (LanHosting.takesHostName(this.server, this.connection, packet.name())) {
			this.disconnect(Component.translatable("gather.disconnect.host_name"));
			ci.cancel();
		}
	}

	@WrapOperation(method = "handleHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/UUIDUtil;createOfflineProfile(Ljava/lang/String;)Lcom/mojang/authlib/GameProfile;"))
	private GameProfile gather$knownProfile(String name, Operation<GameProfile> original) {
		return LanHosting.offlineProfile(this.server, name, original::call);
	}
}
