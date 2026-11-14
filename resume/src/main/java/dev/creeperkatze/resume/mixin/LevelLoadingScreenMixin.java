package dev.creeperkatze.resume.mixin;

import dev.creeperkatze.resume.LoadingBackground;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9 {
import net.minecraft.client.gui.screens.LevelLoadingScreen;
//?} else {
/*import net.minecraft.client.gui.screens.ReceivingLevelScreen;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Before 1.21.9 the screen with the portal backgrounds is ReceivingLevelScreen
//? if >=1.21.9 {
@Mixin(LevelLoadingScreen.class)
//?} else {
/*@Mixin(ReceivingLevelScreen.class)
*///?}
public class LevelLoadingScreenMixin {
	@Shadow
	//? if >=1.21.9 {
	private LevelLoadingScreen.Reason reason;
	//?} else {
	/*private ReceivingLevelScreen.Reason reason;
	*///?}

	@Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
	private void resume$extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		// Portals keep their own background
		//? if >=1.21.9 {
		boolean portal = reason != LevelLoadingScreen.Reason.OTHER;
		//?} else {
		/*boolean portal = reason != ReceivingLevelScreen.Reason.OTHER;
		*///?}
		if (!portal && LoadingBackground.extract((Screen) (Object) this, graphics)) {
			ci.cancel();
		}
	}
}
