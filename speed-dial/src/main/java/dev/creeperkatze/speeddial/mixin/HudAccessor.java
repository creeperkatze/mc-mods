package dev.creeperkatze.speeddial.mixin;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
public interface HudAccessor {
	@Accessor("overlayMessageString")
	Component speedDial$getOverlayMessage();

	@Accessor("overlayMessageTime")
	void speedDial$setOverlayMessageTime(int time);
}
