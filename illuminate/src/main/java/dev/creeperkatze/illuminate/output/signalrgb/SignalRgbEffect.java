package dev.creeperkatze.illuminate.output.signalrgb;

import dev.creeperkatze.illuminate.Illuminate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// The Illuminate lighting effect that SignalRGB loads from its user effects folder
public final class SignalRgbEffect {
	private static final String EFFECT = "Illuminate.html";
	// SignalRGB shows a PNG with the same name as the effect as its thumbnail
	private static final String THUMBNAIL = "Illuminate.png";
	private static final List<String> FILES = List.of(EFFECT, THUMBNAIL);
	private static final String RESOURCES = "/assets/" + Illuminate.MOD_ID + "/signalrgb/";
	private static final String APPLY_URI = "signalrgb://effect/apply/Illuminate?-silentlaunch-";

	private SignalRgbEffect() {
	}

	public static Path effectsDirectory() {
		Path home = Path.of(System.getProperty("user.home"));
		List<Path> documents = List.of(home.resolve("Documents"), home.resolve("OneDrive").resolve("Documents"));
		for (Path candidate : documents) {
			if (Files.isDirectory(candidate.resolve("WhirlwindFX"))) {
				return candidate.resolve("WhirlwindFX").resolve("Effects");
			}
		}
		return documents.getFirst().resolve("WhirlwindFX").resolve("Effects");
	}

	public static boolean isInstalled() {
		return Files.isRegularFile(effectsDirectory().resolve(EFFECT));
	}

	public static void install() throws IOException {
		Path directory = effectsDirectory();
		Files.createDirectories(directory);
		for (String file : FILES) {
			Files.write(directory.resolve(file), bundled(file));
		}
		Illuminate.LOGGER.info("Installed the SignalRGB effect to {}", directory);
	}

	// Keeps an installed effect in step with the mod version
	public static void updateIfInstalled() {
		if (!isInstalled()) {
			return;
		}
		try {
			Path directory = effectsDirectory();
			for (String file : FILES) {
				Path target = directory.resolve(file);
				byte[] bundled = bundled(file);
				if (!Files.isRegularFile(target) || !Arrays.equals(Files.readAllBytes(target), bundled)) {
					Files.write(target, bundled);
					Illuminate.LOGGER.info("Updated {} at {}", file, directory);
				}
			}
		} catch (IOException e) {
			Illuminate.LOGGER.warn("Could not update the SignalRGB effect: {}", e.toString());
		}
	}

	// Asks SignalRGB to switch to the Illuminate effect without bringing its window up
	public static void apply() {
		if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows")) {
			return;
		}
		try {
			new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", APPLY_URI).start();
		} catch (IOException e) {
			Illuminate.LOGGER.warn("Could not switch SignalRGB to the Illuminate effect: {}", e.getMessage());
		}
	}

	private static byte[] bundled(String file) throws IOException {
		try (InputStream stream = SignalRgbEffect.class.getResourceAsStream(RESOURCES + file)) {
			if (stream == null) {
				throw new IOException("Missing " + RESOURCES + file);
			}
			return stream.readAllBytes();
		}
	}
}
