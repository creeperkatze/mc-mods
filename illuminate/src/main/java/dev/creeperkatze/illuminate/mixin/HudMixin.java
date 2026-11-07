package dev.creeperkatze.illuminate.mixin;

import dev.creeperkatze.illuminate.preview.LightingPreview;
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
	private void illuminate$extractPreview(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		LightingPreview.extract(graphics);
	}
}
