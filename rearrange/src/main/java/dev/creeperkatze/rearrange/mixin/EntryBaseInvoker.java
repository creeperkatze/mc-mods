package dev.creeperkatze.rearrange.mixin;

import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(PackSelectionModel.EntryBase.class)
public interface EntryBaseInvoker {
	@Invoker("getSelfList")
	List<Pack> rearrange$getSelfList();

	@Invoker("getOtherList")
	List<Pack> rearrange$getOtherList();

	@Invoker("move")
	void rearrange$move(int direction);
}
