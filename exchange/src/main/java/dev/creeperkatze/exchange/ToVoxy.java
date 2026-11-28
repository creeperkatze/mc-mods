package dev.creeperkatze.exchange;

import dev.creeperkatze.exchange.dh.DhDatabase;
import dev.creeperkatze.exchange.dh.DhRow;
import dev.creeperkatze.exchange.dh.DhSection;
import dev.creeperkatze.exchange.dh.DhStates;
import dev.creeperkatze.exchange.voxy.VoxyIds;
import dev.creeperkatze.exchange.voxy.VoxyWorld;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.cortex.voxy.common.voxelization.VoxelizedSection;
import me.cortex.voxy.common.voxelization.WorldVoxilizedSectionMipper;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.common.world.WorldSection;
import me.cortex.voxy.common.world.WorldUpdater;
import me.cortex.voxy.common.world.other.Mapper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ToVoxy {
	private static final int CHUNK = 16;
	private static final int CHUNKS_PER_ROW = DhSection.WIDTH / CHUNK;

	private final LodWorld world;
	private final LodCopy job;
	private final Registry<Biome> biomes;
	private final Holder<Biome> defaultBiome;
	private final Map<String, Holder<Biome>> biomeCache = new ConcurrentHashMap<>();

	ToVoxy(LodWorld world, LodCopy job) {
		this.world = world;
		this.job = job;
		this.biomes = world.registries().lookupOrThrow(Registries.BIOME);
		this.defaultBiome = this.biomes.getOrThrow(Biomes.PLAINS);
	}

	void run() throws Exception {
		List<LodWorld.Dimension> dimensions = this.world.dimensions().stream()
			.filter(dimension -> Files.isRegularFile(dimension.dhDatabase()))
			.toList();
		for (int i = 0; i < dimensions.size() && !this.job.isStopped(); i++) {
			this.copy(dimensions.get(i), i, dimensions.size());
		}
	}

	private void copy(LodWorld.Dimension dimension, int index, int count) throws Exception {
		try (DhDatabase database = DhDatabase.open(dimension.dhDatabase(), false)) {
			int rows = database.hasLods() ? database.count() : 0;
			// Distant Horizons creates empty databases for every dimension it loads
			if (rows == 0) {
				return;
			}
			this.job.startDimension(dimension, index, count, rows);
			try (VoxyWorld voxy = VoxyWorld.open(this.world.voxyPath(), VoxyIds.worldId(dimension.level(), this.world.seed()))) {
				Writer writer = new Writer(dimension, voxy);
				try (Workers workers = new Workers()) {
					database.forEachRow(row -> {
						if (this.job.isStopped()) {
							return false;
						}
						String area = dimension.level().identifier() + " " + row.x() * DhSection.WIDTH + ", " + row.z() * DhSection.WIDTH;
						workers.submit(area, () -> {
							try {
								writer.write(row);
							} finally {
								this.job.advance();
							}
						});
						return true;
					});
				}
			}
		}
	}

	private Holder<Biome> biome(String id) {
		return this.biomeCache.computeIfAbsent(id, key -> {
			Identifier identifier = Identifier.tryParse(key);
			if (identifier == null) {
				return this.defaultBiome;
			}
			return this.biomes.get(identifier).<Holder<Biome>>map(biome -> biome).orElse(this.defaultBiome);
		});
	}

	private final class Writer {
		private final WorldEngine engine;
		private final Mapper mapper;
		private final LongSet existing;
		private final int minY;
		private final int height;
		private final ThreadLocal<long[]> blocks;
		private final ThreadLocal<VoxelizedSection> section = ThreadLocal.withInitial(VoxelizedSection::createEmpty);

		Writer(LodWorld.Dimension dimension, VoxyWorld voxy) {
			this.engine = voxy.engine();
			this.mapper = this.engine.getMapper();
			this.existing = voxy.sections(0);
			this.minY = dimension.minY();
			this.height = dimension.height();
			this.blocks = ThreadLocal.withInitial(() -> new long[CHUNK * CHUNK * this.height]);
		}

		void write(DhRow row) throws IOException {
			DhSection area = DhSection.decode(row, row.format());
			long[] ids = this.voxyIds(area.mapping);
			for (int x = 0; x < CHUNKS_PER_ROW; x++) {
				for (int z = 0; z < CHUNKS_PER_ROW; z++) {
					this.writeChunk(area, ids, row.x() * CHUNKS_PER_ROW + x, row.z() * CHUNKS_PER_ROW + z, x * CHUNK, z * CHUNK);
				}
			}
		}

		// Voxy ids without light
		private long[] voxyIds(List<String> mapping) {
			long[] ids = new long[mapping.size()];
			for (int i = 0; i < ids.length; i++) {
				String entry = mapping.get(i);
				BlockState state = DhStates.parse(DhStates.block(entry));
				if (!state.isAir()) {
					int block = this.mapper.getIdForBlockState(state);
					int biome = this.mapper.getIdForBiome(ToVoxy.this.biome(DhStates.biome(entry)));
					ids[i] = Mapper.composeMappingId((byte) 0, block, biome);
				}
			}
			return ids;
		}

		private void writeChunk(DhSection area, long[] ids, int chunkX, int chunkZ, int offsetX, int offsetZ) {
			if (!hasColumns(area, offsetX, offsetZ) || this.hasChunk(chunkX, chunkZ)) {
				return;
			}
			long[] blocks = this.blocks.get();
			Arrays.fill(blocks, 0);
			for (int x = 0; x < CHUNK; x++) {
				for (int z = 0; z < CHUNK; z++) {
					this.fillColumn(blocks, area.columns[DhSection.index(offsetX + x, offsetZ + z)], ids, x, z);
				}
			}
			VoxelizedSection section = this.section.get();
			for (int y = 0; y < this.height / CHUNK; y++) {
				int offset = y * CHUNK * CHUNK * CHUNK;
				boolean filled = false;
				int nonAir = 0;
				for (int i = 0; i < CHUNK * CHUNK * CHUNK; i++) {
					long voxel = blocks[offset + i];
					section.section[i] = voxel;
					filled |= voxel != 0;
					nonAir += Mapper.isAir(voxel) ? 0 : 1;
				}
				if (!filled) {
					continue;
				}
				section.lvl0NonAirCount = nonAir;
				WorldVoxilizedSectionMipper.mipSection(section, this.mapper);
				section.setPosition(chunkX, (this.minY >> 4) + y, chunkZ);
				WorldUpdater.insertUpdate(this.engine, section);
			}
		}

		// A point stores the light above it. Its voxels take the light of the next point down.
		private void fillColumn(long[] blocks, long[] points, long[] ids, int x, int z) {
			for (int i = 0; i < points.length; i++) {
				long point = points[i];
				int id = DhSection.id(point);
				if (id >= ids.length) {
					continue;
				}
				long lightPoint = points[Math.min(i + 1, points.length - 1)];
				long voxel = Mapper.withLight(ids[id], DhSection.skyLight(lightPoint) | DhSection.blockLight(lightPoint) << 4);
				int bottom = DhSection.bottomY(point);
				int top = Math.min(bottom + DhSection.height(point), this.height);
				for (int y = bottom; y < top; y++) {
					blocks[y << 8 | z << 4 | x] = voxel;
				}
			}
		}

		// Voxy saves whole chunks. Any block or light means it has the chunk.
		private boolean hasChunk(int chunkX, int chunkZ) {
			int x = chunkX >> 1;
			int z = chunkZ >> 1;
			int startX = (chunkX & 1) * CHUNK;
			int startZ = (chunkZ & 1) * CHUNK;
			for (int y = this.minY >> 5; y <= (this.minY + this.height - 1) >> 5; y++) {
				if (!this.existing.contains(WorldEngine.getWorldSectionId(0, x, y, z))) {
					continue;
				}
				WorldSection section = this.engine.acquireIfExists(0, x, y, z);
				if (section == null) {
					continue;
				}
				try {
					long[] data = section._unsafeGetRawDataArray();
					for (int localY = 0; localY < 32; localY++) {
						for (int localZ = startZ; localZ < startZ + CHUNK; localZ++) {
							for (int localX = startX; localX < startX + CHUNK; localX++) {
								if (data[localY << 10 | localZ << 5 | localX] != 0) {
									return true;
								}
							}
						}
					}
				} finally {
					section.release();
				}
			}
			return false;
		}

		private static boolean hasColumns(DhSection area, int offsetX, int offsetZ) {
			for (int x = offsetX; x < offsetX + CHUNK; x++) {
				for (int z = offsetZ; z < offsetZ + CHUNK; z++) {
					if (area.columns[DhSection.index(x, z)].length > 0) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
