package dev.creeperkatze.illuminate.game;

import dev.creeperkatze.illuminate.scene.BiomeFeel;
import dev.creeperkatze.illuminate.scene.BiomeFeels;
import dev.creeperkatze.illuminate.scene.Rgb;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record GameState(
		boolean inWorld,
		boolean menuOpen,
		Dimension dimension,
		boolean hasSky,
		Rgb horizon,
		BiomeFeel feel,
		Rgb sunGlow,
		float sunGlowStrength,
		float sunU,
		float moonStrength,
		float moonU,
		float moonV,
		float moonlight,
		Rgb waterFog,
		float skyLight,
		float daylight,
		float blockLight,
		Rgb blockTint,
		float rain,
		float thunder,
		Biome.Precipitation precipitation,
		boolean deepDark,
		FogType fluid,
		float health,
		float air,
		float frozen,
		boolean burning,
		float sleep,
		boolean dead,
		List<ActiveEffect> effects,
		float nightVision,
		float darkness,
		float blindness,
		float nausea,
		@Nullable Rgb bossColor,
		float portal
) {
	public static final GameState MENU = new GameState(false, false, Dimension.OTHER, false, Rgb.BLACK, BiomeFeels.DEFAULT, Rgb.BLACK, 0.0F, 0.5F, 0.0F, 0.5F, 0.5F, 0.0F,
			Rgb.BLACK, 0.0F, 0.0F, 0.0F, Rgb.BLACK, 0.0F, 0.0F, Biome.Precipitation.NONE, false, FogType.NONE, 1.0F, 1.0F, 0.0F, false, 0.0F, false, List.of(),
			0.0F, 0.0F, 0.0F, 0.0F, null, 0.0F);

	public enum Dimension {
		OVERWORLD,
		NETHER,
		END,
		OTHER
	}

	public record ActiveEffect(Rgb color, boolean harmful, int amplifier) {
	}
}
