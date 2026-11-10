package dev.creeperkatze.resume.mixin;

import dev.creeperkatze.resume.LoadingBackground;
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
	private void resume$extractFade(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		LoadingBackground.extractFade(graphics);
	}
}
