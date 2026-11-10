package dev.creeperkatze.resume.mixin;

import dev.creeperkatze.resume.Place;
import dev.creeperkatze.resume.Resume;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {
	// Earlier than doWorldLoad, so the screenshot already shows while the world data is read
	@Inject(method = "openWorld", at = @At("HEAD"))
	private void resume$joinWorld(String levelId, Runnable onCancel, CallbackInfo ci) {
		Resume.onJoin(Place.world(levelId));
	}
}
