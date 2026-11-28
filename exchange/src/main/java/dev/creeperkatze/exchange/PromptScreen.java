package dev.creeperkatze.exchange;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

final class PromptScreen extends ConfirmScreen {
	private final List<LodMod> targets;
	private final Consumer<LodMod> onCopy;
	private final Component skipLabel;
	private final Runnable onSkip;

	PromptScreen(List<LodMod> targets, Map<LodMod, Long> missing, Component skipLabel, Consumer<LodMod> onCopy, Runnable onSkip, Runnable onLater) {
		super(later -> onLater.run(), Component.translatable("exchange.prompt.title"), message(targets, missing));
		this.targets = targets;
		this.skipLabel = skipLabel;
		this.onCopy = onCopy;
		this.onSkip = onSkip;
	}

	// Targets come sorted by how much they gain
	private static Component message(List<LodMod> targets, Map<LodMod, Long> missing) {
		LodMod target = targets.getFirst();
		Component source = target.other().displayName();
		if (targets.size() > 1) {
			return Component.translatable(
				"exchange.prompt.message.both", source, chunks(missing.get(target)), target.displayName(), chunks(missing.get(target.other()))
			);
		}
		return Component.translatable("exchange.prompt.message", source, chunks(missing.get(target)), target.displayName());
	}

	private static String chunks(long count) {
		return String.format("%,d", count);
	}

	@Override
	protected void addButtons(LinearLayout buttonLayout) {
		for (LodMod target : this.targets) {
			Component label = Component.translatable("exchange.prompt.copy", target.displayName());
			buttonLayout.addChild(Button.builder(label, button -> this.onCopy.accept(target)).build());
		}
		Button skip = Button.builder(this.skipLabel, button -> this.onSkip.run()).build();
		// Three buttons don't fit in one row on small windows
		if (this.targets.size() > 1) {
			this.layout.addChild(skip);
		} else {
			buttonLayout.addChild(skip);
		}
	}
}
