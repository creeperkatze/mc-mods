package dev.creeperkatze.exchange;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Converts many areas at once and keeps going when one of them fails
final class Workers implements AutoCloseable {
	private static final int THREADS = Math.clamp(Runtime.getRuntime().availableProcessors() - 2, 1, 8);
	private static final int MAX_LOGGED_FAILURES = 10;

	private final ThreadPoolExecutor executor;
	private final AtomicInteger failures = new AtomicInteger();

	interface Task {
		void run() throws Exception;
	}

	Workers() {
		AtomicInteger count = new AtomicInteger();
		// Running full queues on the submitting thread keeps memory bounded
		this.executor = new ThreadPoolExecutor(
			THREADS, THREADS, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(THREADS * 4), runnable -> {
				Thread thread = new Thread(runnable, "Exchange Worker " + count.incrementAndGet());
				thread.setDaemon(true);
				return thread;
			}, new ThreadPoolExecutor.CallerRunsPolicy()
		);
	}

	void submit(String area, Task task) {
		this.executor.execute(() -> {
			try {
				task.run();
			} catch (Exception e) {
				if (this.failures.incrementAndGet() <= MAX_LOGGED_FAILURES) {
					Exchange.LOGGER.warn("Failed to copy the LODs at {}", area, e);
				}
			}
		});
	}

	@Override
	public void close() throws InterruptedException {
		this.executor.shutdown();
		this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		if (this.failures.get() > 0) {
			Exchange.LOGGER.warn("Skipped {} areas that failed to copy", this.failures.get());
		}
	}
}
