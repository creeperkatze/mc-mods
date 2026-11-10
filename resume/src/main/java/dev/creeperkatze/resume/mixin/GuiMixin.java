package dev.creeperkatze.resume.mixin;

import dev.creeperkatze.resume.LoadingBackground;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Shadow
	private @Nullable Screen screen;

	@Inject(method = "setScreen", at = @At("HEAD"))
	private void resume$onScreenChange(@Nullable Screen next, CallbackInfo ci) {
		LoadingBackground.onScreenChange(screen, next);
	}
}
