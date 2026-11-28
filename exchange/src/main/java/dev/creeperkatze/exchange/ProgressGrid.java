package dev.creeperkatze.exchange;

import dev.creeperkatze.exchange.dh.DhDatabase;
import it.unimi.dsi.fastutil.longs.LongCollection;

import java.util.concurrent.atomic.AtomicIntegerArray;

// The copy stage of every area in the dimension, in a grid the size of vanilla's chunk loading map
public final class ProgressGrid {
	// Vanilla's map was this many chunks wide back when the spawn radius was 11
	public static final int MAX_CELLS = 23;

	public enum Stage {
		WAITING,
		QUEUED,
		READING,
		WRITING,
		DONE
	}

	private static final Stage[] STAGES = Stage.values();

	private final int minX;
	private final int minZ;
	private final int scale;
	private final int width;
	private final int height;
	private final int[] rows;
	private final AtomicIntegerArray remaining;
	private final AtomicIntegerArray stages;

	private ProgressGrid(LongCollection positions) {
		int minX = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for (long position : positions) {
			minX = Math.min(minX, DhDatabase.unpackX(position));
			minZ = Math.min(minZ, DhDatabase.unpackZ(position));
			maxX = Math.max(maxX, DhDatabase.unpackX(position));
			maxZ = Math.max(maxZ, DhDatabase.unpackZ(position));
		}
		if (positions.isEmpty()) {
			minX = minZ = maxX = maxZ = 0;
		}
		this.minX = minX;
		this.minZ = minZ;
		// Big worlds put several areas into one cell
		this.scale = Math.max(1, Math.ceilDiv(Math.max(maxX - minX + 1, maxZ - minZ + 1), MAX_CELLS));
		this.width = Math.ceilDiv(maxX - minX + 1, this.scale);
		this.height = Math.ceilDiv(maxZ - minZ + 1, this.scale);
		this.rows = new int[this.width * this.height];
		for (long position : positions) {
			this.rows[this.cell(DhDatabase.unpackX(position), DhDatabase.unpackZ(position))]++;
		}
		this.remaining = new AtomicIntegerArray(this.rows);
		this.stages = new AtomicIntegerArray(this.rows.length);
	}

	// Takes Distant Horizons rows as packed positions
	static ProgressGrid of(LongCollection positions) {
		return new ProgressGrid(positions);
	}

	public int width() {
		return this.width;
	}

	public int height() {
		return this.height;
	}

	public boolean hasLods(int x, int z) {
		return this.rows[z * this.width + x] > 0;
	}

	// The furthest stage any area of the cell reached, but done only once all of them are
	public Stage stage(int x, int z) {
		int cell = z * this.width + x;
		Stage stage = STAGES[this.stages.get(cell)];
		return stage == Stage.DONE && this.remaining.get(cell) > 0 ? Stage.WRITING : stage;
	}

	void update(int x, int z, Stage stage) {
		int cell = this.cell(x, z);
		this.stages.accumulateAndGet(cell, stage.ordinal(), Math::max);
		if (stage == Stage.DONE) {
			this.remaining.decrementAndGet(cell);
		}
	}

	// Copying cell by cell lets cells finish one after another instead of all at the end
	int cell(long position) {
		return this.cell(DhDatabase.unpackX(position), DhDatabase.unpackZ(position));
	}

	private int cell(int x, int z) {
		return (z - this.minZ) / this.scale * this.width + (x - this.minX) / this.scale;
	}
}
