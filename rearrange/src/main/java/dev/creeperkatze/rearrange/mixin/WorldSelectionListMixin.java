package dev.creeperkatze.rearrange.mixin;

import dev.creeperkatze.rearrange.WorldOrder;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(WorldSelectionList.class)
public class WorldSelectionListMixin {
	@ModifyVariable(method = "fillLevels", at = @At("HEAD"), argsOnly = true)
	private List<LevelSummary> rearrange$sortLevels(List<LevelSummary> levels) {
		return WorldOrder.sort(levels);
	}
}
