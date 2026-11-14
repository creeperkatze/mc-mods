package dev.creeperkatze.organize.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >=1.21.9 {
import com.llamalad7.mixinextras.sugar.Local;
//?}
import dev.creeperkatze.organize.PackFolderScreen;
import dev.creeperkatze.organize.PackFolders;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
//? if >=1.21.9 {
import org.jspecify.annotations.Nullable;
//?}
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
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

	@ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/packs/PackSelectionScreen;packDir:Ljava/nio/file/Path;", opcode = Opcodes.GETFIELD))
	private Path organize$openFolderDir(Path packDir) {
		return PackFolders.directory(packDir, this.organize$folder);
	}

	//? if >=1.21.9 {
	@WrapOperation(method = "filterEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/packs/TransferableSelectionList;updateList(Ljava/util/stream/Stream;Lnet/minecraft/client/gui/screens/packs/PackSelectionModel$EntryBase;)V"))
	private void organize$showFolder(TransferableSelectionList list, Stream<PackSelectionModel.Entry> entries, PackSelectionModel.@Nullable EntryBase transferredEntry, Operation<Void> original, @Local(argsOnly = true) String search) {
		PackFolders.show((PackSelectionScreen) (Object) this, list, search, entries, shown -> original.call(list, shown, transferredEntry));
	}
	//?} else {
	/*// There is no search box yet
	@WrapOperation(method = "populateLists", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/packs/PackSelectionScreen;updateList(Lnet/minecraft/client/gui/screens/packs/TransferableSelectionList;Ljava/util/stream/Stream;)V"))
	private void organize$showFolder(PackSelectionScreen screen, TransferableSelectionList list, Stream<PackSelectionModel.Entry> entries, Operation<Void> original) {
		PackFolders.show(screen, list, "", entries, shown -> original.call(screen, list, shown));
	}
	*///?}
}
