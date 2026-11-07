package dev.creeperkatze.illuminate.game;

import dev.creeperkatze.illuminate.scene.BiomeFeel;
import dev.creeperkatze.illuminate.scene.BiomeFeel.Motion;
import dev.creeperkatze.illuminate.scene.BiomeFeels;
import dev.creeperkatze.illuminate.scene.Rgb;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

// Works out how any biome feels, vanilla or modded, from its tags and its own colours.
// Tags pick the motion and the kind of accent, the biome's foliage, grass and water colours pick the colours.
final class BiomeFeelResolver {
	private static final TagKey<Biome> UNDERGROUND = convention("is_underground");
	private static final TagKey<Biome> CAVE = convention("is_cave");
	private static final TagKey<Biome> LUSH = convention("is_lush");
	private static final TagKey<Biome> OCEAN = convention("is_ocean");
	private static final TagKey<Biome> DEEP_OCEAN = convention("is_deep_ocean");
	private static final TagKey<Biome> RIVER = convention("is_river");
	private static final TagKey<Biome> AQUATIC_ICY = convention("is_aquatic_icy");
	private static final TagKey<Biome> BEACH = convention("is_beach");
	private static final TagKey<Biome> STONY_SHORES = convention("is_stony_shores");
	private static final TagKey<Biome> SNOWY = convention("is_snowy");
	private static final TagKey<Biome> ICY = convention("is_icy");
	private static final TagKey<Biome> DESERT = convention("is_desert");
	private static final TagKey<Biome> SANDY = convention("is_sandy");
	private static final TagKey<Biome> BADLANDS = convention("is_badlands");
	private static final TagKey<Biome> SAVANNA = convention("is_savanna");
	private static final TagKey<Biome> JUNGLE = convention("is_jungle");
	private static final TagKey<Biome> SWAMP = convention("is_swamp");
	private static final TagKey<Biome> MUSHROOM = convention("is_mushroom");
	private static final TagKey<Biome> FLOWER_FOREST = convention("is_flower_forest");
	private static final TagKey<Biome> FLORAL = convention("is_floral");
	private static final TagKey<Biome> BIRCH_FOREST = convention("is_birch_forest");
	private static final TagKey<Biome> DARK_FOREST = convention("is_dark_forest");
	private static final TagKey<Biome> TAIGA = convention("is_taiga");
	private static final TagKey<Biome> FOREST = convention("is_forest");
	private static final TagKey<Biome> MOUNTAIN = convention("is_mountain");
	private static final TagKey<Biome> WINDSWEPT = convention("is_windswept");
	private static final TagKey<Biome> MAGICAL = convention("is_magical");
	private static final TagKey<Biome> SPOOKY = convention("is_spooky");
	private static final TagKey<Biome> WASTELAND = convention("is_wasteland");
	private static final TagKey<Biome> DEAD = convention("is_dead");

	private BiomeFeelResolver() {
	}

