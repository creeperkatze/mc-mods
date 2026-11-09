package dev.creeperkatze.accelerate.mixin;

import dev.creeperkatze.accelerate.FlightSpeed;
//? if >=26.1 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

//? if >=26.1 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public class HudMixin {
	@Shadow
	private Component overlayMessageString;

	@ModifyVariable(method = "extractOverlayMessage", at = @At("STORE"), ordinal = 0)
	private int accelerate$noFade(int alpha) {
		return alpha > 0 && this.overlayMessageString == FlightSpeed.lastMessage() ? 255 : alpha;
	}
}
