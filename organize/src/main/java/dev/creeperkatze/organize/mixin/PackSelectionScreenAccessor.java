package dev.creeperkatze.organize.mixin;

import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;

@Mixin(PackSelectionScreen.class)
public interface PackSelectionScreenAccessor {
	@Accessor("availablePackList")
	@Nullable TransferableSelectionList organize$getAvailablePackList();

	@Accessor("packDir")
	Path organize$getPackDir();

	@Accessor("model")
	PackSelectionModel organize$getModel();

	//? if >=1.21.9 {
	@Invoker("populateLists")
	void organize$populateLists(PackSelectionModel.@Nullable EntryBase transferredEntry);
	//?} else {
	/*@Invoker("populateLists")
	void organize$populateLists();
	*///?}
}
