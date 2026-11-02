package dev.creeperkatze.organize;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

// Loads the icon.png of a pack folder, and loads it again once the file changes
public final class PackFolderIcons {
	private static final String ICON_FILE = "icon.png";
	private static final Map<Path, Icon> ICONS = new HashMap<>();

	private PackFolderIcons() {
	}

	public static @Nullable Identifier get(Path folder) {
		Path file = folder.resolve(ICON_FILE);
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
