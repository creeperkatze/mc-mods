package dev.creeperkatze.rearrange.mixin;

import dev.creeperkatze.rearrange.ListDrag;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Vanilla moves servers and selected packs with Shift and the arrow keys, this does the same for worlds
@Mixin(WorldSelectionList.WorldListEntry.class)
public class WorldListEntryMixin {
	@Shadow
	@Final
	private WorldSelectionList list;

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void rearrange$moveWorld(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!event.hasShiftDown() || !event.isUp() && !event.isDown()) {
			return;
		}
		ListDrag drag = ListDrag.of(this.list);
		if (drag != null && drag.step((LayoutElement) this, event.isUp() ? -1 : 1)) {
			cir.setReturnValue(true);
		}
	}
}
