package dev.creeperkatze.exchange.dh;

import io.airlift.compress.zstd.ZstdInputStream;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.jpountz.xxhash.XXHashFactory;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class DhCompression {
	static final byte UNCOMPRESSED = 0;
	static final byte LZ4 = 1;
	static final byte ZSTD_STREAM = 2;
	static final byte LZMA2 = 3;
	static final byte ZSTD_BLOCK = 4;

	private DhCompression() {
	}

	static InputStream input(byte mode, byte[] data) throws IOException {
		return switch (mode) {
			case UNCOMPRESSED -> new ByteArrayInputStream(data);
			case LZ4 -> new LZ4FrameInputStream(new ByteArrayInputStream(data));
			case ZSTD_STREAM, ZSTD_BLOCK -> new ZstdInputStream(new ByteArrayInputStream(data));
			case LZMA2 -> new XZInputStream(new ByteArrayInputStream(data));
			default -> throw new IOException("Unknown Distant Horizons compression mode " + mode);
		};
	}

	// Same LZ4 frame settings as Distant Horizons
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

}
