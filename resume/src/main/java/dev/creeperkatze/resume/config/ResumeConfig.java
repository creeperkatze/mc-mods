package dev.creeperkatze.resume.config;

import dev.creeperkatze.resume.Resume;
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

public class ResumeConfig {
	public static final ConfigClassHandler<ResumeConfig> HANDLER = ConfigClassHandler.createBuilder(ResumeConfig.class)
			.id(Identifier.fromNamespaceAndPath(Resume.MOD_ID, "config"))
			.serializer(handler -> GsonConfigSerializerBuilder.create(handler)
					.setPath(configDir().resolve(Resume.MOD_ID + ".json"))
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
	public boolean worlds = true;
	@SerialEntry
	public boolean servers = true;
	@SerialEntry
	public float brightness = 0.5F;
	@SerialEntry
	public float fadeDuration = 0.0F;
}
