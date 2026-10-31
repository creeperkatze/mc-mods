package dev.creeperkatze.speeddial.mixin;

import dev.creeperkatze.speeddial.SpeedDial;
import dev.creeperkatze.speeddial.FlightSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	/** Pick Block does nothing in spectator, so it can reset the speed instead. */
	@Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
	private void speedDial$resetSpeed(CallbackInfo ci) {
		LocalPlayer player = ((Minecraft) (Object) this).player;
		if (SpeedDial.config().pickBlockResetsSpeed && FlightSpeed.isActive(player) && player.isSpectator()) {
			FlightSpeed.reset(player);
			ci.cancel();
		}
	}
}
