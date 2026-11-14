package dev.creeperkatze.illuminate.mixin;

//? if <1.21.11 {
/*import net.minecraft.client.renderer.PanoramaRenderer;
import org.spongepowered.asm.mixin.gen.Accessor;
*///?}
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;

// Older versions keep the title screen panorama on Screen instead of the game renderer
@Mixin(Screen.class)
public interface ScreenPanoramaAccessor {
	//? if <1.21.11 {
	/*@Accessor("PANORAMA")
	static PanoramaRenderer illuminate$panorama() {
		throw new AssertionError();
	}
	*///?}
}
