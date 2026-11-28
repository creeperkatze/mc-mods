package dev.creeperkatze.exchange.mixin;

import dev.creeperkatze.exchange.CopyFlow;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin extends Screen {
	@Shadow
	@Final
	private LinearLayout layout;

	private EditWorldScreenMixin(Component title) {
		super(title);
	}

	// Below Optimize World, before the spacer above Save and Cancel
	@Inject(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/gui/layouts/SpacerElement", ordinal = 1))
	private void exchange$addCopyButton(Minecraft minecraft, LevelStorageSource.LevelStorageAccess levelAccess, String name, BooleanConsumer callback, CallbackInfo ci) {
		this.layout.addChild(CopyFlow.editScreenButton(this, levelAccess));
	}
}
