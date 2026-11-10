package dev.creeperkatze.resume;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Screenshots {
	private static @Nullable Place current;
	private static @Nullable Path pending;

	private Screenshots() {
	}

	public static void onJoin(Place place) {
		current = place;
	}

	public static void onDisconnect(Minecraft minecraft) {
		Place place = current;
		if (place == null || minecraft.level == null) {
			return;
		}
		current = null;
		if (!place.enabled()) {
			return;
		}
		// Renders one more frame so the world is captured before the pause menu is drawn over it
		pending = place.screenshot();
		//? if >=26.2 {
		minecraft.renderFrame(true);
		//?} else {
		/*minecraft.gameRenderer.render(minecraft.getDeltaTracker(), true);
		*///?}
		pending = null;
	}

	public static void onWorldRendered(RenderTarget target) {
		Path file = pending;
		if (file == null) {
			return;
		}
		pending = null;
		Screenshot.takeScreenshot(target, image -> Util.ioPool().execute(() -> {
			try (image) {
				Files.createDirectories(file.getParent());
				image.writeToFile(file);
			} catch (IOException e) {
				Resume.LOGGER.warn("Could not save the screenshot {}", file, e);
			}
		}));
	}
}
