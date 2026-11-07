package dev.creeperkatze.illuminate.mixin;

import dev.creeperkatze.illuminate.game.GameEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@Inject(method = "createFireworks", at = @At("HEAD"))
	private void illuminate$onFireworks(double x, double y, double z, double xd, double yd, double zd, List<FireworkExplosion> explosions, boolean playSound, CallbackInfo ci) {
		GameEvents.onFirework(new Vec3(x, y, z), explosions);
	}
}
