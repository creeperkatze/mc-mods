package dev.creeperkatze.resume.mixin;

import dev.creeperkatze.resume.LoadingBackground;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void resume$extractFade(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		LoadingBackground.extractFade(graphics);
	}
}
