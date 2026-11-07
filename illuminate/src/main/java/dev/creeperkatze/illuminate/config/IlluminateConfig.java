package dev.creeperkatze.illuminate.config;

import dev.creeperkatze.illuminate.Illuminate;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
/*? if fabric {*/
import net.fabricmc.loader.api.FabricLoader;
/*?} else {*//*
import net.neoforged.fml.loading.FMLPaths;
*//*?}*/
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class IlluminateConfig {
	public static final ConfigClassHandler<IlluminateConfig> HANDLER = ConfigClassHandler.createBuilder(IlluminateConfig.class)
			.id(Identifier.fromNamespaceAndPath(Illuminate.MOD_ID, "config"))
			.serializer(handler -> GsonConfigSerializerBuilder.create(handler)
					.setPath(configDir().resolve(Illuminate.MOD_ID + ".json"))
					.build())
			.build();

	private static Path configDir() {
		/*? if fabric {*/
		return FabricLoader.getInstance().getConfigDir();
		/*?} else {*//*
		return FMLPaths.CONFIGDIR.get();
		*//*?}*/
	}

	@SerialEntry
	public boolean enabled = true;
	@SerialEntry
	public float brightness = 1.0F;
	@SerialEntry
	public float saturation = 1.25F;
	@SerialEntry
	public float smoothing = 0.5F;
	@SerialEntry
	public int frameRate = 30;
	@SerialEntry
	public boolean reduceFlashing = false;
	@SerialEntry
	public boolean dimInMenus = true;
	@SerialEntry
	public IdleMode idleMode = IdleMode.AMBIENT;

	@SerialEntry
	public boolean environment = true;
	@SerialEntry
	public boolean weather = true;
	@SerialEntry
	public boolean submerged = true;
	@SerialEntry
	public boolean portals = true;
	@SerialEntry
	public boolean statusEffects = true;
	@SerialEntry
	public boolean bossBars = true;
	@SerialEntry
	public boolean lowHealth = true;
	@SerialEntry
	public boolean hazards = true;
	@SerialEntry
	public boolean sleep = true;
	@SerialEntry
	public boolean damage = true;
	@SerialEntry
	public boolean worldEvents = true;
	@SerialEntry
	public boolean fireworks = true;
	@SerialEntry
	public float eventIntensity = 1.0F;

	@SerialEntry
	public boolean signalRgb = true;
	@SerialEntry
	public int signalRgbPort = 16034;
	@SerialEntry
	public boolean signalRgbApplyEffect = true;
	@SerialEntry
	public boolean openRgb = false;
	@SerialEntry
	public String openRgbHost = "127.0.0.1";
	@SerialEntry
	public int openRgbPort = 6742;
	@SerialEntry
	public Map<String, Placement> openRgbPlacements = new LinkedHashMap<>();

	@SerialEntry
	public boolean showPreview = false;
	@SerialEntry
	public boolean showVirtualRoom = false;
	@SerialEntry
	public boolean logEvents = false;

	public Placement placement(String device) {
		return openRgbPlacements.getOrDefault(device, Placement.CENTER);
	}

	// Replaces the map instead of changing it, since the lighting thread reads it at the same time
	public void setPlacement(String device, Placement placement) {
		Map<String, Placement> placements = new LinkedHashMap<>(openRgbPlacements);
		placements.put(device, placement);
		openRgbPlacements = placements;
	}
}
