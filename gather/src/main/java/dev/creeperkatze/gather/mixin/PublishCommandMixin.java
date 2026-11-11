package dev.creeperkatze.gather.mixin;

import dev.creeperkatze.gather.LanHosting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.PublishCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PublishCommand.class)
public class PublishCommandMixin {
	@Inject(method = "getSuccessMessage", at = @At("HEAD"), cancellable = true)
	private static void gather$publishedMessage(int port, CallbackInfoReturnable<MutableComponent> cir) {
		LanHosting.publishedMessage(port).ifPresent(cir::setReturnValue);
	}
}
