package dev.creeperkatze.resume.mixin;

import dev.creeperkatze.resume.Place;
import dev.creeperkatze.resume.Resume;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
	@Inject(method = "startConnecting", at = @At("HEAD"))
	private static void resume$joinServer(Screen parent, Minecraft minecraft, ServerAddress hostAndPort, ServerData data,
			boolean isQuickPlay, @Nullable TransferState transferState, CallbackInfo ci) {
		Resume.onJoin(Place.server(data));
	}
}
