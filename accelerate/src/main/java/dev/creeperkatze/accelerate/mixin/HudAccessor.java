package dev.creeperkatze.accelerate.mixin;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
public interface HudAccessor {
	@Accessor("overlayMessageString")
	Component accelerate$getOverlayMessage();

	@Accessor("overlayMessageTime")
	void accelerate$setOverlayMessageTime(int time);
}
