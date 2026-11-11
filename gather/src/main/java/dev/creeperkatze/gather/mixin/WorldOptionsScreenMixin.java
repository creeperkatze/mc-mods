package dev.creeperkatze.gather.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.creeperkatze.gather.HostOptions;
import dev.creeperkatze.gather.HostOptionsScreen;
import dev.creeperkatze.gather.LanHosting;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
//? if >=26.3 {
import net.minecraft.client.gui.screens.WorldOptionsScreen;
//?} else {
/*import net.minecraft.client.gui.screens.MultiplayerOptionsScreen;
*///?}
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The screen that opens the world to LAN
//? if >=26.3 {
@Mixin(WorldOptionsScreen.class)
//?} else {
/*@Mixin(MultiplayerOptionsScreen.class)
*///?}
public abstract class WorldOptionsScreenMixin extends Screen {
	private WorldOptionsScreenMixin(Component title) {
		super(title);
	}

	@Shadow
	private int port;

	@ModifyExpressionValue(method = "publish", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"))
	private MutableComponent gather$publishedMessage(MutableComponent original) {
		return LanHosting.publishedMessage(this.port).orElse(original);
	}

	//? if >=26.3 {
	@Unique
	private GridLayout.@Nullable RowHelper gather$rows;
	@Unique
	private @Nullable HostOptions gather$options;

	@Shadow
	private void updateApplyChangesActiveState() {
		throw new AssertionError();
	}

	@ModifyExpressionValue(method = "multiplayerOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;createRowHelper(I)Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;"))
	private GridLayout.RowHelper gather$keepRows(GridLayout.RowHelper rows) {
		this.gather$rows = rows;
		return rows;
	}

	@Inject(method = "multiplayerOptions", at = @At("TAIL"))
	private void gather$addOptions(LinearLayout content, IntegratedServer server, CallbackInfo ci) {
		if (this.gather$rows != null) {
			this.gather$options = new HostOptions(server, this::updateApplyChangesActiveState);
			this.gather$options.addTo(this.gather$rows);
		}
	}

	@ModifyReturnValue(method = "hasSettingsChanges", at = @At("RETURN"))
	private boolean gather$hasChanges(boolean original) {
		return original || this.gather$options != null && this.gather$options.changed();
	}

	// Before vanilla publishes, so the world opens with the new settings
	@Inject(method = "applyChanges", at = @At("HEAD"))
	private void gather$apply(@Nullable IntegratedServer server, CallbackInfo ci) {
		if (this.gather$options != null) {
			this.gather$options.apply();
		}
	}

	// Guest commands and the forced game mode can change without opening the world again
	@Inject(method = "applyChanges", at = @At("TAIL"))
	private void gather$rememberPublished(@Nullable IntegratedServer server, CallbackInfo ci) {
		if (server != null) {
			LanHosting.rememberPublished(server);
		}
	}
	//?} else {
	/*@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;visitWidgets(Ljava/util/function/Consumer;)V"))
	private void gather$addHostSettings(CallbackInfo ci, @Local(ordinal = 0) LinearLayout content, @Local IntegratedServer server) {
		content.addChild(HostOptionsScreen.openButton(this, server).build());
	}
	*///?}
}
