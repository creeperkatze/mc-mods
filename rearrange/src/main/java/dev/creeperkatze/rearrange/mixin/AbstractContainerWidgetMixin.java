package dev.creeperkatze.rearrange.mixin;

//? if >=1.21.9 {
import dev.creeperkatze.rearrange.ListDrag;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?}
import net.minecraft.client.gui.components.AbstractContainerWidget;
import org.spongepowered.asm.mixin.Mixin;

// Before 1.21.9 the selection lists handle mouse buttons themselves, see AbstractSelectionListMixin
@Mixin(AbstractContainerWidget.class)
public class AbstractContainerWidgetMixin {
	//? if >=1.21.9 {
	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void rearrange$press(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		ListDrag drag = ListDrag.of(this);
		if (drag != null) {
			drag.press(event.x(), event.y(), event.button());
		}
	}

	@Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
	private void rearrange$drag(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
		ListDrag drag = ListDrag.of(this);
		if (drag != null && drag.drag(event.x(), event.y())) {
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
	//?}
}
