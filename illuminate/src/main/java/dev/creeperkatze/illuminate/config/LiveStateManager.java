package dev.creeperkatze.illuminate.config;

import dev.isxander.yacl3.api.StateManager;

import java.util.function.Supplier;

// A read only value that YACL asks for again on every frame, so labels using it stay current
final class LiveStateManager<T> implements StateManager<T> {
	private final Supplier<T> value;

	LiveStateManager(Supplier<T> value) {
		this.value = value;
	}

	@Override
	public void set(T value) {
		throw new UnsupportedOperationException("Live values cannot be set");
	}

	@Override
	public T get() {
		return value.get();
	}

	@Override
	public void apply() {
	}

	@Override
	public void resetToDefault(ResetAction action) {
	}

	@Override
	public void sync() {
	}

	@Override
	public boolean isSynced() {
		return true;
	}

	@Override
	public boolean isAlwaysSynced() {
		return true;
	}

	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public void addListener(StateListener<T> listener) {
	}
}
