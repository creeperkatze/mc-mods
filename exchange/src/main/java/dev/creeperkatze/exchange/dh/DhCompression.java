package dev.creeperkatze.exchange.dh;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.jpountz.xxhash.XXHashFactory;
import org.lwjgl.util.zstd.Zstd;
import org.lwjgl.util.zstd.ZstdErrors;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

// The compression modes of Distant Horizons. LZ4 ships with Minecraft, XZ and Zstd come with Voxy
final class DhCompression {
	static final byte UNCOMPRESSED = 0;
	static final byte LZ4 = 1;
	static final byte ZSTD_STREAM = 2;
	static final byte LZMA2 = 3;
	static final byte ZSTD_BLOCK = 4;

	private static final int MAX_ZSTD_SIZE = 1 << 28;

	private DhCompression() {
	}

	static InputStream input(byte mode, byte[] data) throws IOException {
		return switch (mode) {
			case UNCOMPRESSED -> new ByteArrayInputStream(data);
			case LZ4 -> new LZ4FrameInputStream(new ByteArrayInputStream(data));
			case ZSTD_STREAM, ZSTD_BLOCK -> new ByteArrayInputStream(zstd(data));
			case LZMA2 -> new XZInputStream(new ByteArrayInputStream(data));
			default -> throw new IOException("Unknown Distant Horizons compression mode " + mode);
		};
	}

	// The same LZ4 frame settings Distant Horizons writes
	static OutputStream lz4Output(OutputStream out) throws IOException {
		return new LZ4FrameOutputStream(
			out,
			LZ4FrameOutputStream.BLOCKSIZE.SIZE_64KB,
			-1L,
			LZ4Factory.fastestInstance().fastCompressor(),
			XXHashFactory.fastestInstance().hash32(),
			LZ4FrameOutputStream.FLG.Bits.BLOCK_INDEPENDENCE
		);
	}

	private static byte[] zstd(byte[] data) throws IOException {
		ByteBuffer source = ByteBuffer.allocateDirect(data.length).put(data).flip();
		long contentSize = Zstd.ZSTD_getFrameContentSize(source);
		// Streamed frames don't store their size
		int capacity = contentSize >= 0 && contentSize <= MAX_ZSTD_SIZE ? (int) contentSize : data.length * 8;
		while (true) {
			ByteBuffer target = ByteBuffer.allocateDirect(Math.max(capacity, 64));
			long result = Zstd.ZSTD_decompress(target, source);
			if (!Zstd.ZSTD_isError(result)) {
				byte[] out = new byte[(int) result];
				target.get(0, out);
				return out;
			}
			if (Zstd.ZSTD_getErrorCode(result) != ZstdErrors.ZSTD_error_dstSize_tooSmall || capacity >= MAX_ZSTD_SIZE) {
				throw new IOException("Zstd failed: " + Zstd.ZSTD_getErrorName(result));
			}
			capacity *= 2;
		}
	}
}