	static BiomeFeel resolve(ClientLevel level, BlockPos pos, Holder<Biome> biome) {
		String name = biome.unwrapKey().map(key -> key.identifier().toString()).orElse("unknown");
		BiomeFeel tuned = BiomeFeels.tuned(name);
		if (tuned != null) {
			return tuned;
		}
		Rgb foliage = vivid(BiomeColors.getAverageFoliageColor(level, pos));
		Rgb grass = vivid(BiomeColors.getAverageGrassColor(level, pos));
		Rgb water = vivid(BiomeColors.getAverageWaterColor(level, pos));
		boolean frozen = is(biome, SNOWY, ICY, AQUATIC_ICY);
		String label = name + ", from tags";

		if (is(biome, UNDERGROUND, CAVE)) {
			return is(biome, LUSH)
					? new BiomeFeel(label, foliage, BiomeFeels.GLOW, Motion.MOTES, 0.9F, true)
					: new BiomeFeel(label, BiomeFeels.EARTH, BiomeFeels.EMBER, Motion.CALM, 0.4F, true);
		}
		if (is(biome, OCEAN, DEEP_OCEAN, RIVER) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) {
			Rgb deep = is(biome, DEEP_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN) ? water.scale(0.6F) : water;
			return frozen
					? new BiomeFeel(label, deep.lerp(BiomeFeels.ICE, 0.6F), BiomeFeels.SNOW, Motion.GLINT, 1.0F, false)
					: new BiomeFeel(label, deep, water.lerp(Rgb.WHITE, 0.45F), Motion.SHIMMER, 0.5F, false);
		}
		if (is(biome, BEACH, STONY_SHORES) || biome.is(BiomeTags.IS_BEACH)) {
			Rgb shore = is(biome, STONY_SHORES) ? BiomeFeels.STONE : frozen ? BiomeFeels.SNOW : BiomeFeels.SHORE_SAND;
			return new BiomeFeel(label, shore, water, Motion.SHIMMER, 0.4F, false);
		}
		if (frozen) {
			boolean wooded = is(biome, FOREST, TAIGA) || biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA);
			return new BiomeFeel(label, wooded ? foliage.scale(0.6F) : BiomeFeels.ICE, BiomeFeels.SNOW, Motion.GLINT, wooded ? 0.9F : 1.0F, false);
		}
		if (is(biome, BADLANDS) || biome.is(BiomeTags.IS_BADLANDS)) {
			return new BiomeFeel(label, BiomeFeels.TERRACOTTA, new Rgb(0.75F, 0.2F, 0.1F), Motion.HEAT, 0.5F, false);
		}
		if (is(biome, DESERT, SANDY)) {
			return new BiomeFeel(label, BiomeFeels.SAND, new Rgb(1.0F, 0.9F, 0.6F), Motion.HEAT, 0.5F, false);
		}
		if (is(biome, SAVANNA) || biome.is(BiomeTags.IS_SAVANNA)) {
			return new BiomeFeel(label, grass, new Rgb(0.9F, 0.45F, 0.15F), Motion.HEAT, 0.35F, false);
		}
		if (is(biome, JUNGLE) || biome.is(BiomeTags.IS_JUNGLE)) {
			return new BiomeFeel(label, foliage, foliage.lerp(BiomeFeels.SUNLIGHT, 0.6F), Motion.DAPPLED, 0.6F, false);
		}
		if (is(biome, SWAMP)) {
			return new BiomeFeel(label, foliage.scale(0.6F), water.scale(0.5F), Motion.CALM, 0.6F, false);
		}
		if (is(biome, MUSHROOM)) {
			return new BiomeFeel(label, new Rgb(0.55F, 0.35F, 0.65F), new Rgb(0.85F, 0.2F, 0.2F), Motion.MOTES, 0.8F, false);
		}
		if (is(biome, SPOOKY, WASTELAND, DEAD)) {
			return new BiomeFeel(label, BiomeFeels.STONE.scale(0.7F), foliage, Motion.MOTES, 0.5F, false);
		}
		if (is(biome, MAGICAL)) {
			return new BiomeFeel(label, foliage, BiomeFeels.MAGIC, Motion.MOTES, 0.7F, false);
		}
		if (is(biome, FLOWER_FOREST, FLORAL)) {
			return new BiomeFeel(label, foliage, BiomeFeels.BLOSSOM, Motion.DAPPLED, 0.6F, false);
		}
		if (is(biome, DARK_FOREST)) {
			return new BiomeFeel(label, foliage.scale(0.35F), foliage.lerp(BiomeFeels.SUNLIGHT, 0.4F).scale(0.6F), Motion.DAPPLED, 0.4F, false);
		}
		if (is(biome, BIRCH_FOREST)) {
			return new BiomeFeel(label, foliage, new Rgb(0.95F, 0.95F, 0.85F), Motion.DAPPLED, 0.6F, false);
		}
		if (is(biome, TAIGA) || biome.is(BiomeTags.IS_TAIGA)) {
			return new BiomeFeel(label, foliage.scale(0.6F), new Rgb(0.5F, 0.35F, 0.2F), Motion.CALM, 0.4F, false);
		}
		if (is(biome, FOREST) || biome.is(BiomeTags.IS_FOREST)) {
			return new BiomeFeel(label, foliage.scale(0.7F), BiomeFeels.SUNLIGHT, Motion.DAPPLED, 0.6F, false);
		}
		if (is(biome, MOUNTAIN, WINDSWEPT) || biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(BiomeTags.IS_HILL)) {
			return new BiomeFeel(label, BiomeFeels.STONE, BiomeFeels.SKY, Motion.CALM, 0.5F, false);
		}
		// Open land and anything without useful tags: sky with the colour of the land drifting through
		return new BiomeFeel(label, BiomeFeels.SKY, grass, Motion.CALM, 0.5F, false);
	}

	// Biome colours are muted, so they are made more vivid for the lights
	private static Rgb vivid(int color) {
		return Rgb.of(color).saturate(1.4F);
	}

	@SafeVarargs
	private static boolean is(Holder<Biome> biome, TagKey<Biome>... tags) {
		for (TagKey<Biome> tag : tags) {
			if (biome.is(tag)) {
				return true;
			}
		}
		return false;
	}

	private static TagKey<Biome> convention(String path) {
		return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", path));
	}
}
