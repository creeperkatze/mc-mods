package dev.creeperkatze.rearrange.mixin;

import net.minecraft.client.MouseHandler;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonInfo;
import org.jspecify.annotations.Nullable;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
	//? if >=1.21.9 {
	@Accessor("activeButton")
	@Nullable MouseButtonInfo rearrange$getActiveButton();
	//?} else {
	/*// -1 while no button is down
	@Accessor("activeButton")
	int rearrange$getActiveButton();
	*///?}
}
