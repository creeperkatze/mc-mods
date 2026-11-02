package dev.creeperkatze.accelerate.config;

import dev.creeperkatze.accelerate.Accelerate;
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

public class AccelerateConfig {
	public static final ConfigClassHandler<AccelerateConfig> HANDLER = ConfigClassHandler.createBuilder(AccelerateConfig.class)
			.id(Identifier.fromNamespaceAndPath(Accelerate.MOD_ID, "config"))
			.serializer(handler -> GsonConfigSerializerBuilder.create(handler)
					.setPath(configDir().resolve(Accelerate.MOD_ID + ".json"))
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
	public float maxSpeed = 50.0F;
	@SerialEntry
	public StepMode stepMode = StepMode.PROGRESSIVE;
	@SerialEntry
	public float progressiveMultiplier = 1.15F;
	@SerialEntry
	public float linearStep = 1.0F;
	@SerialEntry
	public SpeedDisplay speedDisplay = SpeedDisplay.MULTIPLIER;
	@SerialEntry
	public boolean rememberSpeed = false;
	@SerialEntry
	public float rememberedSpeed = 1.0F;
}
