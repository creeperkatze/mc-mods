package dev.creeperkatze.organize.mixin;

import dev.creeperkatze.organize.Organize;
import dev.creeperkatze.organize.PackFolders;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.file.Path;

// Vanilla only watches the pack folder and the folders directly inside it
@Mixin(targets = "net.minecraft.client.gui.screens.packs.PackSelectionScreen$Watcher")
public abstract class PackWatcherMixin {
	@Shadow
	@Final
	private Path packPath;

	@Shadow
	private void watchDir(Path packPath) throws IOException {
		throw new AssertionError();
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void organize$watchFolders(Path packPath, CallbackInfo ci) {
		this.organize$watchFolders();
	}

	// A change can be a new folder, which gets watched from now on
	@Inject(method = "pollForChanges", at = @At("RETURN"))
	private void organize$watchNewFolders(CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			this.organize$watchFolders();
		}
	}

	@Unique
	private void organize$watchFolders() {
		PackFolders.watchFolders(this.packPath, folder -> {
			try {
				this.watchDir(folder);
			} catch (IOException e) {
				Organize.LOGGER.warn("Failed to watch pack folder {} for changes", folder, e);
			}
		});
	}
}
