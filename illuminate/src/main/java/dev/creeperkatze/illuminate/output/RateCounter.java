package dev.creeperkatze.illuminate.output;

// Counts how often something happens per second, for the debug info
public final class RateCounter {
	private int count;
	private long windowStart;
	private volatile float perSecond;

	public void count() {
		count++;
	}

	public void update(long now) {
		if (windowStart == 0L) {
			windowStart = now;
		} else if (now - windowStart >= 1000L) {
			perSecond = count * 1000.0F / (now - windowStart);
			count = 0;
			windowStart = now;
		}
	}

	public float perSecond() {
		return perSecond;
	}
}
