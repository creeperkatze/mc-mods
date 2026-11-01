package dev.creeperkatze.rearrange.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldSelectionList.class)
public interface WorldSelectionListAccessor {
	@Accessor("filter")
	String rearrange$getFilter();

	@Accessor("entryType")
	WorldSelectionList.EntryType rearrange$getEntryType();
}
