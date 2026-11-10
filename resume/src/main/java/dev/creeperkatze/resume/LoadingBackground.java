package dev.creeperkatze.resume;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LoadingBackground {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Resume.MOD_ID, "loading_background");

	private static @Nullable Path shown;
	private static int width;
	private static int height;
	private static boolean joining;
	private static long shownAt;
	private static long fadeStart = -1;

	private LoadingBackground() {
	}

	public static void onJoin(Place place) {
		Path file = place.screenshot();
		if (file.equals(shown)) {
			joining = true;
			shownAt = Util.getMillis();
			fadeStart = -1;
			return;
		}
		release();
		if (!place.enabled() || !Files.isRegularFile(file)) {
			return;
		}
		try (InputStream input = Files.newInputStream(file)) {
			NativeImage image = NativeImage.read(input);
			width = image.getWidth();
			height = image.getHeight();
			Minecraft.getInstance().getTextureManager().register(TEXTURE,
					new DynamicTexture(() -> "Resume loading background", image));
			shown = file;
			joining = true;
			shownAt = Util.getMillis();
		} catch (IOException e) {
			Resume.LOGGER.warn("Could not load the screenshot {}", file, e);
		}
	}

	public static void onScreenChange(@Nullable Screen from, @Nullable Screen to) {
		if (joining && from instanceof LevelLoadingScreen && !(to instanceof LevelLoadingScreen)) {
			if (Resume.config().fadeOutDuration > 0.0F) {
				joining = false;
				fadeStart = Util.getMillis();
			} else {
				release();
			}
		} else if (to instanceof TitleScreen || to instanceof JoinMultiplayerScreen || to instanceof SelectWorldScreen
				|| to instanceof DisconnectedScreen) {
			// Joining was canceled or failed
			release();
		}
	}

	public static boolean extract(Screen screen, GuiGraphicsExtractor graphics) {
		boolean loading = screen instanceof LevelLoadingScreen || screen instanceof ConnectScreen
				|| screen instanceof ProgressScreen || screen instanceof GenericMessageScreen;
		if (!joining || !loading) {
			return false;
		}
		draw(graphics, screen.width, screen.height, 1.0F);
		return true;
	}

	public static void extractFade(GuiGraphicsExtractor graphics) {
		if (fadeStart < 0) {
			return;
		}
		float progress = (Util.getMillis() - fadeStart) / (Resume.config().fadeOutDuration * 1000.0F);
		if (!(progress < 1.0F)) {
			release();
			return;
		}
		draw(graphics, graphics.guiWidth(), graphics.guiHeight(), 1.0F - progress);
	}

	// Crops the screenshot to cover the screen, since the window may have a different size now
	private static void draw(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, float alpha) {
		float scale = Math.max((float) screenWidth / width, (float) screenHeight / height);
		int sourceWidth = Math.min(width, Math.round(screenWidth / scale));
		int sourceHeight = Math.min(height, Math.round(screenHeight / scale));
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, (width - sourceWidth) / 2.0F,
				(height - sourceHeight) / 2.0F, screenWidth, screenHeight, sourceWidth, sourceHeight, width, height,
				ARGB.white(alpha));
		float brightness = Resume.config().brightness * fadeIn();
		graphics.fill(0, 0, screenWidth, screenHeight, ARGB.black(alpha * (1.0F - brightness)));
	}

	// Rises from 0 to 1 over the fade in duration after the screenshot first shows
	private static float fadeIn() {
		float duration = Resume.config().fadeInDuration * 1000.0F;
		if (duration <= 0.0F) {
			return 1.0F;
		}
		return Math.min(1.0F, (Util.getMillis() - shownAt) / duration);
	}

	private static void release() {
		if (shown != null) {
			Minecraft.getInstance().getTextureManager().release(TEXTURE);
			shown = null;
		}
		joining = false;
		fadeStart = -1;
	}
}
