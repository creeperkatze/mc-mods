package dev.creeperkatze.organize.mixin;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
//? if >=1.21.9 {
import org.spongepowered.asm.mixin.gen.Invoker;
//?}

import java.util.List;

@Mixin(AbstractSelectionList.class)
public interface AbstractSelectionListAccessor {
	@Accessor("children")
	List<LayoutElement> organize$getChildren();

	//? if >=1.21.9 {
	@Invoker("repositionEntries")
	void organize$repositionEntries();
	//?}
}
