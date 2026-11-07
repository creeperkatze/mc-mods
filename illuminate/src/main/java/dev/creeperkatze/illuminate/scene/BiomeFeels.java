package dev.creeperkatze.illuminate.scene;

import dev.creeperkatze.illuminate.scene.BiomeFeel.Motion;

import java.util.HashMap;
import java.util.Map;

public final class BiomeFeels {
	private static final Rgb SKY = new Rgb(0.35F, 0.6F, 1.0F);
	private static final Rgb GRASS = new Rgb(0.4F, 0.85F, 0.25F);
	private static final Rgb SUNLIGHT = new Rgb(0.95F, 0.9F, 0.5F);
	private static final Rgb SNOW = new Rgb(0.95F, 0.97F, 1.0F);
	private static final Rgb ICE = new Rgb(0.65F, 0.8F, 1.0F);
	private static final Rgb STONE = new Rgb(0.5F, 0.55F, 0.62F);
	private static final Rgb SAND = new Rgb(1.0F, 0.75F, 0.35F);
	private static final Rgb TERRACOTTA = new Rgb(0.9F, 0.38F, 0.12F);
	private static final Rgb SAVANNA = new Rgb(0.8F, 0.65F, 0.25F);
	private static final Rgb SPRUCE = new Rgb(0.1F, 0.35F, 0.25F);
	private static final Rgb WATER = new Rgb(0.15F, 0.4F, 0.95F);
	private static final Rgb FOAM = new Rgb(0.5F, 0.85F, 1.0F);

	public static final BiomeFeel DEFAULT = new BiomeFeel("default", SKY, GRASS, Motion.CALM, 0.5F, false);

	private static final Map<String, BiomeFeel> FEELS = new HashMap<>();

