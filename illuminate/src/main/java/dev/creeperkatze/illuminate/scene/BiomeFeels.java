package dev.creeperkatze.illuminate.scene;

import dev.creeperkatze.illuminate.scene.BiomeFeel.Motion;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

// Shared colours and the few vanilla biomes whose character comes from blocks that tags and biome colours don't know about
public final class BiomeFeels {
	public static final Rgb SKY = new Rgb(0.35F, 0.6F, 1.0F);
	public static final Rgb SUNLIGHT = new Rgb(0.95F, 0.9F, 0.5F);
	public static final Rgb SNOW = new Rgb(0.95F, 0.97F, 1.0F);
	public static final Rgb ICE = new Rgb(0.65F, 0.8F, 1.0F);
	public static final Rgb STONE = new Rgb(0.5F, 0.55F, 0.62F);
	public static final Rgb SAND = new Rgb(1.0F, 0.75F, 0.35F);
	public static final Rgb SHORE_SAND = new Rgb(1.0F, 0.85F, 0.55F);
	public static final Rgb TERRACOTTA = new Rgb(0.9F, 0.38F, 0.12F);
	public static final Rgb EARTH = new Rgb(0.45F, 0.33F, 0.25F);
	public static final Rgb EMBER = new Rgb(0.8F, 0.55F, 0.3F);
	public static final Rgb BLOSSOM = new Rgb(1.0F, 0.45F, 0.75F);
	public static final Rgb GLOW = new Rgb(1.0F, 0.75F, 0.25F);
	public static final Rgb MAGIC = new Rgb(0.75F, 0.5F, 1.0F);

	public static final BiomeFeel DEFAULT = new BiomeFeel("default", SKY, new Rgb(0.4F, 0.85F, 0.25F), Motion.CALM, 0.5F, false);

	private static final Map<String, BiomeFeel> TUNED = new HashMap<>();

	static {
		tuned("minecraft:cherry_grove", new Rgb(1.0F, 0.55F, 0.75F), new Rgb(1.0F, 0.85F, 0.92F), Motion.MOTES, 0.9F, false);
		tuned("minecraft:sunflower_plains", SKY, new Rgb(1.0F, 0.8F, 0.15F), Motion.CALM, 0.6F, false);
		tuned("minecraft:meadow", new Rgb(0.45F, 0.7F, 1.0F), MAGIC, Motion.MOTES, 0.8F, false);
		tuned("minecraft:pale_garden", new Rgb(0.55F, 0.57F, 0.52F), new Rgb(1.0F, 0.55F, 0.2F), Motion.MOTES, 0.7F, false);
		tuned("minecraft:mushroom_fields", new Rgb(0.55F, 0.35F, 0.65F), new Rgb(0.85F, 0.2F, 0.2F), Motion.MOTES, 0.8F, false);
		tuned("minecraft:lush_caves", new Rgb(0.2F, 0.65F, 0.25F), GLOW, Motion.MOTES, 0.9F, true);
		tuned("minecraft:deep_dark", new Rgb(0.0F, 0.03F, 0.05F), new Rgb(0.0F, 0.6F, 0.65F), Motion.PULSE, 0.6F, true);
	}

	private BiomeFeels() {
	}

	public static @Nullable BiomeFeel tuned(String biome) {
		return TUNED.get(biome);
	}

	private static void tuned(String name, Rgb base, Rgb accent, Motion motion, float strength, boolean underground) {
		TUNED.put(name, new BiomeFeel(name + ", tuned", base, accent, motion, strength, underground));
	}
}
