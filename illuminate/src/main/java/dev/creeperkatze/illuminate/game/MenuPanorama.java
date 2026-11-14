package dev.creeperkatze.illuminate.game;

import com.mojang.blaze3d.platform.NativeImage;
import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.mixin.PanoramaAccessor;
//? if <1.21.11 {
/*import dev.creeperkatze.illuminate.mixin.ScreenPanoramaAccessor;
*///?}
import dev.creeperkatze.illuminate.scene.Canvas;
import dev.creeperkatze.illuminate.scene.Rgb;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class MenuPanorama {
	private static final int COLUMNS = 16;
	private static final int ROWS = Canvas.HEIGHT;
	// panorama_0 to panorama_3 are the side faces, from the front turning right
	private static final int SIDES = 4;
	private static final float ROOM_ANGLE = 180.0F;

	private static volatile Rgb @Nullable [] strip;
	private static volatile float spin;
	private static @Nullable List<String> loadedPacks;

	private MenuPanorama() {
	}

	public static void tick(Minecraft client) {
		//? if >=26.2 {
		spin = ((PanoramaAccessor) client.gameRenderer.panorama()).illuminate$spin();
		//?} else if >=1.21.11 {
		/*spin = ((PanoramaAccessor) client.gameRenderer.getPanorama()).illuminate$spin();
		*///?} else {
		/*spin = ((PanoramaAccessor) ScreenPanoramaAccessor.illuminate$panorama()).illuminate$spin();
		*///?}
		//? if >=26.2 {
		boolean loading = client.gui.overlay() != null;
		//?} else {
		/*boolean loading = client.getOverlay() != null;
		*///?}
		List<String> packs = List.copyOf(client.options.resourcePacks);
		if (!loading && !packs.equals(loadedPacks)) {
			loadedPacks = packs;
			strip = load(client.getResourceManager());
		}
	}

	public static @Nullable Rgb sample(float u, float v) {
		Rgb[] colors = strip;
		if (colors == null) {
			return null;
		}
		// The panorama turns right as it spins, 0 degrees looks at the front face
		float angle = spin + (u - 0.5F) * ROOM_ANGLE + 45.0F;
		float wrapped = ((angle % 360.0F) + 360.0F) % 360.0F;
		int face = (int) (wrapped / 90.0F) % SIDES;
		float local = (float) Math.toRadians(wrapped - face * 90.0F - 45.0F);
		float x = 0.5F + 0.5F * (float) Math.tan(local);
		int width = COLUMNS * SIDES;
		float column = (face + x) * COLUMNS - 0.5F;
		float row = Math.clamp(v * ROWS - 0.5F, 0.0F, ROWS - 1.0F);
		int c0 = Math.floorMod((int) Math.floor(column), width);
		int c1 = (c0 + 1) % width;
		int r0 = (int) row;
		int r1 = Math.min(r0 + 1, ROWS - 1);
		float fx = column - (float) Math.floor(column);
		float fy = row - r0;
		Rgb top = colors[r0 * width + c0].lerp(colors[r0 * width + c1], fx);
		Rgb bottom = colors[r1 * width + c0].lerp(colors[r1 * width + c1], fx);
		return top.lerp(bottom, fy);
	}

	private static Rgb @Nullable [] load(ResourceManager resources) {
		int width = COLUMNS * SIDES;
		Rgb[] colors = new Rgb[width * ROWS];
		for (int side = 0; side < SIDES; side++) {
			Identifier location = Identifier.withDefaultNamespace("textures/gui/title/background/panorama_" + side + ".png");
			try (InputStream stream = resources.open(location); NativeImage image = NativeImage.read(stream)) {
				for (int row = 0; row < ROWS; row++) {
					for (int column = 0; column < COLUMNS; column++) {
						colors[row * width + side * COLUMNS + column] = average(image, column, row);
					}
				}
			} catch (IOException e) {
				Illuminate.LOGGER.warn("Could not read the title screen panorama: {}", e.toString());
				return null;
			}
		}
		return colors;
	}

	private static Rgb average(NativeImage image, int column, int row) {
		int x0 = column * image.getWidth() / COLUMNS;
		int x1 = Math.max((column + 1) * image.getWidth() / COLUMNS, x0 + 1);
		int y0 = row * image.getHeight() / ROWS;
		int y1 = Math.max((row + 1) * image.getHeight() / ROWS, y0 + 1);
		int step = Math.max(1, Math.min(x1 - x0, y1 - y0) / 8);
		float red = 0.0F;
		float green = 0.0F;
		float blue = 0.0F;
		int count = 0;
		for (int y = y0; y < y1; y += step) {
			for (int x = x0; x < x1; x += step) {
				//? if >=1.21.2 {
				Rgb pixel = Rgb.of(image.getPixel(x, y) & 0xFFFFFF);
				//?} else {
				/*// Older versions store pixels as ABGR
				int abgr = image.getPixelRGBA(x, y);
				Rgb pixel = Rgb.of((abgr & 0xFF) << 16 | (abgr & 0xFF00) | (abgr >> 16 & 0xFF));
				*///?}
				red += pixel.r();
				green += pixel.g();
				blue += pixel.b();
				count++;
			}
		}
		return new Rgb(red / count, green / count, blue / count);
	}
}
