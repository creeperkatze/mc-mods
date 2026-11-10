package dev.creeperkatze.resume;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.nio.file.Path;
import java.util.Locale;

public record Place(Path screenshot, boolean singleplayer) {
	public static Place world(String levelId) {
		return new Place(directory().resolve("worlds").resolve(levelId + ".png"), true);
	}

	public static Place server(ServerData server) {
		String name = server.ip.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
		return new Place(directory().resolve("servers").resolve(name + ".png"), false);
	}

	public static Path directory() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve(Resume.MOD_ID);
	}

	public boolean enabled() {
		return singleplayer ? Resume.config().worlds : Resume.config().servers;
	}
}
