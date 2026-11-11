package dev.creeperkatze.gather;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

// Gather's settings in the multiplayer section of the world options screen, or on their own screen in older versions
public final class HostOptions {
	private static final int WIDGET_WIDTH = 150;
	private static final int WIDGET_HEIGHT = 20;
	private static final int ROW_WIDTH = 308;
	private static final int SERVER_NAME_MAX_LENGTH = 60;
	private static final Component ONLINE_MODE = Component.translatable("gather.online_mode");
	private static final Tooltip ONLINE_MODE_ON_TOOLTIP = Tooltip.create(Component.translatable("gather.online_mode.on.tooltip"));
	private static final Tooltip ONLINE_MODE_OFF_TOOLTIP = Tooltip.create(Component.translatable("gather.online_mode.off.tooltip"));
	private static final Component MAX_PLAYERS = Component.translatable("gather.max_players");
	private static final Component SERVER_NAME = Component.translatable("gather.server_name");
	private static final Tooltip SERVER_NAME_TOOLTIP = Tooltip.create(Component.translatable("gather.server_name.tooltip"));
	private static final Component WHITELIST = Component.translatable("gather.whitelist");
	private static final Tooltip WHITELIST_TOOLTIP = Tooltip.create(Component.translatable("gather.whitelist.tooltip"));
	private static final Component IDLE_TIMEOUT = Component.translatable("gather.idle_timeout");
	private static final Tooltip IDLE_TIMEOUT_TOOLTIP = Tooltip.create(Component.translatable("gather.idle_timeout.tooltip"));
	private static final Component OPEN_ON_LOAD = Component.translatable("gather.open_on_load");
	private static final Tooltip OPEN_ON_LOAD_TOOLTIP = Tooltip.create(Component.translatable("gather.open_on_load.tooltip"));

	private final IntegratedServer server;
	private final HostSettings initial;
	private final Runnable onChange;
	private boolean onlineMode;
	private int maxPlayers;
	private String serverName;
	private boolean whitelist;
	private int idleTimeout;
	private boolean openOnLoad;

	public HostOptions(IntegratedServer server, Runnable onChange) {
		this.server = server;
		this.initial = LanHosting.settings(server);
		this.onChange = onChange;
		this.onlineMode = this.initial.onlineMode();
		this.maxPlayers = this.initial.maxPlayers();
		this.serverName = this.initial.serverName();
		this.whitelist = this.initial.whitelist();
		this.idleTimeout = this.initial.idleTimeout();
		this.openOnLoad = this.initial.openOnLoad();
	}

	public void addTo(GridLayout.RowHelper rows) {
		rows.addChild(CycleButton.onOffBuilder(this.onlineMode)
				.withTooltip(value -> value ? ONLINE_MODE_ON_TOOLTIP : ONLINE_MODE_OFF_TOOLTIP)
				.create(ONLINE_MODE, (button, value) -> {
					this.onlineMode = value;
					this.onChange.run();
				}));
		rows.addChild(new IntSlider(HostSettings.MIN_PLAYERS, HostSettings.MAX_PLAYERS, this.maxPlayers,
				players -> Component.translatable("options.generic_value", MAX_PLAYERS, players),
				players -> this.maxPlayers = players));

		EditBox name = new EditBox(Minecraft.getInstance().font, 0, 0, ROW_WIDTH, WIDGET_HEIGHT, SERVER_NAME);
		name.setMaxLength(SERVER_NAME_MAX_LENGTH);
		name.setValue(this.serverName);
		name.setHint(Component.literal(LanHosting.defaultServerName(this.server)));
		name.setTooltip(SERVER_NAME_TOOLTIP);
		name.setResponder(value -> {
			this.serverName = value;
			this.onChange.run();
		});
		name.addFormatter((visible, offset) -> ServerNameFormat.preview(name.getValue(), visible, offset));
		rows.addChild(name, 2);

		rows.addChild(CycleButton.onOffBuilder(this.whitelist)
				.withTooltip(value -> WHITELIST_TOOLTIP)
				.create(WHITELIST, (button, value) -> {
					this.whitelist = value;
					this.onChange.run();
				}));
		IntSlider idle = rows.addChild(new IntSlider(0, HostSettings.MAX_IDLE_TIMEOUT, this.idleTimeout,
				minutes -> Component.translatable("options.generic_value", IDLE_TIMEOUT,
						minutes == 0 ? CommonComponents.OPTION_OFF : Component.translatable("gather.idle_timeout.minutes", minutes)),
				minutes -> this.idleTimeout = minutes));
		idle.setTooltip(IDLE_TIMEOUT_TOOLTIP);

		rows.addChild(CycleButton.onOffBuilder(this.openOnLoad)
				.withTooltip(value -> OPEN_ON_LOAD_TOOLTIP)
				.create(OPEN_ON_LOAD, (button, value) -> {
					this.openOnLoad = value;
					this.onChange.run();
				}));
	}

	public boolean changed() {
		return this.onlineMode != this.initial.onlineMode()
				|| this.maxPlayers != this.initial.maxPlayers()
				|| !this.serverName.strip().equals(this.initial.serverName())
				|| this.whitelist != this.initial.whitelist()
				|| this.idleTimeout != this.initial.idleTimeout()
				|| this.openOnLoad != this.initial.openOnLoad();
	}

	public void apply() {
		if (this.changed()) {
			LanHosting.update(this.server, LanHosting.settings(this.server)
					.withOptions(this.onlineMode, this.maxPlayers, this.serverName.strip(), this.whitelist, this.idleTimeout, this.openOnLoad));
		}
	}

	private final class IntSlider extends AbstractSliderButton {
		private final int min;
		private final int max;
		private final IntFunction<Component> label;
		private final IntConsumer setter;

		private IntSlider(int min, int max, int initial, IntFunction<Component> label, IntConsumer setter) {
			super(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.empty(), (initial - min) / (double) (max - min));
			this.min = min;
			this.max = max;
			this.label = label;
			this.setter = setter;
			this.updateMessage();
		}

		private int intValue() {
			return this.min + (int) Math.round(this.value * (this.max - this.min));
		}

		@Override
		protected void updateMessage() {
			this.setMessage(this.label.apply(this.intValue()));
		}

		@Override
		protected void applyValue() {
			this.setter.accept(this.intValue());
			HostOptions.this.onChange.run();
		}
	}
}
