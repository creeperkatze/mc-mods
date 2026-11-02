package dev.creeperkatze.gather;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// How a world is hosted, kept in a file inside the world folder
public record HostSettings(
		boolean onlineMode,
		int maxPlayers,
		String serverName,
		boolean whitelist,
		int idleTimeout,
		boolean openOnLoad,
		int port,
		boolean guestCommands,
		boolean forceGameMode
) {
	public static final int MIN_PLAYERS = 2;
	public static final int MAX_PLAYERS = 50;
	public static final int MAX_IDLE_TIMEOUT = 60;
	public static final HostSettings DEFAULT = new HostSettings(true, 8, "", false, 0, false, 0, false, true);

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = Gather.MOD_ID + ".json";

	public static HostSettings load(Path worldDir) {
		Path path = worldDir.resolve(FILE_NAME);
		if (!Files.exists(path)) {
			return DEFAULT;
		}
		try {
			HostSettings settings = GSON.fromJson(Files.readString(path), HostSettings.class);
			return settings == null ? DEFAULT : settings.validated();
		} catch (IOException | JsonParseException e) {
			Gather.LOGGER.warn("Couldn't load the host settings of {}", worldDir, e);
			return DEFAULT;
		}
	}

	public void save(Path worldDir) {
		try {
			Files.writeString(worldDir.resolve(FILE_NAME), GSON.toJson(this));
		} catch (IOException e) {
			Gather.LOGGER.warn("Couldn't save the host settings of {}", worldDir, e);
		}
	}

	private HostSettings validated() {
		return new HostSettings(
				this.onlineMode,
				Math.clamp(this.maxPlayers, MIN_PLAYERS, MAX_PLAYERS),
				this.serverName == null ? "" : this.serverName,
				this.whitelist,
				Math.clamp(this.idleTimeout, 0, MAX_IDLE_TIMEOUT),
				this.openOnLoad,
				this.port,
				this.guestCommands,
				this.forceGameMode
		);
	}

	public HostSettings withOptions(boolean onlineMode, int maxPlayers, String serverName, boolean whitelist, int idleTimeout, boolean openOnLoad) {
		return new HostSettings(onlineMode, maxPlayers, serverName, whitelist, idleTimeout, openOnLoad, this.port, this.guestCommands, this.forceGameMode);
	}

	public HostSettings withPublished(int port, boolean guestCommands, boolean forceGameMode) {
		return new HostSettings(this.onlineMode, this.maxPlayers, this.serverName, this.whitelist, this.idleTimeout, this.openOnLoad, port, guestCommands, forceGameMode);
	}

	public HostSettings withWhitelist(boolean whitelist) {
		return new HostSettings(this.onlineMode, this.maxPlayers, this.serverName, whitelist, this.idleTimeout, this.openOnLoad, this.port, this.guestCommands, this.forceGameMode);
	}

	public HostSettings withIdleTimeout(int idleTimeout) {
		return new HostSettings(this.onlineMode, this.maxPlayers, this.serverName, this.whitelist, idleTimeout, this.openOnLoad, this.port, this.guestCommands, this.forceGameMode);
	}
}