	static {
		// Open land: sky with the colour of the land drifting through
		add("plains", SKY, GRASS, Motion.CALM, 0.5F);
		add("sunflower_plains", SKY, new Rgb(1.0F, 0.8F, 0.15F), Motion.CALM, 0.6F);
		add("meadow", new Rgb(0.45F, 0.7F, 1.0F), new Rgb(0.75F, 0.5F, 1.0F), Motion.MOTES, 0.8F);
		add("savanna", SAVANNA, new Rgb(0.9F, 0.45F, 0.15F), Motion.HEAT, 0.35F);
		add("savanna_plateau", SAVANNA, new Rgb(0.9F, 0.45F, 0.15F), Motion.HEAT, 0.35F);
		add("windswept_savanna", SAVANNA, SKY, Motion.CALM, 0.5F);

		// Forests: green with sun falling through the leaves
		add("forest", new Rgb(0.15F, 0.55F, 0.15F), SUNLIGHT, Motion.DAPPLED, 0.6F);
		add("flower_forest", new Rgb(0.15F, 0.55F, 0.15F), new Rgb(1.0F, 0.45F, 0.75F), Motion.DAPPLED, 0.6F);
		add("birch_forest", new Rgb(0.45F, 0.75F, 0.3F), new Rgb(0.95F, 0.95F, 0.85F), Motion.DAPPLED, 0.6F);
		add("old_growth_birch_forest", new Rgb(0.45F, 0.75F, 0.3F), new Rgb(0.95F, 0.95F, 0.85F), Motion.DAPPLED, 0.6F);
		add("dark_forest", new Rgb(0.05F, 0.25F, 0.06F), new Rgb(0.5F, 0.6F, 0.2F), Motion.DAPPLED, 0.4F);
		add("pale_garden", new Rgb(0.55F, 0.57F, 0.52F), new Rgb(1.0F, 0.55F, 0.2F), Motion.MOTES, 0.7F);
		add("jungle", new Rgb(0.1F, 0.7F, 0.1F), new Rgb(0.7F, 0.95F, 0.2F), Motion.DAPPLED, 0.6F);
		add("sparse_jungle", new Rgb(0.1F, 0.7F, 0.1F), new Rgb(0.7F, 0.95F, 0.2F), Motion.DAPPLED, 0.5F);
		add("bamboo_jungle", new Rgb(0.1F, 0.7F, 0.1F), new Rgb(0.55F, 0.9F, 0.3F), Motion.DAPPLED, 0.6F);
		add("cherry_grove", new Rgb(1.0F, 0.55F, 0.75F), new Rgb(1.0F, 0.85F, 0.92F), Motion.MOTES, 0.9F);

		// Taiga: dark spruce, snowy ones glint
		add("taiga", SPRUCE, new Rgb(0.5F, 0.35F, 0.2F), Motion.CALM, 0.4F);
		add("old_growth_pine_taiga", SPRUCE, new Rgb(0.5F, 0.35F, 0.2F), Motion.CALM, 0.4F);
		add("old_growth_spruce_taiga", SPRUCE, new Rgb(0.5F, 0.35F, 0.2F), Motion.CALM, 0.4F);
		add("snowy_taiga", SPRUCE, SNOW, Motion.GLINT, 0.9F);

		// Swamps: murky and slow
		add("swamp", new Rgb(0.25F, 0.35F, 0.15F), new Rgb(0.1F, 0.3F, 0.25F), Motion.CALM, 0.6F);
		add("mangrove_swamp", new Rgb(0.3F, 0.4F, 0.15F), new Rgb(0.4F, 0.3F, 0.15F), Motion.CALM, 0.6F);
		add("mushroom_fields", new Rgb(0.55F, 0.35F, 0.65F), new Rgb(0.85F, 0.2F, 0.2F), Motion.MOTES, 0.8F);

		// Dry land: warm air rising
		add("desert", SAND, new Rgb(1.0F, 0.9F, 0.6F), Motion.HEAT, 0.5F);
		add("badlands", TERRACOTTA, new Rgb(0.75F, 0.2F, 0.1F), Motion.HEAT, 0.5F);
		add("eroded_badlands", TERRACOTTA, new Rgb(0.75F, 0.2F, 0.1F), Motion.HEAT, 0.5F);
		add("wooded_badlands", TERRACOTTA, new Rgb(0.4F, 0.5F, 0.15F), Motion.HEAT, 0.4F);

		// Cold and high places: ice, snow and bare rock
		add("snowy_plains", new Rgb(0.7F, 0.82F, 1.0F), SNOW, Motion.GLINT, 1.0F);
		add("ice_spikes", new Rgb(0.45F, 0.75F, 1.0F), SNOW, Motion.GLINT, 1.0F);
		add("grove", ICE, SPRUCE, Motion.CALM, 0.3F);
		add("snowy_slopes", ICE, SNOW, Motion.GLINT, 1.0F);
		add("frozen_peaks", ICE, SNOW, Motion.GLINT, 1.0F);
		add("jagged_peaks", ICE, SNOW, Motion.GLINT, 1.0F);
		add("stony_peaks", STONE, SKY, Motion.CALM, 0.5F);
		add("windswept_hills", STONE, SKY, Motion.CALM, 0.5F);
		add("windswept_gravelly_hills", STONE, SKY, Motion.CALM, 0.5F);
		add("windswept_forest", SPRUCE, STONE, Motion.CALM, 0.5F);
		add("stony_shore", STONE, WATER, Motion.SHIMMER, 0.4F);

		// Water: rippling light
		add("river", WATER, FOAM, Motion.SHIMMER, 0.5F);
		add("frozen_river", ICE, SNOW, Motion.GLINT, 1.0F);
		add("beach", new Rgb(1.0F, 0.85F, 0.55F), WATER, Motion.SHIMMER, 0.4F);
		add("snowy_beach", SNOW, WATER, Motion.SHIMMER, 0.4F);
		add("ocean", new Rgb(0.1F, 0.3F, 0.85F), FOAM, Motion.SHIMMER, 0.5F);
		add("deep_ocean", new Rgb(0.05F, 0.15F, 0.55F), new Rgb(0.2F, 0.45F, 0.9F), Motion.SHIMMER, 0.5F);
		add("warm_ocean", new Rgb(0.1F, 0.75F, 0.8F), new Rgb(0.6F, 1.0F, 0.9F), Motion.SHIMMER, 0.5F);
		add("lukewarm_ocean", new Rgb(0.1F, 0.55F, 0.85F), FOAM, Motion.SHIMMER, 0.5F);
		add("deep_lukewarm_ocean", new Rgb(0.05F, 0.35F, 0.65F), FOAM, Motion.SHIMMER, 0.5F);
		add("cold_ocean", new Rgb(0.15F, 0.3F, 0.7F), FOAM, Motion.SHIMMER, 0.5F);
		add("deep_cold_ocean", new Rgb(0.08F, 0.18F, 0.5F), new Rgb(0.3F, 0.5F, 0.85F), Motion.SHIMMER, 0.5F);
		add("frozen_ocean", new Rgb(0.5F, 0.7F, 0.95F), SNOW, Motion.GLINT, 1.0F);
		add("deep_frozen_ocean", new Rgb(0.35F, 0.55F, 0.85F), SNOW, Motion.GLINT, 1.0F);

		// Cave biomes keep their feel in the dark
		underground("lush_caves", new Rgb(0.2F, 0.65F, 0.25F), new Rgb(1.0F, 0.75F, 0.25F), Motion.MOTES, 0.9F);
		underground("dripstone_caves", new Rgb(0.45F, 0.3F, 0.2F), new Rgb(0.8F, 0.55F, 0.3F), Motion.CALM, 0.4F);
		underground("deep_dark", new Rgb(0.0F, 0.03F, 0.05F), new Rgb(0.0F, 0.6F, 0.65F), Motion.PULSE, 0.6F);
	}

	private BiomeFeels() {
	}

	public static BiomeFeel of(String biome) {
		return FEELS.getOrDefault(biome, DEFAULT);
	}

	private static void add(String name, Rgb base, Rgb accent, Motion motion, float strength) {
		FEELS.put(name, new BiomeFeel(name, base, accent, motion, strength, false));
	}

	private static void underground(String name, Rgb base, Rgb accent, Motion motion, float strength) {
		FEELS.put(name, new BiomeFeel(name, base, accent, motion, strength, true));
	}
}
