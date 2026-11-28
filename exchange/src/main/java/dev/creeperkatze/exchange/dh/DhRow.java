package dev.creeperkatze.exchange.dh;

import org.jspecify.annotations.Nullable;

// A full detail FullData row with its blobs still compressed
public record DhRow(
	int x,
	int z,
	byte format,
	byte compression,
	byte[] data,
	byte @Nullable [] north,
	byte @Nullable [] south,
	byte @Nullable [] east,
	byte @Nullable [] west,
	byte[] generationSteps,
	byte[] worldCompression,
	byte[] mapping,
	boolean applyToChildren,
	boolean regenerate
) {
}
