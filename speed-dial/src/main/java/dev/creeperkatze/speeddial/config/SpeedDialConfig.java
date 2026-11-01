package dev.creeperkatze.speeddial.config;

import dev.creeperkatze.speeddial.SpeedDial;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public class SpeedDialConfig {
	public static final ConfigClassHandler<SpeedDialConfig> HANDLER = ConfigClassHandler.createBuilder(SpeedDialConfig.class)
			.id(Identifier.fromNamespaceAndPath(SpeedDial.MOD_ID, "config"))
			.serializer(handler -> GsonConfigSerializerBuilder.create(handler)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve(SpeedDial.MOD_ID + ".json"))
					.build())
			.build();

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
	public boolean matchVerticalSpeed = false;
	@SerialEntry
	public boolean rememberSpeed = false;
	@SerialEntry
	public float rememberedSpeed = 1.0F;
}
