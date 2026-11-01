package dev.creeperkatze.speeddial.mixin;

import dev.creeperkatze.speeddial.FlightSpeed;
import dev.creeperkatze.speeddial.SpeedDial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
	private void speedDial$resetSpeed(CallbackInfo ci) {
		LocalPlayer player = ((Minecraft) (Object) this).player;
		if (SpeedDial.config().pickBlockResetsSpeed && FlightSpeed.isActive(player) && player.isSpectator()) {
			FlightSpeed.reset(player);
			ci.cancel();
		}
	}
}
