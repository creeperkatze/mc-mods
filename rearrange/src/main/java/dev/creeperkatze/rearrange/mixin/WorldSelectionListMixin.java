package dev.creeperkatze.rearrange.mixin;

//? if <1.21.9 {
/*import com.mojang.blaze3d.platform.InputConstants;
import dev.creeperkatze.rearrange.ListDrag;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}
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

	//? if <1.21.9 {
	/*// Vanilla moves servers and selected packs with Shift and the arrow keys, this does the same for worlds
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void rearrange$moveWorld(int key, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (!Screen.hasShiftDown() || key != InputConstants.KEY_UP && key != InputConstants.KEY_DOWN) {
			return;
		}
		WorldSelectionList list = (WorldSelectionList) (Object) this;
		WorldSelectionList.Entry selected = list.getSelected();
		ListDrag drag = ListDrag.of(list);
		if (selected != null && drag != null && drag.step(selected, key == InputConstants.KEY_UP ? -1 : 1)) {
			cir.setReturnValue(true);
		}
	}
	*///?}
}
