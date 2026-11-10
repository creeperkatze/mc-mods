package dev.creeperkatze.resume.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ResumeConfigScreen {
	private ResumeConfigScreen() {
	}

	public static Screen create(Screen parent) {
		ResumeConfig defaults = ResumeConfig.HANDLER.defaults();
		ResumeConfig config = ResumeConfig.HANDLER.instance();
		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("resume.config.title"))
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("resume.config.category.general"))
						.option(toggle("worlds", defaults.worlds, () -> config.worlds, value -> config.worlds = value))
						.option(toggle("servers", defaults.servers, () -> config.servers,
								value -> config.servers = value))
						.option(Option.<Float>createBuilder()
								.name(name("brightness"))
								.description(description("brightness"))
								.binding(defaults.brightness, () -> config.brightness,
										value -> config.brightness = value)
								.controller(option -> FloatSliderControllerBuilder.create(option)
										.range(0.0F, 1.0F)
										.step(0.01F)
										.formatValue(value -> Component.translatable("resume.config.percent",
												Math.round(value * 100.0F))))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(name("blur"))
								.description(description("blur"))
								.binding(defaults.blur, () -> config.blur, value -> config.blur = value)
								.controller(option -> IntegerSliderControllerBuilder.create(option)
										.range(0, 10)
										.step(1)
										.formatValue(value -> value == 0 ? CommonComponents.OPTION_OFF
												: Component.literal(String.valueOf(value))))
								.build())
						.option(seconds("fade_in_duration", defaults.fadeInDuration, () -> config.fadeInDuration,
								value -> config.fadeInDuration = value))
						.option(seconds("fade_out_duration", defaults.fadeOutDuration, () -> config.fadeOutDuration,
								value -> config.fadeOutDuration = value))
						.build())
				.save(ResumeConfig.HANDLER::save)
				.build()
				.generateScreen(parent);
	}

	private static Option<Float> seconds(String key, float defaultValue, Supplier<Float> getter, Consumer<Float> setter) {
		return Option.<Float>createBuilder()
				.name(name(key))
				.description(description(key))
				.binding(defaultValue, getter, setter)
				.controller(option -> FloatSliderControllerBuilder.create(option)
						.range(0.0F, 3.0F)
						.step(0.1F)
						.formatValue(value -> Component.translatable("resume.config.seconds",
								String.format("%.1f", value))))
				.build();
	}

	private static Option<Boolean> toggle(String key, boolean defaultValue, Supplier<Boolean> getter,
			Consumer<Boolean> setter) {
		return Option.<Boolean>createBuilder()
				.name(name(key))
				.description(description(key))
				.binding(defaultValue, getter, setter)
				.controller(option -> BooleanControllerBuilder.create(option).onOffFormatter())
				.build();
	}

	private static Component name(String key) {
		return Component.translatable("resume.config." + key);
	}

	private static OptionDescription description(String key) {
		return OptionDescription.of(Component.translatable("resume.config." + key + ".tooltip"));
	}
}
