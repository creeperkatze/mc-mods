package dev.creeperkatze.rearrange.mixin;

import dev.creeperkatze.rearrange.ResetWorldOrderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
	@Shadow
	protected @Nullable EditBox searchBox;
	@Shadow
	private @Nullable WorldSelectionList list;

	@Unique
	private @Nullable ResetWorldOrderButton rearrange$resetButton;

	private SelectWorldScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void rearrange$addResetButton(CallbackInfo ci) {
		this.rearrange$resetButton = this.addRenderableWidget(new ResetWorldOrderButton(this, () -> this.list));
		this.rearrange$resetButton.position(this.searchBox);
	}

	@Inject(method = "repositionElements", at = @At("TAIL"))
	private void rearrange$positionResetButton(CallbackInfo ci) {
		if (this.rearrange$resetButton != null) {
			this.rearrange$resetButton.position(this.searchBox);
		}
	}
}
