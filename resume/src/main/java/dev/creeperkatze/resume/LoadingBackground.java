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
import java.util.concurrent.CompletableFuture;

public final class LoadingBackground {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Resume.MOD_ID, "loading_background");

	// Image width that gets a blur radius of one pixel per step, so the blur looks the same at any resolution
	private static final float BLUR_SCALE = 960.0F;

	private static @Nullable Path shown;
	private static @Nullable Path loading;
	private static int shownBlur;
	private static int width;
	private static int height;
	private static boolean joining;
	private static long shownAt;
	private static long fadeStart = -1;

	private LoadingBackground() {
	}

	public static void onJoin(Place place) {
		Path file = place.screenshot();
		int blur = Resume.config().blur;
		if (file.equals(shown) && blur == shownBlur || file.equals(loading)) {
			joining = true;
			shownAt = Util.getMillis();
			fadeStart = -1;
			return;
		}
		release();
		if (!place.enabled() || !Files.isRegularFile(file)) {
			return;
		}
		loading = file;
		joining = true;
		// Reading and blurring a large screenshot takes a moment, so it happens off the main thread
		CompletableFuture.supplyAsync(() -> read(file, blur), Util.backgroundExecutor())
				.thenAcceptAsync(image -> show(file, blur, image), Minecraft.getInstance());
	}

	private static @Nullable NativeImage read(Path file, int blur) {
		try (InputStream input = Files.newInputStream(file)) {
			NativeImage image = NativeImage.read(input);
			if (blur > 0) {
				Blur.apply(image, Math.max(1, Math.round(blur * image.getWidth() / BLUR_SCALE)));
			}
			return image;
		} catch (IOException e) {
			Resume.LOGGER.warn("Could not load the screenshot {}", file, e);
			return null;
		}
	}

	private static void show(Path file, int blur, @Nullable NativeImage image) {
		if (!file.equals(loading)) {
			// Joining was canceled while the screenshot loaded
			if (image != null) {
				image.close();
			}
			return;
		}
		loading = null;
		if (image == null) {
			return;
		}
		width = image.getWidth();
		height = image.getHeight();
		Minecraft.getInstance().getTextureManager().register(TEXTURE,
				new DynamicTexture(() -> "Resume loading background", image));
		shown = file;
		shownBlur = blur;
		shownAt = Util.getMillis();
	}

	public static void onScreenChange(@Nullable Screen from, @Nullable Screen to) {
		if (joining && from instanceof LevelLoadingScreen && !(to instanceof LevelLoadingScreen)) {
			if (shown != null && Resume.config().fadeOutDuration > 0.0F) {
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
		boolean loadingScreen = screen instanceof LevelLoadingScreen || screen instanceof ConnectScreen
				|| screen instanceof ProgressScreen || screen instanceof GenericMessageScreen;
		if (!joining || shown == null || !loadingScreen) {
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
		loading = null;
		joining = false;
		fadeStart = -1;
	}
}
