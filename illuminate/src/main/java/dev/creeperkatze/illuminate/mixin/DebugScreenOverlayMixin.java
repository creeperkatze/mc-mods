package dev.creeperkatze.illuminate.mixin;

//? if <1.21.9 {
/*import dev.creeperkatze.illuminate.preview.IlluminateDebugEntry;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
*///?}
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;

// Before debug screen entries, Illuminate's lines go below the game information on the left
@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
	//? if <1.21.9 {
	/*@Inject(method = "getGameInformation", at = @At("RETURN"))
	private void illuminate$addLines(CallbackInfoReturnable<List<String>> cir) {
		cir.getReturnValue().add("");
		cir.getReturnValue().addAll(IlluminateDebugEntry.lines());
	}
	*///?}
}
