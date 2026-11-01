package dev.creeperkatze.speeddial.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.creeperkatze.speeddial.FlightSpeed;
import dev.creeperkatze.speeddial.SpeedDial;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlyingSpeed()F"))
	private float speedDial$verticalSpeed(Abilities abilities, Operation<Float> original) {
		float speed = original.call(abilities);
		LocalPlayer player = (LocalPlayer) (Object) this;
		if (!SpeedDial.config().matchVerticalSpeed || !FlightSpeed.isActive(player)) {
			return speed;
		}
		return speed * FlightSpeed.VERTICAL_FACTOR * (player.isSprinting() ? 2.0F : 1.0F);
	}
}
