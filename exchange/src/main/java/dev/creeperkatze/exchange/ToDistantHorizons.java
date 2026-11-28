package dev.creeperkatze.exchange;

import dev.creeperkatze.exchange.dh.DhDatabase;
import dev.creeperkatze.exchange.dh.DhRow;
import dev.creeperkatze.exchange.dh.DhSection;
import dev.creeperkatze.exchange.dh.DhStates;
import dev.creeperkatze.exchange.voxy.VoxyIds;
import dev.creeperkatze.exchange.voxy.VoxyReader;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
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
	private static final int COLUMNS = SECTION * SECTION;

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
			this.voxels = ThreadLocal.withInitial(() -> new long[COLUMNS * this.height]);
			this.runs = ThreadLocal.withInitial(() -> new Runs(this.height));
			this.blocks = voxy.blocks();
			this.biomes = voxy.biomes();
		}

		void write(int x, int z) throws IOException, SQLException {
			DhRow existingRow = this.existing.contains(DhDatabase.pack(x, z)) ? this.database.read(x, z) : null;
			DhSection area = existingRow == null ? DhSection.empty() : DhSection.decode(existingRow, this.database.format());
			Map<String, Integer> idsByEntry = new HashMap<>();
			for (int i = 0; i < area.mapping.size(); i++) {
				idsByEntry.putIfAbsent(area.mapping.get(i), i);
			}
			Long2IntMap ids = new Long2IntOpenHashMap();
			long[] voxels = this.voxels.get();
			Runs runs = this.runs.get();
			boolean changed = false;
			for (int sectionX = 0; sectionX < SECTIONS_PER_ROW; sectionX++) {
				for (int sectionZ = 0; sectionZ < SECTIONS_PER_ROW; sectionZ++) {
					if (!this.load(x * SECTIONS_PER_ROW + sectionX, z * SECTIONS_PER_ROW + sectionZ, voxels)) {
						continue;
					}
					runs.sweep(voxels);
					for (int localX = 0; localX < SECTION; localX++) {
						for (int localZ = 0; localZ < SECTION; localZ++) {
							int index = DhSection.index(sectionX * SECTION + localX, sectionZ * SECTION + localZ);
							if (area.columns[index].length > 0) {
								continue;
							}
							long[] points = this.points(voxels, runs, localZ * SECTION + localX, area.mapping, idsByEntry, ids);
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
			boolean loaded = false;
			for (int y = Math.floorDiv(this.minY, SECTION); y <= Math.floorDiv(this.minY + this.height - 1, SECTION); y++) {
				int firstLayer = y * SECTION - this.minY;
				if (ys.contains(y) && this.voxy.read(VoxyIds.sectionKey(0, x, y, z), voxels, firstLayer, this.height)) {
					loaded = true;
				} else {
					Arrays.fill(voxels, Math.max(0, firstLayer) * COLUMNS, Math.min(this.height, firstLayer + SECTION) * COLUMNS, 0);
				}
			}
			return loaded;
		}

		// Null where Voxy has no data
		private long @Nullable [] points(long[] voxels, Runs runs, int column, List<String> mapping, Map<String, Integer> idsByEntry, Long2IntMap ids) {
			if (!runs.hasData[column]) {
				return null;
			}
			int first = column * this.height;
			int count = runs.count[column];
			// Distant Horizons doesn't store the air below the lowest block
			while (count > 1 && runs.key[first + count - 1] == 0) {
				count--;
			}
			long[] points = new long[count];
			for (int i = 0; i < count; i++) {
				int run = first + i;
				int block = VoxyIds.blockId(runs.key[run]);
				int biome = runs.biomeAt(first, i, count);
				int id = ids.computeIfAbsent((long) block << 32 | biome & 0xFFFFFFFFL, key -> idsByEntry.computeIfAbsent(
					DhStates.entry(this.biomeName(biome), this.blockName(block)), entry -> {
						mapping.add(entry);
						return mapping.size() - 1;
					}
				));
				int top = runs.top[run];
				int skyLight = 15;
				int blockLight = 0;
				if (top + 1 < this.height) {
					int light = VoxyIds.light(voxels[(top + 1) * COLUMNS + column]);
					skyLight = light & 0xF;
					blockLight = light >> 4 & 0xF;
				}
				points[i] = DhSection.point(id, top - runs.bottom[run] + 1, runs.bottom[run], blockLight, skyLight);
			}
			return points;
		}

		private String blockName(int id) {
			return id > 0 && id < this.blocks.length && this.blocks[id] != null ? this.blocks[id] : DhStates.AIR;
		}

		private String biomeName(int id) {
			return id >= 0 && id < this.biomes.length && this.biomes[id] != null ? this.biomes[id] : DhStates.DEFAULT_BIOME;
		}
	}

	// Runs of equal voxels for every column of a Voxy column, stored per column from the top down
	private static final class Runs {
		private static final long BLOCK_AND_BIOME = 0x00FF_FFFF_F800_0000L;

		final int height;
		final int[] top;
		final int[] bottom;
		final long[] key;
		final int[] count = new int[COLUMNS];
		final boolean[] hasData = new boolean[COLUMNS];
		private final long[] currentKey = new long[COLUMNS];
		private final int[] currentTop = new int[COLUMNS];

		Runs(int height) {
			this.height = height;
			this.top = new int[COLUMNS * height];
			this.bottom = new int[COLUMNS * height];
			this.key = new long[COLUMNS * height];
		}

		// Walks layer by layer in memory order. Walking one column at a time misses the cache on every voxel.
		void sweep(long[] voxels) {
			int topLayer = (this.height - 1) * COLUMNS;
			for (int column = 0; column < COLUMNS; column++) {
				this.currentKey[column] = key(voxels[topLayer + column]);
				this.currentTop[column] = this.height - 1;
				this.count[column] = 0;
				this.hasData[column] = false;
			}
			for (int y = this.height - 1; y >= 0; y--) {
				int layer = y * COLUMNS;
				// A layer like the one above can't start a run, which skips most of the sky
				if (y < this.height - 1 && Arrays.equals(voxels, layer, layer + COLUMNS, voxels, layer + COLUMNS, layer + 2 * COLUMNS)) {
					continue;
				}
				for (int column = 0; column < COLUMNS; column++) {
					long voxel = voxels[layer + column];
					this.hasData[column] |= voxel != 0;
					long key = key(voxel);
					if (key != this.currentKey[column]) {
						this.add(column, y + 1, this.currentKey[column]);
						this.currentKey[column] = key;
						this.currentTop[column] = y;
					}
				}
			}
			for (int column = 0; column < COLUMNS; column++) {
				this.add(column, 0, this.currentKey[column]);
			}
		}

		private void add(int column, int bottom, long key) {
			int run = column * this.height + this.count[column]++;
			this.top[run] = this.currentTop[column];
			this.bottom[run] = bottom;
			this.key[run] = key;
		}

		// Voxy air has no biome. It borrows the one of the nearest block and prefers the one below.
		int biomeAt(int first, int index, int count) {
			for (int i = index; i < count; i++) {
				if (this.key[first + i] != 0) {
					return VoxyIds.biomeId(this.key[first + i]);
				}
			}
			for (int i = index - 1; i >= 0; i--) {
				if (this.key[first + i] != 0) {
					return VoxyIds.biomeId(this.key[first + i]);
				}
			}
			return -1;
		}

		// Air keeps no biome so all air in a column forms one run
		private static long key(long voxel) {
			return VoxyIds.blockId(voxel) == 0 ? 0 : voxel & BLOCK_AND_BIOME;
		}
	}
}
