package dev.creeperkatze.speeddial.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SpeedDialConfigScreen {
	private SpeedDialConfigScreen() {
	}

	public static Screen create(Screen parent) {
		SpeedDialConfig defaults = SpeedDialConfig.HANDLER.defaults();
		SpeedDialConfig config = SpeedDialConfig.HANDLER.instance();
		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("speed-dial.config.title"))
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("speed-dial.config.title"))
						.option(Option.<Float>createBuilder()
								.name(name("max_speed"))
								.description(description("max_speed"))
								.binding(defaults.maxSpeed, () -> config.maxSpeed, value -> config.maxSpeed = value)
								.controller(option -> FloatSliderControllerBuilder.create(option)
										.range(1.0F, 100.0F)
										.step(1.0F)
										.formatValue(value -> Component.translatable("speed-dial.config.multiplier", Math.round(value))))
								.build())
						.option(cycle("step_mode", StepMode.class, StepMode::caption, defaults.stepMode, () -> config.stepMode, value -> config.stepMode = value))
						.option(Option.<Float>createBuilder()
								.name(name("progressive_step"))
								.description(description("progressive_step"))
								.binding(defaults.progressiveMultiplier, () -> config.progressiveMultiplier, value -> config.progressiveMultiplier = value)
								.controller(option -> FloatSliderControllerBuilder.create(option)
										.range(1.01F, 2.0F)
										.step(0.01F)
										.formatValue(value -> Component.translatable("speed-dial.config.times", String.format(Locale.ROOT, "%.2f", value))))
								.build())
						.option(Option.<Float>createBuilder()
								.name(name("linear_step"))
								.description(description("linear_step"))
								.binding(defaults.linearStep, () -> config.linearStep, value -> config.linearStep = value)
								.controller(option -> FloatSliderControllerBuilder.create(option)
										.range(0.1F, 10.0F)
										.step(0.1F)
										.formatValue(value -> Component.translatable("speed-dial.config.plus", formatTenths(value))))
								.build())
						.option(cycle("speed_display", SpeedDisplay.class, SpeedDisplay::caption, defaults.speedDisplay, () -> config.speedDisplay, value -> config.speedDisplay = value))
						.option(toggle("remember_speed", defaults.rememberSpeed, () -> config.rememberSpeed, value -> config.rememberSpeed = value))						.build())
				.save(SpeedDialConfig.HANDLER::save)
				.build()
				.generateScreen(parent);
	}

	private static String formatTenths(float value) {
		int tenths = Math.round(value * 10.0F);
		return tenths % 10 == 0 ? String.valueOf(tenths / 10) : String.format(Locale.ROOT, "%.1f", tenths / 10.0F);
	}

	private static <T extends Enum<T>> Option<T> cycle(String key, Class<T> type, Function<T, Component> caption, T defaultValue, Supplier<T> getter, Consumer<T> setter) {
		return Option.<T>createBuilder()
				.name(name(key))
				.description(description(key))
				.binding(defaultValue, getter, setter)
				.controller(option -> EnumControllerBuilder.create(option)
						.enumClass(type)
						.formatValue(caption::apply))
				.build();
	}

	private static Option<Boolean> toggle(String key, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		return Option.<Boolean>createBuilder()
				.name(name(key))
				.description(description(key))
				.binding(defaultValue, getter, setter)
				.controller(option -> BooleanControllerBuilder.create(option).onOffFormatter())
				.build();
	}

	private static Component name(String key) {
		return Component.translatable("speed-dial.config." + key);
	}

	private static OptionDescription description(String key) {
		return OptionDescription.of(Component.translatable("speed-dial.config." + key + ".tooltip"));
	}
}
