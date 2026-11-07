package dev.creeperkatze.illuminate.config;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.output.signalrgb.SignalRgbEffect;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class IlluminateConfigScreen {
	private IlluminateConfigScreen() {
	}

	public static Screen create(Screen parent) {
		IlluminateConfig defaults = IlluminateConfig.HANDLER.defaults();
		IlluminateConfig config = IlluminateConfig.HANDLER.instance();
		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("illuminate.config.title"))
				.category(general(defaults, config))
				.category(effects(defaults, config))
				.category(devices(defaults, config))
				.category(debug(defaults, config))
				.save(IlluminateConfig.HANDLER::save)
				.build()
				.generateScreen(parent);
	}

	private static ConfigCategory general(IlluminateConfig defaults, IlluminateConfig config) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("illuminate.config.category.general"))
				.option(toggle("enabled", defaults.enabled, () -> config.enabled, value -> config.enabled = value))
				.option(percent("brightness", 0.05F, 1.0F, defaults.brightness, () -> config.brightness,
						value -> config.brightness = value))
				.option(percent("saturation", 0.0F, 2.0F, defaults.saturation, () -> config.saturation,
						value -> config.saturation = value))
				.option(percent("smoothing", 0.0F, 1.0F, defaults.smoothing, () -> config.smoothing,
						value -> config.smoothing = value))
				.option(Option.<Integer>createBuilder()
						.name(name("frame_rate"))
						.description(description("frame_rate"))
						.binding(defaults.frameRate, () -> config.frameRate, value -> config.frameRate = value)
						.controller(option -> IntegerSliderControllerBuilder.create(option).range(10, 60).step(5)
								.formatValue(value -> Component.translatable("illuminate.config.fps", value)))
						.build())
				.option(toggle("reduce_flashing", defaults.reduceFlashing, () -> config.reduceFlashing,
						value -> config.reduceFlashing = value))
				.option(toggle("dim_in_menus", defaults.dimInMenus, () -> config.dimInMenus,
						value -> config.dimInMenus = value))
				.option(cycle("idle_mode", IdleMode.class, IdleMode::caption, defaults.idleMode, () -> config.idleMode,
						value -> config.idleMode = value))
				.build();
	}

	private static ConfigCategory debug(IlluminateConfig defaults, IlluminateConfig config) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("illuminate.config.category.debug"))
				.option(toggle("show_preview", defaults.showPreview, () -> config.showPreview,
						value -> config.showPreview = value))
				.option(toggle("log_events", defaults.logEvents, () -> config.logEvents,
						value -> config.logEvents = value))
				.option(LabelOption.create(Component.translatable("illuminate.config.f3_entry")))
				.option(LabelOption.create(Component.translatable("illuminate.config.test_command")))
				.build();
	}

	private static ConfigCategory effects(IlluminateConfig defaults, IlluminateConfig config) {
		OptionGroup world = OptionGroup.createBuilder()
				.name(Component.translatable("illuminate.config.group.world"))
				.option(toggle("environment", defaults.environment, () -> config.environment,
						value -> config.environment = value))
				.option(toggle("weather", defaults.weather, () -> config.weather, value -> config.weather = value))
				.option(toggle("submerged", defaults.submerged, () -> config.submerged,
						value -> config.submerged = value))
				.option(toggle("portals", defaults.portals, () -> config.portals, value -> config.portals = value))
				.option(toggle("boss_bars", defaults.bossBars, () -> config.bossBars, value -> config.bossBars = value))
				.build();
		OptionGroup player = OptionGroup.createBuilder()
				.name(Component.translatable("illuminate.config.group.player"))
				.option(toggle("status_effects", defaults.statusEffects, () -> config.statusEffects,
						value -> config.statusEffects = value))
				.option(toggle("low_health", defaults.lowHealth, () -> config.lowHealth,
						value -> config.lowHealth = value))
				.option(toggle("hazards", defaults.hazards, () -> config.hazards, value -> config.hazards = value))
				.option(toggle("sleep", defaults.sleep, () -> config.sleep, value -> config.sleep = value))
				.build();
		OptionGroup events = OptionGroup.createBuilder()
				.name(Component.translatable("illuminate.config.group.events"))
				.option(toggle("damage", defaults.damage, () -> config.damage, value -> config.damage = value))
				.option(toggle("world_events", defaults.worldEvents, () -> config.worldEvents,
						value -> config.worldEvents = value))
				.option(toggle("fireworks", defaults.fireworks, () -> config.fireworks,
						value -> config.fireworks = value))
				.option(percent("event_intensity", 0.1F, 1.5F, defaults.eventIntensity, () -> config.eventIntensity,
						value -> config.eventIntensity = value))
				.build();
		return ConfigCategory.createBuilder()
				.name(Component.translatable("illuminate.config.category.effects"))
				.group(world)
				.group(player)
				.group(events)
				.build();
	}

	private static ConfigCategory devices(IlluminateConfig defaults, IlluminateConfig config) {
		OptionGroup.Builder signalRgb = OptionGroup.createBuilder()
				.name(Component.translatable("illuminate.config.group.signalrgb"))
				.option(LabelOption.create(status(Illuminate.engine().signalRgbStatus())))
				.option(toggle("signalrgb", defaults.signalRgb, () -> config.signalRgb,
						value -> config.signalRgb = value))
				.option(toggle("signalrgb_apply_effect", defaults.signalRgbApplyEffect,
						() -> config.signalRgbApplyEffect, value -> config.signalRgbApplyEffect = value))
				.option(ButtonOption.createBuilder()
						.name(name("signalrgb_effect"))
						.description(OptionDescription
								.of(Component.translatable("illuminate.config.signalrgb_effect.tooltip",
										SignalRgbEffect.effectsDirectory().toString())))
						.text(Component.translatable("illuminate.config.install"))
						.action((screen, button) -> installEffect())
						.build())
				.option(port("signalrgb_port", defaults.signalRgbPort, () -> config.signalRgbPort,
						value -> config.signalRgbPort = value));

		OptionGroup.Builder openRgb = OptionGroup.createBuilder()
				.name(Component.translatable("illuminate.config.group.openrgb"))
				.option(LabelOption.create(status(Illuminate.engine().openRgbStatus())))
				.option(toggle("openrgb", defaults.openRgb, () -> config.openRgb, value -> config.openRgb = value))
				.option(Option.<String>createBuilder()
						.name(name("openrgb_host"))
						.description(description("openrgb_host"))
						.binding(defaults.openRgbHost, () -> config.openRgbHost,
								value -> config.openRgbHost = value.isBlank() ? defaults.openRgbHost : value.trim())
						.controller(StringControllerBuilder::create)
						.build())
				.option(port("openrgb_port", defaults.openRgbPort, () -> config.openRgbPort,
						value -> config.openRgbPort = value));

		ConfigCategory.Builder category = ConfigCategory.createBuilder()
				.name(Component.translatable("illuminate.config.category.devices"))
				.option(ButtonOption.createBuilder()
						.name(name("live_preview"))
						.description(description("live_preview"))
						.text(Component.translatable("illuminate.config.open"))
						.action((screen, button) -> Minecraft.getInstance().gui.setScreen(new LightingScreen(screen)))
						.build());
		if (config.signalRgb && config.openRgb) {
			category.option(LabelOption.create(Component.translatable("illuminate.config.overlap")));
		}
		category.group(signalRgb.build()).group(openRgb.build());

		Set<String> deviceNames = new LinkedHashSet<>(Illuminate.engine().openRgbDevices());
		deviceNames.addAll(config.openRgbPlacements.keySet());
		if (!deviceNames.isEmpty()) {
			OptionGroup.Builder placements = OptionGroup.createBuilder()
					.name(Component.translatable("illuminate.config.group.placements"))
					.description(
							OptionDescription.of(Component.translatable("illuminate.config.group.placements.tooltip")));
			for (String device : deviceNames) {
				placements.option(Option.<Placement>createBuilder()
						.name(Component.literal(device))
						.description(choice("placement"))
						.binding(Placement.CENTER, () -> config.placement(device),
								value -> config.setPlacement(device, value))
						.controller(option -> EnumControllerBuilder.create(option).enumClass(Placement.class)
								.formatValue(Placement::caption))
						.build());
			}
			category.group(placements.build());
		}
		return category.build();
	}

	private static void installEffect() {
		Component title = Component.translatable("illuminate.config.title");
		Component message;
		try {
			SignalRgbEffect.install();
			message = Component.translatable("illuminate.toast.effect_installed");
		} catch (IOException e) {
			Illuminate.LOGGER.error("Could not install the SignalRGB effect", e);
			message = Component.translatable("illuminate.toast.effect_failed", e.getMessage());
		}
		SystemToast.addOrUpdate(Minecraft.getInstance().gui.toastManager(),
				SystemToast.SystemToastId.PERIODIC_NOTIFICATION, title, message);
	}

	private static Component status(String status) {
		return Component.translatable("illuminate.config.status",
				Component.translatable("illuminate.config.status." + status));
	}

	private static Option<Integer> port(String key, int defaultValue, Supplier<Integer> getter,
			Consumer<Integer> setter) {
		return Option.<Integer>createBuilder()
				.name(name(key))
				.description(description(key))
				.binding(defaultValue, getter, setter)
				.controller(option -> IntegerFieldControllerBuilder.create(option).range(1, 65535)
						.formatValue(value -> Component.literal(String.valueOf(value))))
				.build();
	}

	private static Option<Float> percent(String key, float min, float max, float defaultValue, Supplier<Float> getter,
			Consumer<Float> setter) {
		return Option.<Float>createBuilder()
				.name(name(key))
				.description(description(key))
				.binding(defaultValue, getter, setter)
				.controller(option -> FloatSliderControllerBuilder.create(option)
						.range(min, max)
						.step(0.05F)
						.formatValue(value -> Component.translatable("illuminate.config.percent",
								Math.round(value * 100.0F))))
				.build();
	}

	private static <T extends Enum<T>> Option<T> cycle(String key, Class<T> type, Function<T, Component> caption,
			T defaultValue, Supplier<T> getter, Consumer<T> setter) {
		return Option.<T>createBuilder()
				.name(name(key))
				.description(choice(key))
				.binding(defaultValue, getter, setter)
				.controller(option -> EnumControllerBuilder.create(option)
						.enumClass(type)
						.formatValue(caption::apply))
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

	private static <T extends Enum<T>> Function<T, OptionDescription> choice(String key) {
		return value -> OptionDescription.of(Component.translatable("illuminate.config." + key + ".tooltip")
				.append("\n\n")
				.append(Component.translatable(
						"illuminate.config." + key + "." + value.name().toLowerCase(Locale.ROOT) + ".tooltip")));
	}

	private static Component name(String key) {
		return Component.translatable("illuminate.config." + key);
	}

	private static OptionDescription description(String key) {
		return OptionDescription.of(Component.translatable("illuminate.config." + key + ".tooltip"));
	}
}
