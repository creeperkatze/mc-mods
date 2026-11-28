package dev.creeperkatze.exchange.dh;

import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Columns list their data points from the top down
public final class DhSection {
	public static final int WIDTH = 64;
	public static final int COLUMNS = WIDTH * WIDTH;
	public static final byte GENERATION_EMPTY = 0;
	public static final byte GENERATION_LIGHT = 9;
	private static final byte GENERATION_SURFACE = 5;
	private static final byte GENERATION_FEATURES = 8;

	private static final byte FORMAT_NO_ADJACENT_DATA = 1;
	public static final byte FORMAT_NO_BLOCK_MODELS = 2;
	public static final byte FORMAT_BLOCK_MODELS = 3;
	private static final long[] EMPTY = new long[0];

	public final List<String> mapping;
	public final long[][] columns;
	public final byte[] generationSteps;
	public final byte[] worldCompression;

	private DhSection(List<String> mapping) {
		this.mapping = mapping;
		this.columns = new long[COLUMNS][];
		Arrays.fill(this.columns, EMPTY);
		this.generationSteps = new byte[COLUMNS];
		this.worldCompression = new byte[COLUMNS];
	}

	public static DhSection empty() {
		return new DhSection(new ArrayList<>());
	}

	public static int index(int x, int z) {
		return x * WIDTH + z;
	}

	// Same bit layout as Distant Horizons' FullDataPointUtil
	public static long point(int id, int height, int bottomY, int blockLight, int skyLight) {
		return (id & 0x7FFFFFFFL)
			| (long) (height & 0xFFF) << 32
			| (long) (bottomY & 0xFFF) << 44
			| (long) (skyLight & 0xF) << 56
			| (long) (blockLight & 0xF) << 60;
	}

	public static int id(long point) {
		return (int) (point & 0x7FFFFFFFL);
	}

	public static int height(long point) {
		return (int) (point >>> 32 & 0xFFF);
	}

	public static int bottomY(long point) {
		return (int) (point >>> 44 & 0xFFF);
	}

	public static int skyLight(long point) {
		return (int) (point >>> 56 & 0xF);
	}

	public static int blockLight(long point) {
		return (int) (point >>> 60 & 0xF);
	}

	public boolean hasData() {
		for (long[] column : this.columns) {
			if (column.length > 0) {
				return true;
			}
		}
		return false;
	}

	public static DhSection decode(DhRow row, byte targetFormat) throws IOException {
		DhSection section = new DhSection(readMapping(row));
		readBytes(row.compression(), row.generationSteps(), section.generationSteps);
		readBytes(row.compression(), row.worldCompression(), section.worldCompression);
		if (row.format() == FORMAT_NO_ADJACENT_DATA) {
			section.readColumnsV1(row.compression(), row.data());
		} else {
			section.readColumns(row.compression(), row.data(), 1, WIDTH - 1, 1, WIDTH - 1);
			section.readColumns(row.compression(), row.north(), 0, WIDTH, 0, 1);
			section.readColumns(row.compression(), row.south(), 0, WIDTH, WIDTH - 1, WIDTH);
			section.readColumns(row.compression(), row.east(), WIDTH - 1, WIDTH, 0, WIDTH);
			section.readColumns(row.compression(), row.west(), 0, 1, 0, WIDTH);
		}
		// Distant Horizons generates columns from before block models again
		if (row.format() < FORMAT_BLOCK_MODELS && targetFormat >= FORMAT_BLOCK_MODELS) {
			for (int i = 0; i < COLUMNS; i++) {
				if (section.generationSteps[i] >= GENERATION_FEATURES) {
					section.generationSteps[i] = GENERATION_SURFACE;
				}
			}
		}
		return section;
	}

	public DhRow encode(int x, int z, byte format, boolean applyToChildren, boolean regenerate) throws IOException {
		return new DhRow(
			x,
			z,
			format,
			DhCompression.LZ4,
			this.writeColumns(1, WIDTH - 1, 1, WIDTH - 1),
			this.writeColumns(0, WIDTH, 0, 1),
			this.writeColumns(0, WIDTH, WIDTH - 1, WIDTH),
			this.writeColumns(WIDTH - 1, WIDTH, 0, WIDTH),
			this.writeColumns(0, 1, 0, WIDTH),
			writeBytes(this.generationSteps),
			writeBytes(this.worldCompression),
			this.writeMapping(),
			applyToChildren,
			regenerate
		);
	}

