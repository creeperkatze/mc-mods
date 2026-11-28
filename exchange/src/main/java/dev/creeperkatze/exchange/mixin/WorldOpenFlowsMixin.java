package dev.creeperkatze.exchange.mixin;

import dev.creeperkatze.exchange.CopyFlow;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {
	// The last step before the world loads, after every check and with the world's registries ready
	@Inject(method = "openWorldDoLoad", at = @At("HEAD"), cancellable = true)
	private void exchange$offerCopy(LevelStorageSource.LevelStorageAccess worldAccess, WorldStem worldStem, PackRepository packRepository, CallbackInfo ci) {
		WorldOpenFlowsInvoker flows = (WorldOpenFlowsInvoker) (Object) this;
		if (CopyFlow.beforeWorldLoad(worldAccess, worldStem, () -> flows.exchange$openWorldDoLoad(worldAccess, worldStem, packRepository))) {
			ci.cancel();
		}
	}
}
