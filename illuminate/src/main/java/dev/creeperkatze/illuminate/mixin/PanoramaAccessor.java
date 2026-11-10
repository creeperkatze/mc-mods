package dev.creeperkatze.illuminate.mixin;

//? if >=26.2 {
import net.minecraft.client.renderer.Panorama;
//?} else {
/*import net.minecraft.client.renderer.PanoramaRenderer;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=26.2 {
@Mixin(Panorama.class)
//?} else {
/*@Mixin(PanoramaRenderer.class)
*///?}
public interface PanoramaAccessor {
	@Accessor("spin")
	float illuminate$spin();
}
