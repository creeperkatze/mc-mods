package dev.creeperkatze.resume.mixin;

import dev.creeperkatze.resume.Place;
import dev.creeperkatze.resume.Resume;
import dev.creeperkatze.resume.Screenshots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
//? if >=26.2 {
import net.minecraft.world.level.gamerules.GameRules;
//?}
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
import java.util.Optional;
//?}

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "doWorldLoad", at = @At("HEAD"))
	//? if >=26.2 {
	private void resume$joinWorld(LevelStorageSource.LevelStorageAccess levelSourceAccess, PackRepository packRepository,
			WorldStem worldStem, Optional<GameRules> gameRules, boolean newWorld, CallbackInfo ci) {
	//?} else {
	/*private void resume$joinWorld(LevelStorageSource.LevelStorageAccess levelSourceAccess, PackRepository packRepository,
			WorldStem worldStem, boolean newWorld, CallbackInfo ci) {
	*///?}
		Resume.onJoin(Place.world(levelSourceAccess.getLevelId()));
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"))
	private void resume$captureScreenshot(Screen screen, boolean keepResourcePacks, boolean stopSound, CallbackInfo ci) {
		Screenshots.onDisconnect((Minecraft) (Object) this);
	}
}
