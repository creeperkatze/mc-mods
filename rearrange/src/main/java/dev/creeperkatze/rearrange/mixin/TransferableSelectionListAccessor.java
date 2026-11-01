package dev.creeperkatze.rearrange.mixin;

import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TransferableSelectionList.class)
public interface TransferableSelectionListAccessor {
	@Accessor("screen")
	PackSelectionScreen rearrange$getScreen();
}
