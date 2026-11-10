package dev.creeperkatze.resume.mixin;

import dev.creeperkatze.resume.LoadingBackground;
//? if >=26.2 {
import net.minecraft.client.gui.Gui;
//?} else {
/*import net.minecraft.client.Minecraft;
*///?}
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(Gui.class)
//?} else {
/*@Mixin(Minecraft.class)
*///?}
public class GuiMixin {
	@Shadow
	//? if >=26.2 {
	private @Nullable Screen screen;
	//?} else {
	/*public @Nullable Screen screen;
	*///?}

	@Inject(method = "setScreen", at = @At("HEAD"))
	private void resume$onScreenChange(@Nullable Screen next, CallbackInfo ci) {
		LoadingBackground.onScreenChange(screen, next);
	}
}
