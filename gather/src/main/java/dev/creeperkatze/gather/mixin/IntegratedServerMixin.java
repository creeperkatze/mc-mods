package dev.creeperkatze.gather.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.creeperkatze.gather.HostSettings;
import dev.creeperkatze.gather.HostedServer;
import dev.creeperkatze.gather.LanHosting;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//? if >=26.2 && <26.3 {
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}
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

	//? if >=26.2 {
	@Inject(method = "publishServer(Lnet/minecraft/server/MinecraftServer$MultiplayerScope;I)Z", at = @At("RETURN"))
	//?} else {
	/*@Inject(method = "publishServer", at = @At("RETURN"))
	*///?}
	private void gather$onPublished(CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			LanHosting.rememberPublished((IntegratedServer) (Object) this);
		}
	}

	//? if >=26.2 && <26.3 {
	/*@Inject(method = {"setGameTypeForOtherPlayers", "setCommandsAllowedForOtherPlayers"}, at = @At("RETURN"))
	private void gather$onGuestSettingsChanged(CallbackInfo ci) {
		LanHosting.rememberPublished((IntegratedServer) (Object) this);
	}
	*///?}
}
