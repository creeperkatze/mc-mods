package dev.creeperkatze.resume.mixin;

import dev.creeperkatze.resume.Screenshots;
//? if <26.2 {
/*import net.minecraft.client.Minecraft;
*///?}
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	// Right after the world is drawn and before the HUD and screens, where vanilla takes the world icon
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;tryTakeScreenshotIfNeeded()V", shift = At.Shift.AFTER))
	private void resume$captureWorld(CallbackInfo ci) {
		//? if >=26.2 {
		Screenshots.onWorldRendered(((GameRenderer) (Object) this).mainRenderTarget());
		//?} else {
		/*Screenshots.onWorldRendered(Minecraft.getInstance().getMainRenderTarget());
		*///?}
	}
}
