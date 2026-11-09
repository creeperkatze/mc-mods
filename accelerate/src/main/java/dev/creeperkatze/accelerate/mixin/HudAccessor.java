package dev.creeperkatze.accelerate.mixin;

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public interface HudAccessor {
	@Accessor("overlayMessageString")
	Component accelerate$getOverlayMessage();

	@Accessor("overlayMessageTime")
	void accelerate$setOverlayMessageTime(int time);
}
