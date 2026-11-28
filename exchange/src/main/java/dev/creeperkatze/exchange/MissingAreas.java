package dev.creeperkatze.exchange;

import dev.creeperkatze.exchange.dh.DhDatabase;
import dev.creeperkatze.exchange.voxy.VoxyIds;
import dev.creeperkatze.exchange.voxy.VoxyReader;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

// Counts whole Voxy sections because finding single chunks means decoding every LOD
final class MissingAreas {
	private static final int CHUNKS_PER_SECTION = 4;

	private MissingAreas() {
	}

	// The chunks each target is missing that the other mod has
	static Map<LodMod, Long> count(LodWorld world) throws IOException, SQLException {
		long toVoxy = 0;
		long toDistantHorizons = 0;
		for (LodWorld.Dimension dimension : world.dimensions()) {
			LongSet rows = dhRows(dimension);
			LongSet columns = voxyColumns(world, dimension);
			// A Distant Horizons row covers 2 by 2 Voxy sections
			for (long column : columns) {
				if (!rows.contains(DhDatabase.pack(DhDatabase.unpackX(column) >> 1, DhDatabase.unpackZ(column) >> 1))) {
					toDistantHorizons++;
				}
			}
			for (long row : rows) {
				for (int x = 0; x < 2; x++) {
					for (int z = 0; z < 2; z++) {
						if (!columns.contains(DhDatabase.pack(DhDatabase.unpackX(row) * 2 + x, DhDatabase.unpackZ(row) * 2 + z))) {
							toVoxy++;
						}
					}
				}
			}
		}
		Map<LodMod, Long> missing = new EnumMap<>(LodMod.class);
		missing.put(LodMod.VOXY, toVoxy * CHUNKS_PER_SECTION);
		missing.put(LodMod.DISTANT_HORIZONS, toDistantHorizons * CHUNKS_PER_SECTION);
		return missing;
	}

	private static LongSet dhRows(LodWorld.Dimension dimension) throws IOException, SQLException {
		if (!Files.isRegularFile(dimension.dhDatabase())) {
			return new LongOpenHashSet();
		}
		try (DhDatabase database = DhDatabase.open(dimension.dhDatabase(), false)) {
			return database.hasLods() ? database.positions() : new LongOpenHashSet();
		}
	}

	private static LongSet voxyColumns(LodWorld world, LodWorld.Dimension dimension) throws IOException {
		LongSet columns = new LongOpenHashSet();
		String worldId = VoxyIds.worldId(dimension.level(), world.seed());
		Path storage = world.voxyPath().resolve(worldId).resolve("storage");
		if (!Files.isDirectory(storage)) {
			return columns;
		}
		try (VoxyReader voxy = VoxyReader.open(world.voxyPath(), worldId)) {
			for (long key : voxy.sections(0)) {
				columns.add(DhDatabase.pack(VoxyIds.x(key), VoxyIds.z(key)));
			}
		}
		return columns;
	}
}
