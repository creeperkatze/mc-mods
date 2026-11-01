package dev.creeperkatze.speeddial.config;

import dev.creeperkatze.speeddial.SpeedDial;
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

public class SpeedDialConfig {
	public static final ConfigClassHandler<SpeedDialConfig> HANDLER = ConfigClassHandler.createBuilder(SpeedDialConfig.class)
			.id(Identifier.fromNamespaceAndPath(SpeedDial.MOD_ID, "config"))
			.serializer(handler -> GsonConfigSerializerBuilder.create(handler)
					.setPath(configDir().resolve(SpeedDial.MOD_ID + ".json"))
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
