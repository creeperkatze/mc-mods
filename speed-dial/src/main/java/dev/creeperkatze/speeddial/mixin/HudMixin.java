package dev.creeperkatze.speeddial.mixin;

import dev.creeperkatze.speeddial.FlightSpeed;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Hud.class)
public class HudMixin {
	@Shadow
	private Component overlayMessageString;

	/** Keeps the speed message fully visible until it disappears instead of fading out. */
	@ModifyVariable(method = "extractOverlayMessage", at = @At("STORE"), ordinal = 0)
	private int speedDial$noFade(int alpha) {
		return alpha > 0 && this.overlayMessageString == FlightSpeed.lastMessage() ? 255 : alpha;
	}
}
