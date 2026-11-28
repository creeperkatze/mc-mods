package dev.creeperkatze.exchange;

import dev.creeperkatze.exchange.dh.DhDatabase;
import dev.creeperkatze.exchange.dh.DhRow;
import dev.creeperkatze.exchange.dh.DhSection;
import dev.creeperkatze.exchange.dh.DhStates;
import dev.creeperkatze.exchange.voxy.VoxyIds;
import dev.creeperkatze.exchange.voxy.VoxyReader;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ToDistantHorizons {
	private static final int SECTION = VoxyIds.SECTION;
	private static final int SECTIONS_PER_ROW = DhSection.WIDTH / SECTION;

	private final LodWorld world;
	private final LodCopy job;

	ToDistantHorizons(LodWorld world, LodCopy job) {
		this.world = world;
		this.job = job;
	}

	void run() throws Exception {
		List<LodWorld.Dimension> dimensions = this.world.dimensions().stream()
			.filter(dimension -> Files.isDirectory(this.world.voxyPath().resolve(VoxyIds.worldId(dimension.level(), this.world.seed()))))
			.toList();
		for (int i = 0; i < dimensions.size() && !this.job.isStopped(); i++) {
			this.copy(dimensions.get(i), i, dimensions.size());
		}
	}

	private void copy(LodWorld.Dimension dimension, int index, int count) throws Exception {
		try (VoxyReader voxy = VoxyReader.open(this.world.voxyPath(), VoxyIds.worldId(dimension.level(), this.world.seed()))) {
			Long2ObjectMap<IntList> columns = new Long2ObjectOpenHashMap<>();
			for (long key : voxy.sections(0)) {
				columns.computeIfAbsent(DhDatabase.pack(VoxyIds.x(key), VoxyIds.z(key)), column -> new IntArrayList()).add(VoxyIds.y(key));
			}
			LongSet rows = new LongOpenHashSet();
			for (long column : columns.keySet()) {
				rows.add(DhDatabase.pack(DhDatabase.unpackX(column) >> 1, DhDatabase.unpackZ(column) >> 1));
			}
			if (rows.isEmpty()) {
				return;
			}
			this.job.startDimension(dimension, index, count, rows.size());
			try (DhDatabase database = DhDatabase.open(dimension.dhDatabase(), true)) {
				Writer writer = new Writer(dimension, voxy, database, columns);
				try (Workers workers = new Workers()) {
					for (long row : rows) {
						if (this.job.isStopped()) {
							break;
						}
						int x = DhDatabase.unpackX(row);
						int z = DhDatabase.unpackZ(row);
						workers.submit(dimension.level().identifier() + " " + x * DhSection.WIDTH + ", " + z * DhSection.WIDTH, () -> {
							try {
								writer.write(x, z);
							} finally {
								this.job.advance();
							}
						});
					}
				}
			}
		}
	}

	private static final class Writer {
		private final VoxyReader voxy;
		private final DhDatabase database;
		private final Long2ObjectMap<IntList> columns;
		private final LongSet existing;
		private final String[] blocks;
		private final String[] biomes;
		private final int minY;
		private final int height;
		private final ThreadLocal<long[]> voxels;
		private final ThreadLocal<Runs> runs;

		Writer(LodWorld.Dimension dimension, VoxyReader voxy, DhDatabase database, Long2ObjectMap<IntList> columns) throws SQLException {
			this.voxy = voxy;
			this.database = database;
			this.columns = columns;
			this.existing = database.positions();
			this.minY = dimension.minY();
			this.height = dimension.height();
			this.voxels = ThreadLocal.withInitial(() -> new long[SECTION * SECTION * this.height]);
			this.runs = ThreadLocal.withInitial(() -> new Runs(this.height));
			this.blocks = voxy.blocks();
			this.biomes = voxy.biomes();
		}

		void write(int x, int z) throws IOException, SQLException {
			DhRow existingRow = this.existing.contains(DhDatabase.pack(x, z)) ? this.database.read(x, z) : null;
			DhSection area = existingRow == null ? DhSection.empty() : DhSection.decode(existingRow, this.database.format());
			Map<String, Integer> ids = new HashMap<>();
			for (int i = 0; i < area.mapping.size(); i++) {
				ids.putIfAbsent(area.mapping.get(i), i);
			}
			long[] voxels = this.voxels.get();
			boolean changed = false;
			for (int sectionX = 0; sectionX < SECTIONS_PER_ROW; sectionX++) {
				for (int sectionZ = 0; sectionZ < SECTIONS_PER_ROW; sectionZ++) {
					if (!this.load(x * SECTIONS_PER_ROW + sectionX, z * SECTIONS_PER_ROW + sectionZ, voxels)) {
						continue;
					}
					for (int localX = 0; localX < SECTION; localX++) {
						for (int localZ = 0; localZ < SECTION; localZ++) {
							int index = DhSection.index(sectionX * SECTION + localX, sectionZ * SECTION + localZ);
							if (area.columns[index].length > 0) {
								continue;
							}
							long[] points = this.column(voxels, localX, localZ, area.mapping, ids);
							if (points != null) {
								area.columns[index] = points;
								area.generationSteps[index] = DhSection.GENERATION_LIGHT;
								area.worldCompression[index] = 0;
								changed = true;
							}
						}
					}
				}
			}
			if (changed) {
				boolean applyToChildren = existingRow != null && existingRow.applyToChildren();
				boolean regenerate = existingRow != null && existingRow.regenerate();
				this.database.write(area.encode(x, z, this.database.format(), applyToChildren, regenerate));
			}
		}

		// Indexed like a Voxy section but as tall as the dimension
		private boolean load(int x, int z, long[] voxels) throws IOException {
			IntList ys = this.columns.get(DhDatabase.pack(x, z));
			if (ys == null) {
				return false;
			}
			Arrays.fill(voxels, 0);
			boolean loaded = false;
			for (int i = 0; i < ys.size(); i++) {
				int y = ys.getInt(i);
				long[] data = this.voxy.read(VoxyIds.sectionKey(0, x, y, z));
				if (data == null) {
					continue;
				}
				for (int localY = 0; localY < SECTION; localY++) {
					int relativeY = y * SECTION + localY - this.minY;
					if (relativeY >= 0 && relativeY < this.height) {
						System.arraycopy(data, localY * SECTION * SECTION, voxels, relativeY * SECTION * SECTION, SECTION * SECTION);
						loaded = true;
					}
				}
			}
			return loaded;
		}

		// Null where Voxy has no data
		private long @Nullable [] column(long[] voxels, int x, int z, List<String> mapping, Map<String, Integer> ids) {
			Runs runs = this.runs.get();
			int count = 0;
			boolean hasData = false;
			int y = this.height - 1;
			while (y >= 0) {
				long voxel = this.voxel(voxels, x, y, z);
				int block = VoxyIds.blockId(voxel);
				int biome = block == 0 ? -1 : VoxyIds.biomeId(voxel);
				hasData |= voxel != 0;
				int top = y;
				while (y > 0) {
					long below = this.voxel(voxels, x, y - 1, z);
					if (VoxyIds.blockId(below) != block || block != 0 && VoxyIds.biomeId(below) != biome) {
						break;
					}
					hasData |= below != 0;
					y--;
				}
				runs.top[count] = top;
				runs.bottom[count] = y;
				runs.block[count] = block;
				runs.biome[count] = biome;
				count++;
				y--;
			}
			if (!hasData) {
				return null;
			}
			// Distant Horizons doesn't store the air below the lowest block
			while (count > 1 && runs.block[count - 1] == 0) {
				count--;
			}
			long[] points = new long[count];
			for (int i = 0; i < count; i++) {
				String entry = DhStates.entry(this.biomeName(runs.biomeAt(i, count)), this.blockName(runs.block[i]));
				int id = ids.computeIfAbsent(entry, key -> {
					mapping.add(key);
					return mapping.size() - 1;
				});
				int skyLight = 15;
				int blockLight = 0;
				if (runs.top[i] + 1 < this.height) {
					int light = VoxyIds.light(this.voxel(voxels, x, runs.top[i] + 1, z));
					skyLight = light & 0xF;
					blockLight = light >> 4 & 0xF;
				}
				points[i] = DhSection.point(id, runs.top[i] - runs.bottom[i] + 1, runs.bottom[i], blockLight, skyLight);
			}
			return points;
		}

		private long voxel(long[] voxels, int x, int y, int z) {
			return voxels[y * SECTION * SECTION + z * SECTION + x];
		}

		private String blockName(int id) {
			return id > 0 && id < this.blocks.length && this.blocks[id] != null ? this.blocks[id] : DhStates.AIR;
		}

		private String biomeName(int id) {
			return id >= 0 && id < this.biomes.length && this.biomes[id] != null ? this.biomes[id] : DhStates.DEFAULT_BIOME;
		}
	}

	private static final class Runs {
		final int[] top;
		final int[] bottom;
		final int[] block;
		final int[] biome;

		Runs(int height) {
			this.top = new int[height];
			this.bottom = new int[height];
			this.block = new int[height];
			this.biome = new int[height];
		}

		// Voxy air has no biome. It borrows the one of the nearest block and prefers the one below.
		int biomeAt(int index, int count) {
			if (this.biome[index] >= 0) {
				return this.biome[index];
			}
			for (int i = index + 1; i < count; i++) {
				if (this.biome[i] >= 0) {
					return this.biome[i];
				}
			}
			for (int i = index - 1; i >= 0; i--) {
				if (this.biome[i] >= 0) {
					return this.biome[i];
				}
			}
			return -1;
		}
	}
}