	private static List<String> readMapping(DhRow row) throws IOException {
		try (DataInputStream in = new DataInputStream(DhCompression.input(row.compression(), row.mapping()))) {
			int count = in.readInt();
			if (count < 0) {
				throw new IOException("Negative mapping size " + count);
			}
			List<String> mapping = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				mapping.add(in.readUTF());
			}
			return mapping;
		}
	}

	private byte[] writeMapping() throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(DhCompression.lz4Output(bytes))) {
			out.writeInt(this.mapping.size());
			for (String entry : this.mapping) {
				out.writeUTF(entry);
			}
		}
		return bytes.toByteArray();
	}

	private static void readBytes(byte compression, byte[] data, byte[] into) throws IOException {
		try (DataInputStream in = new DataInputStream(DhCompression.input(compression, data))) {
			in.readFully(into);
		}
	}

	private static byte[] writeBytes(byte[] data) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (OutputStream out = DhCompression.lz4Output(bytes)) {
			out.write(data);
		}
		return bytes.toByteArray();
	}

	private void readColumnsV1(byte compression, byte[] data) throws IOException {
		try (DataInputStream in = new DataInputStream(DhCompression.input(compression, data))) {
			for (int i = 0; i < COLUMNS; i++) {
				int size = in.readShort();
				if (size < 0) {
					throw new IOException("Negative column size " + size);
				}
				long[] column = new long[size];
				for (int y = 0; y < size; y++) {
					column[y] = in.readLong();
				}
				this.columns[i] = column;
			}
		}
	}

	// Format 2 and later store each field in its own pass
	private void readColumns(byte compression, byte @Nullable [] data, int minX, int maxX, int minZ, int maxZ) throws IOException {
		if (data == null) {
			return;
		}
		try (DataInputStream in = new DataInputStream(DhCompression.input(compression, data))) {
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					this.columns[index(x, z)] = new long[readVarint(in)];
				}
			}
			// Bit 0 marks a gap below the previous point and bit 1 a stored light value
			int[] flags = new int[this.count(minX, maxX, minZ, maxZ)];
			int point = 0;
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					long[] column = this.columns[index(x, z)];
					for (int i = 0; i < column.length; i++) {
						int encoded = readVarint(in);
						column[i] = point(encoded >>> 2, 0, 0, 0, 0);
						flags[point++] = encoded & 3;
					}
				}
			}
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					long[] column = this.columns[index(x, z)];
					for (int i = 0; i < column.length; i++) {
						column[i] = point(id(column[i]), readVarint(in), 0, 0, 0);
					}
				}
			}
			int previousBottom = 0;
			point = 0;
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					long[] column = this.columns[index(x, z)];
					for (int i = 0; i < column.length; i++) {
						int error = (flags[point++] & 1) != 0 ? zigzagDecode(readVarint(in)) : 0;
						int bottom = previousBottom - height(column[i]) + error;
						column[i] = point(id(column[i]), height(column[i]), bottom, 0, 0);
						previousBottom = bottom;
					}
				}
			}
			point = 0;
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					long[] column = this.columns[index(x, z)];
					for (int i = 0; i < column.length; i++) {
						if ((flags[point++] & 2) != 0) {
							int light = in.readUnsignedByte();
							column[i] = point(id(column[i]), height(column[i]), bottomY(column[i]), light >> 4, light & 0xF);
						}
					}
				}
			}
		}
	}

	private byte[] writeColumns(int minX, int maxX, int minZ, int maxZ) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(DhCompression.lz4Output(bytes))) {
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					writeVarint(out, this.columns[index(x, z)].length);
				}
			}
			int previousBottom = 0;
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					for (long point : this.columns[index(x, z)]) {
						boolean hasLight = (blockLight(point) | skyLight(point)) != 0;
						boolean gap = bottomY(point) != previousBottom - height(point);
						previousBottom = bottomY(point);
						writeVarint(out, id(point) << 2 | (hasLight ? 2 : 0) | (gap ? 1 : 0));
					}
				}
			}
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					for (long point : this.columns[index(x, z)]) {
						writeVarint(out, height(point));
					}
				}
			}
			previousBottom = 0;
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					for (long point : this.columns[index(x, z)]) {
						int expected = previousBottom - height(point);
						if (bottomY(point) != expected) {
							writeVarint(out, zigzagEncode(bottomY(point) - expected));
						}
						previousBottom = bottomY(point);
					}
				}
			}
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					for (long point : this.columns[index(x, z)]) {
						int light = blockLight(point) << 4 | skyLight(point);
						if (light != 0) {
							out.writeByte(light);
						}
					}
				}
			}
		}
		return bytes.toByteArray();
	}

	private int count(int minX, int maxX, int minZ, int maxZ) {
		int count = 0;
		for (int x = minX; x < maxX; x++) {
			for (int z = minZ; z < maxZ; z++) {
				count += this.columns[index(x, z)].length;
			}
		}
		return count;
	}

	private static int readVarint(DataInputStream in) throws IOException {
		int value = 0;
		for (int shift = 0; shift < 32; shift += 7) {
			int b = in.readUnsignedByte();
			value |= (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return value;
			}
		}
		throw new IOException("Varint too long");
	}

	private static void writeVarint(DataOutputStream out, int value) throws IOException {
		while ((value & ~0x7F) != 0) {
			out.writeByte(value & 0x7F | 0x80);
			value >>>= 7;
		}
		out.writeByte(value);
	}

	private static int zigzagEncode(int value) {
		return value << 1 ^ value >> 31;
	}

	private static int zigzagDecode(int value) {
		return value >>> 1 ^ -(value & 1);
	}
}
