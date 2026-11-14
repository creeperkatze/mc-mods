package dev.creeperkatze.accelerate.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.creeperkatze.accelerate.FlightSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
//? if <1.21.2 {
/*import net.minecraft.world.entity.player.Inventory;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
	private float accelerate$scrollSpeed(float value, float min, float max, Operation<Float> original) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (!FlightSpeed.isActive(player)) {
			return original.call(value, min, max);
		}
		float current = player.getAbilities().getFlyingSpeed();
		int steps = Math.round((value - current) / FlightSpeed.VANILLA_STEP);
		float speed = FlightSpeed.scroll(current, steps);
		FlightSpeed.onChanged(speed);
		return speed;
	}

	//? if >=1.21.2 {
	@WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I"))
	private int accelerate$creativeScrollSpeed(double wheel, int currentSlot, int slotCount, Operation<Integer> original) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (!FlightSpeed.canChange(player)) {
			return original.call(wheel, currentSlot, slotCount);
		}
		FlightSpeed.updateCreativeBoost(player);
		FlightSpeed.change(player, (int) wheel);
		return currentSlot;
	}
	//?} else {
	/*@WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"))
	private void accelerate$creativeScrollSpeed(Inventory inventory, double wheel, Operation<Void> original) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (!FlightSpeed.canChange(player)) {
			original.call(inventory, wheel);
			return;
		}
		FlightSpeed.updateCreativeBoost(player);
		FlightSpeed.change(player, (int) wheel);
	}
	*///?}
}
