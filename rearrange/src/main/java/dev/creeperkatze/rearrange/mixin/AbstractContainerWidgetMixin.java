package dev.creeperkatze.rearrange.mixin;

import dev.creeperkatze.rearrange.ListDrag;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerWidget.class)
public class AbstractContainerWidgetMixin {
	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void rearrange$press(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		ListDrag drag = ListDrag.of(this);
		if (drag != null) {
			drag.press(event);
		}
	}

	@Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
	private void rearrange$drag(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
		ListDrag drag = ListDrag.of(this);
		if (drag != null && drag.drag(event)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"))
	private void rearrange$release(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
		ListDrag drag = ListDrag.of(this);
		if (drag != null) {
			drag.release();
		}
	}
}
