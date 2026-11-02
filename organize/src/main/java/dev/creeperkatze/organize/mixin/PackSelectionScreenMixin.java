package dev.creeperkatze.organize.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.creeperkatze.organize.PackFolderScreen;
import dev.creeperkatze.organize.PackFolders;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin(PackSelectionScreen.class)
public class PackSelectionScreenMixin implements PackFolderScreen {
	@Unique
	private String organize$folder = "";

	@Override
	public String organize$getFolder() {
		return this.organize$folder;
	}

	@Override
	public void organize$setFolder(String folder) {
		this.organize$folder = folder;
	}

	@WrapOperation(method = "filterEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/packs/TransferableSelectionList;updateList(Ljava/util/stream/Stream;Lnet/minecraft/client/gui/screens/packs/PackSelectionModel$EntryBase;)V"))
	private void organize$showFolder(TransferableSelectionList list, Stream<PackSelectionModel.Entry> entries, PackSelectionModel.@Nullable EntryBase transferredEntry, Operation<Void> original, @Local(argsOnly = true) String search) {
		PackFolders.show((PackSelectionScreen) (Object) this, list, search, entries, shown -> original.call(list, shown, transferredEntry));
	}
}
