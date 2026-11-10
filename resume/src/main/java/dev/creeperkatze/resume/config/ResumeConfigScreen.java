package dev.creeperkatze.resume.config;

import com.mojang.blaze3d.Blaze3D;
import dev.creeperkatze.resume.Place;
import dev.creeperkatze.resume.Resume;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
								.name(name("darkness"))
								.description(description("darkness"))
								.binding(defaults.darkness, () -> config.darkness, value -> config.darkness = value)
								.controller(option -> FloatSliderControllerBuilder.create(option)
										.range(0.0F, 0.9F)
										.step(0.05F)
										.formatValue(value -> Component.translatable("resume.config.percent",
												Math.round(value * 100.0F))))
								.build())
						.option(Option.<Float>createBuilder()
								.name(name("fade_duration"))
								.description(description("fade_duration"))
								.binding(defaults.fadeDuration, () -> config.fadeDuration,
										value -> config.fadeDuration = value)
								.controller(option -> FloatSliderControllerBuilder.create(option)
										.range(0.0F, 3.0F)
										.step(0.1F)
										.formatValue(value -> Component.translatable("resume.config.seconds",
												String.format("%.1f", value))))
								.build())
						.option(ButtonOption.createBuilder()
								.name(name("screenshots"))
								.description(description("screenshots"))
								.text(Component.translatable("resume.config.open_folder"))
								.action((screen, button) -> openFolder())
								.build())
						.build())
				.save(ResumeConfig.HANDLER::save)
				.build()
				.generateScreen(parent);
	}

	private static void openFolder() {
		Path folder = Place.directory();
		try {
			Files.createDirectories(folder);
			Blaze3D.openPath(folder);
		} catch (IOException e) {
			Resume.LOGGER.warn("Could not open {}", folder, e);
		}
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
