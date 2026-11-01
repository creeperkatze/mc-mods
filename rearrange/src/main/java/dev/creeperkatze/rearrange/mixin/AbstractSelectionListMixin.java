package dev.creeperkatze.rearrange.mixin;

import dev.creeperkatze.rearrange.DraggableList;
import dev.creeperkatze.rearrange.ListDrag;
import dev.creeperkatze.rearrange.Reorder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin extends AbstractContainerWidget implements DraggableList {
	@Shadow
	@Final
	private List<LayoutElement> children;

	@Unique
	private boolean rearrange$dragCreated;
	@Unique
	private @Nullable ListDrag rearrange$drag;

	private AbstractSelectionListMixin(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	@Shadow
	private void repositionEntries() {
		throw new AssertionError();
	}

	@Override
	public List<LayoutElement> rearrange$entries() {
		return this.children;
	}

	@Override
	public void rearrange$repositionEntries() {
		this.repositionEntries();
	}

	@Override
	public boolean rearrange$isOverScrollbar(double x, double y) {
		return this.isOverScrollbar(x, y);
	}

	@Override
	public @Nullable ListDrag rearrange$drag() {
		if (!this.rearrange$dragCreated) {
			this.rearrange$dragCreated = true;
			AbstractSelectionList<?> list = (AbstractSelectionList<?>) (Object) this;
			Reorder reorder = Reorder.of(list);
			this.rearrange$drag = reorder == null ? null : new ListDrag(list, reorder);
		}
		return this.rearrange$drag;
	}

	@Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
	private void rearrange$frame(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		ListDrag drag = this.rearrange$drag();
		if (drag != null) {
			drag.frame(mouseY);
		}
	}

	@Inject(method = "extractListItems", at = @At("HEAD"))
	private void rearrange$hideDragged(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		ListDrag drag = this.rearrange$drag();
		if (drag != null) {
			drag.hideDragged();
		}
	}

	@Inject(method = "extractListItems", at = @At("TAIL"))
	private void rearrange$extractDragged(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		ListDrag drag = this.rearrange$drag();
		if (drag != null) {
			drag.extractDragged(graphics, a);
		}
	}
}
