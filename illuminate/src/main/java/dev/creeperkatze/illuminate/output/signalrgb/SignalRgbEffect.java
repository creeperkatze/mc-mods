package dev.creeperkatze.illuminate.output.signalrgb;

import dev.creeperkatze.illuminate.Illuminate;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// The Illuminate lighting effect that SignalRGB loads from its user effects folder
public final class SignalRgbEffect {
	public static final String NAME = "Illuminate";
	private static final String EFFECT = NAME + ".html";
	// SignalRGB shows a PNG with the same name as the effect as its thumbnail
	private static final String THUMBNAIL = "Illuminate.png";
	private static final List<String> FILES = List.of(EFFECT, THUMBNAIL);
	private static final String RESOURCES = "/assets/" + Illuminate.MOD_ID + "/signalrgb/";
	private static final String SELECTED_KEY = "HKCU\\Software\\WhirlwindFX\\SignalRgb\\effects\\selected";
	// SignalRGB only exists for Windows.
	private static final boolean SUPPORTED = Objects.requireNonNullElse(System.getenv("ILLUMINATE_OS"),
			System.getProperty("os.name", "")).toLowerCase(Locale.ROOT).contains("windows");

	private SignalRgbEffect() {
	}

	public static boolean supported() {
		return SUPPORTED;
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
		if (!supported() || !isInstalled()) {
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

	// The name of the effect SignalRGB shows right now
	// Only reads the registry, because the SignalRGB API for this is locked to Pro users
	public static @Nullable String currentEffect() {
		if (!supported()) {
			return null;
		}
		try {
			Process process = new ProcessBuilder("reg", "query", SELECTED_KEY, "/v", "name").redirectErrorStream(true).start();
			String output = new String(process.getInputStream().readAllBytes(), Charset.defaultCharset());
			if (process.waitFor() != 0) {
				return null;
			}
			for (String line : output.split("\\R")) {
				int type = line.indexOf("REG_SZ");
				if (line.strip().startsWith("name") && type >= 0) {
					String name = line.substring(type + "REG_SZ".length()).strip();
					return name.isEmpty() ? null : name;
				}
			}
		} catch (IOException e) {
			Illuminate.LOGGER.warn("Could not read the current SignalRGB effect: {}", e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return null;
	}

	// Asks SignalRGB to switch to an effect without bringing its window up
	public static void apply(String effect) {
		if (!supported()) {
			return;
		}
		String uri = "signalrgb://effect/apply/" + URLEncoder.encode(effect, StandardCharsets.UTF_8).replace("+", "%20") + "?-silentlaunch-";
		try {
			new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", uri).start();
		} catch (IOException e) {
			Illuminate.LOGGER.warn("Could not switch SignalRGB to the {} effect: {}", effect, e.getMessage());
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
