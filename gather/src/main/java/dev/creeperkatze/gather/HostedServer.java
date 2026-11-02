package dev.creeperkatze.gather;

// Implemented by the integrated server through IntegratedServerMixin
public interface HostedServer {
	HostSettings gather$settings();

	void gather$setSettings(HostSettings settings);
}
