package dev.creeperkatze.speeddial.config;

import com.mojang.serialization.Codec;
import dev.creeperkatze.speeddial.SpeedDial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class SpeedDialConfigScreen extends OptionsSubScreen {
	private static final int MIN_SPEED = 1;
	private static final int MAX_SPEED = 100;
	private static final int MIN_PROGRESSIVE_STEP = 101;
	private static final int MAX_PROGRESSIVE_STEP = 200;
	private static final int MIN_LINEAR_STEP = 1;
	private static final int MAX_LINEAR_STEP = 100;

	public SpeedDialConfigScreen(Screen lastScreen) {
		super(lastScreen, Minecraft.getInstance().options, Component.translatable("speed-dial.config.title"));
	}

	@Override
	protected void addOptions() {
		SpeedDialConfig config = SpeedDial.config();
		this.list.addBig(slider(
				"max_speed", MIN_SPEED, MAX_SPEED, Math.round(config.maxSpeed),
				value -> Component.translatable("speed-dial.config.multiplier", value),
				value -> config.maxSpeed = value
		));
		this.list.addBig(cycle("step_mode", StepMode.values(), config.stepMode, StepMode::caption, value -> config.stepMode = value));
		this.list.addSmall(
				slider(
						"progressive_step", MIN_PROGRESSIVE_STEP, MAX_PROGRESSIVE_STEP, Math.round(config.progressiveMultiplier * 100.0F),
						value -> Component.translatable("speed-dial.config.times", String.format(Locale.ROOT, "%.2f", value / 100.0F)),
						value -> config.progressiveMultiplier = value / 100.0F
				),
				slider(
						"linear_step", MIN_LINEAR_STEP, MAX_LINEAR_STEP, Math.round(config.linearStep * 10.0F),
						value -> Component.translatable("speed-dial.config.plus", formatTenths(value)),
						value -> config.linearStep = value / 10.0F
				)
		);
		this.list.addSmall(
				cycle("speed_display", SpeedDisplay.values(), config.speedDisplay, SpeedDisplay::caption, value -> config.speedDisplay = value),
				toggle("match_vertical_speed", config.matchVerticalSpeed, value -> config.matchVerticalSpeed = value),
				toggle("remember_speed", config.rememberSpeed, value -> config.rememberSpeed = value),
				toggle("pick_block_resets_speed", config.pickBlockResetsSpeed, value -> config.pickBlockResetsSpeed = value)
		);
	}

	@Override
	public void removed() {
		super.removed();
		SpeedDial.config().save();
	}

	private static String formatTenths(int value) {
		return value % 10 == 0 ? String.valueOf(value / 10) : String.format(Locale.ROOT, "%.1f", value / 10.0F);
	}

	private static OptionInstance<Integer> slider(String key, int min, int max, int initialValue, Function<Integer, Component> label, OptionInstance.ValueUpdateListener<Integer> onValueUpdate) {
		return new OptionInstance<>(
				"speed-dial.config." + key,
				tooltip(key),
				(caption, value) -> Options.genericValueLabel(caption, label.apply(value)),
				new OptionInstance.IntRange(min, max),
				Mth.clamp(initialValue, min, max),
				onValueUpdate
		);
	}

	private static <T extends Enum<T>> OptionInstance<T> cycle(String key, T[] values, T initialValue, Function<T, Component> caption, OptionInstance.ValueUpdateListener<T> onValueUpdate) {
		Codec<T> codec = Codec.STRING.xmap(name -> Enum.valueOf(initialValue.getDeclaringClass(), name), Enum::name);
		return new OptionInstance<>(
				"speed-dial.config." + key,
				tooltip(key),
				(optionCaption, value) -> caption.apply(value),
				new OptionInstance.Enum<>(List.of(values), codec),
				initialValue,
				onValueUpdate
		);
	}

	private static OptionInstance<Boolean> toggle(String key, boolean initialValue, OptionInstance.ValueUpdateListener<Boolean> onValueUpdate) {
		return OptionInstance.createBoolean("speed-dial.config." + key, tooltip(key), initialValue, onValueUpdate);
	}

	private static <T> OptionInstance.TooltipSupplier<T> tooltip(String key) {
		return OptionInstance.cachedConstantTooltip(Component.translatable("speed-dial.config." + key + ".tooltip"));
	}
}
