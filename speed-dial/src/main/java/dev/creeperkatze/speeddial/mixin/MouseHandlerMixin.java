package dev.creeperkatze.speeddial.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.creeperkatze.speeddial.FlightSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
	private float speedDial$scrollSpeed(float value, float min, float max, Operation<Float> original) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (!FlightSpeed.isActive(player)) {
			return original.call(value, min, max);
		}
		float current = player.getAbilities().getFlyingSpeed();
		int steps = Math.round((value - current) / FlightSpeed.VANILLA_STEP);
		float speed = FlightSpeed.scroll(current, steps);
		FlightSpeed.onChanged(current, speed);
		return speed;
	}

	@WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I"))
	private int speedDial$creativeScrollSpeed(double wheel, int currentSlot, int slotCount, Operation<Integer> original) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (!FlightSpeed.canChange(player)) {
			return original.call(wheel, currentSlot, slotCount);
		}
		FlightSpeed.updateCreativeBoost(player);
		FlightSpeed.change(player, (int) wheel);
		return currentSlot;
	}
}
