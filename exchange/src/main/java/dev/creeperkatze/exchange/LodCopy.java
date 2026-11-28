package dev.creeperkatze.exchange;

import it.unimi.dsi.fastutil.longs.LongCollection;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicLong;

public final class LodCopy {
	public enum State {
		RUNNING,
		COMPLETED,
		STOPPED,
		FAILED
	}

	private final LodWorld world;
	private final LodMod target;
	private final AtomicLong done = new AtomicLong();
	private volatile long total;
	private volatile @Nullable ProgressGrid grid;
	private volatile LodWorld.@Nullable Dimension dimension;
	private volatile boolean stopped;
	private volatile State state = State.RUNNING;

	public LodCopy(LodWorld world, LodMod target) {
		this.world = world;
		this.target = target;
	}

	public void start() {
		Thread thread = new Thread(this::run, "Exchange");
		thread.setDaemon(true);
		thread.start();
	}

	private void run() {
		try {
			if (this.target == LodMod.VOXY) {
				new ToVoxy(this.world, this).run();
			} else {
				new ToDistantHorizons(this.world, this).run();
			}
			this.state = this.stopped ? State.STOPPED : State.COMPLETED;
		} catch (Throwable e) {
			Exchange.LOGGER.error("Failed to copy the LODs to {}", this.target.key(), e);
			this.state = State.FAILED;
		}
	}

	public void stop() {
		this.stopped = true;
	}

	public boolean isStopped() {
		return this.stopped;
	}

	public State state() {
		return this.state;
	}

	public LodMod target() {
		return this.target;
	}

	// Takes the Distant Horizons rows the dimension will copy as packed positions
	void startDimension(LodWorld.Dimension dimension, LongCollection rows) {
		this.dimension = dimension;
		this.done.set(0);
		this.total = rows.size();
		this.grid = ProgressGrid.of(rows);
	}

	void update(int x, int z, ProgressGrid.Stage stage) {
		if (stage == ProgressGrid.Stage.DONE) {
			this.done.incrementAndGet();
		}
		ProgressGrid grid = this.grid;
		if (grid != null) {
			grid.update(x, z, stage);
		}
	}

	public LodWorld.@Nullable Dimension dimension() {
		return this.dimension;
	}

	public @Nullable ProgressGrid grid() {
		return this.grid;
	}

	public long done() {
		return this.done.get();
	}

	public long total() {
		return this.total;
	}
}
