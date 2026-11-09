package dev.creeperkatze.illuminate.mixin;

import dev.creeperkatze.illuminate.preview.LightingPreview;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public class HudMixin {
	//? if >=26.1 {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	//?} else {
	/*@Inject(method = "render", at = @At("TAIL"))
	*///?}
	private void illuminate$extractPreview(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		LightingPreview.extract(graphics);
	}
}
