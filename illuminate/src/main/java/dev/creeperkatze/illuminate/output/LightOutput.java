package dev.creeperkatze.illuminate.output;

public interface LightOutput {
	// One of connecting, connected, unavailable or disabled, used as a translation key suffix
	String status();

	// Hands the lights back to their own software
	void release();

	void close();
}
