package dev.creeperkatze.exchange.voxy;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// Mirrors Voxy's ids and voxel layout
public final class VoxyIds {
	public static final int SECTION = 32;
	public static final int LAYER = SECTION * SECTION;
	public static final int SECTION_VOLUME = LAYER * SECTION;

	private VoxyIds() {
	}

	public static String worldId(ResourceKey<Level> level, long seed) {
		String identity = BiomeManager.obfuscateSeed(seed) + level.toString();
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash).substring(0, 32);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	public static long sectionKey(int level, int x, int y, int z) {
		return (long) level << 60 | (long) (y & 0xFF) << 52 | (long) (z & 0xFFFFFF) << 28 | (long) (x & 0xFFFFFF) << 4;
	}

	public static int level(long key) {
		return (int) (key >>> 60 & 0xF);
	}

	public static int x(long key) {
		return (int) (key << 36 >> 40);
	}

	public static int y(long key) {
		return (int) (key << 4 >> 56);
	}

	public static int z(long key) {
		return (int) (key << 12 >> 40);
	}

	public static int index(int x, int y, int z) {
		return y << 10 | z << 5 | x;
	}

	public static int blockId(long voxel) {
		return (int) (voxel >>> 27 & 0xFFFFF);
	}

	public static int biomeId(long voxel) {
		return (int) (voxel >>> 47 & 0x1FF);
	}

	public static int light(long voxel) {
		return (int) (voxel >>> 56 & 0xFF);
	}
}
