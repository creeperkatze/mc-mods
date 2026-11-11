package dev.creeperkatze.gather;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

// Older versions have no room for Gather's settings on their LAN screen, so they get their own
public final class HostOptionsScreen extends Screen {
	private static final Component TITLE = Component.translatable("gather.host_settings");
	private static final Component OPEN = Component.translatable("gather.host_settings.open");

	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final Screen lastScreen;
	private final HostOptions options;

	private HostOptionsScreen(Screen lastScreen, IntegratedServer server) {
		super(TITLE);
		this.lastScreen = lastScreen;
		this.options = new HostOptions(server, () -> {
		});
	}

	public static Button.Builder openButton(Screen lastScreen, IntegratedServer server) {
		Minecraft minecraft = Minecraft.getInstance();
		return Button.builder(OPEN, button -> minecraft.gui.setScreen(new HostOptionsScreen(lastScreen, server)));
	}

	@Override
	protected void init() {
		this.layout.addTitleHeader(this.title, this.font);
		GridLayout grid = new GridLayout().columnSpacing(8).rowSpacing(4);
		this.options.addTo(grid.createRowHelper(2));
		this.layout.addToContents(grid);
		LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
			this.options.apply();
			this.onClose();
		}).build());
		footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.lastScreen);
	}
}
