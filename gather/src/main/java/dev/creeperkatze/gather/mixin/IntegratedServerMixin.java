package dev.creeperkatze.gather.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.creeperkatze.gather.HostSettings;
import dev.creeperkatze.gather.HostedServer;
import dev.creeperkatze.gather.LanHosting;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin implements HostedServer {
	@Unique
	private HostSettings gather$settings = HostSettings.DEFAULT;

	@Override
	public HostSettings gather$settings() {
		return this.gather$settings;
	}

	@Override
	public void gather$setSettings(HostSettings settings) {
		this.gather$settings = settings;
	}

	@Inject(method = "initServer", at = @At("RETURN"))
	private void gather$loadSettings(CallbackInfoReturnable<Boolean> cir) {
		LanHosting.start((IntegratedServer) (Object) this);
	}

	@ModifyReturnValue(method = "getMaxPlayers", at = @At("RETURN"))
	private int gather$maxPlayers(int original) {
		return this.gather$settings.maxPlayers();
	}

	@Inject(method = "publishServer(Lnet/minecraft/server/MinecraftServer$MultiplayerScope;I)Z", at = @At("RETURN"))
	private void gather$onPublished(MinecraftServer.MultiplayerScope scope, int port, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			LanHosting.onPublished((IntegratedServer) (Object) this, port);
		}
	}
}
