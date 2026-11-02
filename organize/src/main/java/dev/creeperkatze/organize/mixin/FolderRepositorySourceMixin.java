package dev.creeperkatze.organize.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.creeperkatze.organize.PackFolders;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.function.BiConsumer;

@Mixin(FolderRepositorySource.class)
public class FolderRepositorySourceMixin {
	@Shadow
	@Final
	private Path folder;
	@Shadow
	@Final
	private PackType packType;

	// Only resource packs, so worlds never need this mod to find their data packs
	@WrapOperation(method = "loadPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/FolderRepositorySource;discoverPacks(Ljava/nio/file/Path;Lnet/minecraft/world/level/validation/DirectoryValidator;Ljava/util/function/BiConsumer;)V"))
	private void organize$discoverFolders(Path folder, DirectoryValidator validator, BiConsumer<Path, Pack.ResourcesSupplier> result, Operation<Void> original) {
		original.call(folder, validator, result);
		if (this.packType == PackType.CLIENT_RESOURCES) {
			PackFolders.discover(folder, subfolder -> original.call(subfolder, validator, result));
		}
	}

	@ModifyReturnValue(method = "createDiscoveredFilePackInfo", at = @At("RETURN"))
	private PackLocationInfo organize$folderId(PackLocationInfo info, @Local(argsOnly = true) Path content) {
		return PackFolders.locationInfo(info, this.folder, content);
	}
}
