package dev.creeperkatze.organize;

import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

// Loads the icon of a pack folder, and loads it again once the file changes
public final class PackFolderIcons {
	private static final String ICON_FILE = "icon.png";
	// Recursive Resources and Pack Manager can name the icon in folder.json
	private static final String META_FILE = "folder.json";
	private static final Map<Path, Icon> ICONS = new HashMap<>();

	private PackFolderIcons() {
	}

	public static @Nullable Identifier get(Path folder) {
		Path file = iconFile(folder);
		long modified;
		try {
			modified = Files.getLastModifiedTime(file).toMillis();
		} catch (NoSuchFileException e) {
			return null;
		} catch (IOException e) {
			Organize.LOGGER.warn("Failed to read pack folder icon {}", file, e);
			return null;
		}
		Icon icon = ICONS.get(file);
		if (icon == null || icon.modified() != modified) {
			icon = new Icon(modified, load(file));
			ICONS.put(file, icon);
		}
		return icon.location();
	}

	private static Path iconFile(Path folder) {
		Path meta = folder.resolve(META_FILE);
		if (Files.isRegularFile(meta)) {
			try (Reader reader = Files.newBufferedReader(meta)) {
				if (JsonParser.parseReader(reader) instanceof JsonObject json
						&& json.get("icon") instanceof JsonElement icon && icon.isJsonPrimitive()
						&& !icon.getAsString().isBlank()) {
					return folder.resolve(icon.getAsString());
				}
			} catch (IOException | JsonParseException | InvalidPathException e) {
				Organize.LOGGER.warn("Failed to read pack folder meta {}", meta, e);
			}
		}
		return folder.resolve(ICON_FILE);
	}

	private static @Nullable Identifier load(Path file) {
		Identifier location = Identifier.fromNamespaceAndPath(Organize.MOD_ID, "pack_folder/" + Hashing.sha1().hashUnencodedChars(file.toAbsolutePath().toString()));
		try (InputStream stream = Files.newInputStream(file)) {
			NativeImage image = NativeImage.read(stream);
			Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(location::toString, image));
			return location;
		} catch (IOException e) {
			Organize.LOGGER.warn("Failed to load pack folder icon {}", file, e);
			return null;
		}
	}

	private record Icon(long modified, @Nullable Identifier location) {
	}
}
