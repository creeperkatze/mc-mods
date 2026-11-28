package dev.creeperkatze.exchange.voxy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import dev.creeperkatze.exchange.Exchange;
import dev.creeperkatze.exchange.dh.DhStates;
import io.airlift.compress.zstd.ZstdInputStream;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.jpountz.lz4.LZ4Factory;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Reads Voxy's storage without Voxy installed
public final class VoxyReader implements AutoCloseable {
	private static final String SECTIONS = "world_sections";
	private static final String MAPPINGS = "id_mappings";
	private static final int BLOCK_STATE_MAPPING = 1;
	private static final int BIOME_MAPPING = 2;
	private static final int HEADER_BYTES = 16;

	private enum Compression {
		NONE,
		ZSTD,
		LZ4
	}

	private final RocksDB database;
	private final List<ColumnFamilyHandle> handles;
	private final ColumnFamilyHandle sections;
	private final Compression compression;
	private final String[] blocks;
	private final String[] biomes;

	private VoxyReader(RocksDB database, List<ColumnFamilyHandle> handles, ColumnFamilyHandle sections, ColumnFamilyHandle mappings, Compression compression) throws IOException {
		this.database = database;
		this.handles = handles;
		this.sections = sections;
		this.compression = compression;
		List<String> blocks = new ArrayList<>();
		List<String> biomes = new ArrayList<>();
		try (RocksIterator iterator = database.newIterator(mappings)) {
			for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
				int key = ByteBuffer.wrap(iterator.key()).getInt();
				int id = key & (1 << 30) - 1;
				CompoundTag tag = NbtIo.readCompressed(new ByteArrayInputStream(iterator.value()), NbtAccounter.unlimitedHeap());
				switch (key >>> 30) {
					case BLOCK_STATE_MAPPING -> set(blocks, id, blockState(tag));
					case BIOME_MAPPING -> set(biomes, id, tag.getStringOr("biome_id", DhStates.DEFAULT_BIOME));
					default -> Exchange.LOGGER.warn("Skipped a Voxy id mapping of unknown type {}", key >>> 30);
				}
			}
		}
		this.blocks = blocks.toArray(String[]::new);
		this.biomes = biomes.toArray(String[]::new);
	}

	public static VoxyReader open(Path basePath, String worldId) throws IOException {
		Compression compression = compression(basePath.resolve("config.json"));
		Path storage = basePath.resolve(worldId).resolve("storage");
		RocksDB.loadLibrary();
		try (Options options = new Options()) {
			List<ColumnFamilyDescriptor> descriptors = new ArrayList<>();
			for (byte[] name : RocksDB.listColumnFamilies(options, storage.toString())) {
				descriptors.add(new ColumnFamilyDescriptor(name, new ColumnFamilyOptions()));
			}
			List<ColumnFamilyHandle> handles = new ArrayList<>();
			// Read only mode also replays the write ahead log. Voxy keeps its latest changes there.
			RocksDB database = RocksDB.openReadOnly(new DBOptions(), storage.toString(), descriptors, handles);
			ColumnFamilyHandle sections = null;
			ColumnFamilyHandle mappings = null;
			for (ColumnFamilyHandle handle : handles) {
				String name = new String(handle.getName(), StandardCharsets.UTF_8);
				if (name.equals(SECTIONS)) {
					sections = handle;
				} else if (name.equals(MAPPINGS)) {
					mappings = handle;
				}
			}
			if (sections == null || mappings == null) {
				handles.forEach(ColumnFamilyHandle::close);
				database.close();
				throw new IOException("Voxy's storage at " + storage + " is missing its sections or id mappings");
			}
			return new VoxyReader(database, handles, sections, mappings, compression);
		} catch (RocksDBException e) {
			throw new IOException("Failed to open Voxy's storage at " + storage, e);
		}
	}

	// Supports Voxy's default RocksDB storage with an optional compressor
	private static Compression compression(Path config) throws IOException {
		if (!Files.isRegularFile(config)) {
			return Compression.ZSTD;
		}
		JsonObject storage = JsonParser.parseString(Files.readString(config)).getAsJsonObject().getAsJsonObject("sectionStorageConfig");
		if (!type(storage).equals("Serializer")) {
			throw new IOException("Unsupported Voxy storage " + type(storage));
		}
		JsonObject backend = storage.getAsJsonObject("storage");
		Compression compression = Compression.NONE;
		if (type(backend).equals("CompressionAdaptor")) {
			String compressor = type(backend.getAsJsonObject("compressor"));
			compression = switch (compressor) {
				case "ZSTD" -> Compression.ZSTD;
				case "LZ4" -> Compression.LZ4;
				default -> throw new IOException("Unsupported Voxy compressor " + compressor);
			};
			backend = backend.getAsJsonObject("delegate");
		}
		if (!type(backend).equals("RocksDB")) {
			throw new IOException("Unsupported Voxy storage backend " + type(backend));
		}
		return compression;
	}

	private static String type(JsonObject config) {
		return config.has("TYPE") ? config.get("TYPE").getAsString() : "";
	}

	private static String blockState(CompoundTag tag) {
		Tag state = tag.get("block_state");
		if (state == null) {
			return DhStates.AIR;
		}
		int current = SharedConstants.getCurrentVersion().dataVersion().version();
		int version = tag.getIntOr("version", current);
		Dynamic<Tag> dynamic = new Dynamic<>(NbtOps.INSTANCE, state);
		if (version != current) {
			dynamic = DataFixers.getDataFixer().update(References.BLOCK_STATE, dynamic, version, current);
		}
		return BlockState.CODEC.parse(dynamic).result().map(DhStates::serialize).orElse(DhStates.AIR);
	}

	private static void set(List<String> list, int index, String value) {
		while (list.size() <= index) {
			list.add(null);
		}
		list.set(index, value);
	}

	// Distant Horizons names indexed by Voxy block id
	public String[] blocks() {
		return this.blocks;
	}

	public String[] biomes() {
		return this.biomes;
	}

	public LongList sections(int level) {
		LongList keys = new LongArrayList();
		try (RocksIterator iterator = this.database.newIterator(this.sections)) {
			iterator.seek(key(VoxyIds.sectionKey(level, 0, 0, 0) & 0xF000000000000000L));
			for (; iterator.isValid(); iterator.next()) {
				long key = ByteBuffer.wrap(iterator.key()).getLong();
				if (VoxyIds.level(key) != level) {
					break;
				}
				keys.add(key);
			}
		}
		return keys;
	}

	// Indexed like VoxyIds.index. Null if Voxy never saved the section.
	public long @Nullable [] read(long key) throws IOException {
		byte[] stored;
		try {
			stored = this.database.get(this.sections, key(key));
		} catch (RocksDBException e) {
			throw new IOException(e);
		}
		if (stored == null) {
			return null;
		}
		ByteBuffer data = ByteBuffer.wrap(this.decompress(stored)).order(ByteOrder.LITTLE_ENDIAN);
		if (data.getLong() != key) {
			throw new IOException("Voxy section " + Long.toHexString(key) + " holds another section");
		}
		int paletteSize = (int) (data.getLong() & 0xFFFF);
		int paletteStart = HEADER_BYTES + VoxyIds.SECTION_VOLUME * Short.BYTES;
		long[] palette = new long[paletteSize];
		for (int i = 0; i < paletteSize; i++) {
			palette[i] = data.getLong(paletteStart + i * Long.BYTES);
		}
		long[] voxels = new long[VoxyIds.SECTION_VOLUME];
		for (int i = 0; i < voxels.length; i++) {
			int entry = Short.toUnsignedInt(data.getShort(HEADER_BYTES + i * Short.BYTES));
			voxels[i] = entry < paletteSize ? palette[entry] : 0;
		}
		return voxels;
	}

	private byte[] decompress(byte[] stored) throws IOException {
		return switch (this.compression) {
			case NONE -> stored;
			case ZSTD -> {
				try (InputStream in = new ZstdInputStream(new ByteArrayInputStream(stored))) {
					yield in.readAllBytes();
				}
			}
			case LZ4 -> {
				int size = ByteBuffer.wrap(stored).order(ByteOrder.LITTLE_ENDIAN).getInt();
				yield LZ4Factory.fastestInstance().fastDecompressor().decompress(Arrays.copyOfRange(stored, 4, stored.length), size);
			}
		};
	}

	// Voxy stores keys big endian to sort them by level
	private static byte[] key(long key) {
		return ByteBuffer.allocate(Long.BYTES).putLong(key).array();
	}

	@Override
	public void close() {
		this.handles.forEach(ColumnFamilyHandle::close);
		this.database.close();
	}
}
