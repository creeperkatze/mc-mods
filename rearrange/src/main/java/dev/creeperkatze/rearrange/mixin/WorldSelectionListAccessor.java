package dev.creeperkatze.rearrange.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
//? if <1.21.9 {
/*import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(WorldSelectionList.class)
public interface WorldSelectionListAccessor {
	@Accessor("filter")
	String rearrange$getFilter();

	//? if >=1.21.9 {
	@Accessor("entryType")
	WorldSelectionList.EntryType rearrange$getEntryType();
	//?} else {
	/*@Invoker("reloadWorldList")
	void rearrange$reloadWorldList();
	*///?}
}
