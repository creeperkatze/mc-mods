package dev.creeperkatze.rearrange.mixin;

import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PackSelectionScreen.class)
public interface PackSelectionScreenAccessor {
	@Accessor("availablePackList")
	@Nullable TransferableSelectionList rearrange$getAvailablePackList();

	@Accessor("selectedPackList")
	@Nullable TransferableSelectionList rearrange$getSelectedPackList();
}
