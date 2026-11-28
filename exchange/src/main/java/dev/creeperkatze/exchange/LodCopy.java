package dev.creeperkatze.exchange;

import net.minecraft.network.chat.Component;
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
	private volatile LodWorld.@Nullable Dimension dimension;
	private volatile int dimensionIndex;
	private volatile int dimensionCount;
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

	void startDimension(LodWorld.Dimension dimension, int index, int count, long total) {
		this.done.set(0);
		this.total = total;
		this.dimensionIndex = index;
		this.dimensionCount = count;
		this.dimension = dimension;
	}

	void advance() {
		this.done.incrementAndGet();
	}

	public long done() {
		return this.done.get();
	}

	public long total() {
		return this.total;
	}

	public Component status() {
		LodWorld.@Nullable Dimension dimension = this.dimension;
		if (dimension == null) {
			return Component.translatable("exchange.progress.preparing");
		}
		return Component.translatable("exchange.progress.dimension", dimension.name(), this.dimensionIndex + 1, this.dimensionCount);
	}
}
