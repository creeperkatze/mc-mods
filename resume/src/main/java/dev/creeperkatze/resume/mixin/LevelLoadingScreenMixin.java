package dev.creeperkatze.resume.mixin;

import dev.creeperkatze.resume.LoadingBackground;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenMixin {
	@Shadow
	private LevelLoadingScreen.Reason reason;

	@Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
	private void resume$extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		// Portals keep their own background
		if (reason == LevelLoadingScreen.Reason.OTHER
				&& LoadingBackground.extract((LevelLoadingScreen) (Object) this, graphics)) {
			ci.cancel();
		}
	}
}
