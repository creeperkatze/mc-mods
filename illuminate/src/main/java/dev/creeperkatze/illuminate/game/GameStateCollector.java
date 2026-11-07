package dev.creeperkatze.illuminate.game;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.mixin.BossHealthOverlayAccessor;
import dev.creeperkatze.illuminate.scene.Rgb;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Reads the game on every client tick and publishes an immutable snapshot for the lighting thread
public final class GameStateCollector {
	private static final double LIGHTNING_RANGE = 192.0;
	private static final long WORLD_GAP_MS = 3000L;
	// These change how you see instead of tinting the room
	private static final Set<Holder<MobEffect>> VISION_EFFECTS = Set.of(MobEffects.NIGHT_VISION, MobEffects.DARKNESS, MobEffects.BLINDNESS, MobEffects.NAUSEA);

	private static volatile GameState current = GameState.MENU;

	private static @Nullable ResourceKey<Level> lastDimension;
	private static int lastPlayerId = -1;
	private static int lastLevel = -1;
	private static long lastInWorldAt;
	private static final Set<Integer> seenBolts = new HashSet<>();

	private GameStateCollector() {
	}

	public static GameState current() {
		return current;
	}

	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		ClientLevel level = client.level;
		Camera camera = client.gameRenderer.mainCamera();
		if (player == null || level == null || !camera.isInitialized()) {
			// Changing dimension leaves the game without a world for a moment, so the last scene stays to avoid a flash
			if (current.inWorld() && System.currentTimeMillis() - lastInWorldAt < WORLD_GAP_MS) {
				return;
			}
			lastDimension = null;
			lastPlayerId = -1;
			lastLevel = -1;
			seenBolts.clear();
			current = GameState.MENU;
			return;
		}

		lastInWorldAt = System.currentTimeMillis();
		postTransitions(player, level);
		scanLightning(level, camera);
		current = collect(client, player, level, camera);
	}

	private static void postTransitions(LocalPlayer player, ClientLevel level) {
		if (level.dimension() != lastDimension || player.getId() != lastPlayerId) {
			GameEvents.post(new GameEvent.Arrival());
			lastDimension = level.dimension();
			lastPlayerId = player.getId();
			lastLevel = player.experienceLevel;
		}
		if (player.experienceLevel > lastLevel && lastLevel >= 0) {
			GameEvents.post(new GameEvent.LevelUp(player.experienceLevel));
		}
		lastLevel = player.experienceLevel;
	}

	private static void scanLightning(ClientLevel level, Camera camera) {
		Set<Integer> present = new HashSet<>();
		for (Entity entity : level.entitiesForRendering()) {
			if (!(entity instanceof LightningBolt bolt)) {
				continue;
			}
			present.add(bolt.getId());
			if (seenBolts.add(bolt.getId())) {
				Vec3 offset = bolt.position().subtract(camera.position());
				float strength = (float) Math.clamp(1.0 - offset.horizontalDistance() / LIGHTNING_RANGE, 0.15, 1.0);
				if (Minecraft.getInstance().options.hideLightningFlash().get()) {
					strength *= 0.25F;
				}
				GameEvents.post(new GameEvent.Lightning(strength, GameEvents.horizontalPosition(camera, offset.x, offset.z)));
			}
		}
		seenBolts.retainAll(present);
	}

	private static GameState collect(Minecraft client, LocalPlayer player, ClientLevel level, Camera camera) {
		EnvironmentAttributeProbe probe = camera.attributeProbe();
		BlockPos eyes = BlockPos.containing(camera.position());

		float sunAngle = (float) Math.toRadians(probe.getValue(EnvironmentAttributes.SUN_ANGLE, 1.0F));
		float sunX = Math.sin(sunAngle) > 0.0 ? -1.0F : 1.0F;
		float facingSun = Math.max(0.0F, camera.forwardVector().x() * sunX);
		Vector4fc sunrise = probe.getValue(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, 1.0F);
		float sunU = GameEvents.horizontalPosition(camera, sunX, 0.0);

		// The moon moves on a circle from east to west through straight overhead
		float moonAngle = (float) Math.toRadians(probe.getValue(EnvironmentAttributes.MOON_ANGLE, 1.0F));
		float moonHeight = (float) Math.cos(moonAngle);
		float moonX = (float) -Math.sin(moonAngle);
		Vector3fc forward = camera.forwardVector();
		Vector3fc left = camera.leftVector();
		float moonU = 0.5F - 0.5F * (moonX * left.x() + moonHeight * left.y());
		float facingMoon = Math.max(0.0F, moonX * forward.x() + moonHeight * forward.y());
		float night = Math.clamp(probe.getValue(EnvironmentAttributes.STAR_BRIGHTNESS, 1.0F) * 2.0F, 0.0F, 1.0F);
		float moonVisible = Math.clamp((moonHeight + 0.05F) / 0.25F, 0.0F, 1.0F);
		// A full moon gives the most light and a new moon none
		MoonPhase phase = probe.getValue(EnvironmentAttributes.MOON_PHASE, 1.0F);
		float phaseLight = 1.0F - Math.min(phase.index(), MoonPhase.COUNT - phase.index()) / (MoonPhase.COUNT / 2.0F);
		float moonlight = night * moonVisible * (1.0F - level.getRainLevel(1.0F)) * phaseLight;
		// The moon only lights the room while you look at it
		float moonStrength = moonlight * (float) Math.pow(facingMoon, 4.0);

		Holder<Biome> biome = level.getBiome(eyes);
		Biome.Precipitation precipitation = level.precipitationAt(eyes);
		float maxHealth = Math.max(player.getMaxHealth(), 1.0F);
		float blockLight = level.getBrightness(LightLayer.BLOCK, eyes) / 15.0F;
		Rgb horizon = Rgb.of(probe.getValue(EnvironmentAttributes.FOG_COLOR, 1.0F));
		float daylight = probe.getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, 1.0F);
		Rgb blockTint = Rgb.of(probe.getValue(EnvironmentAttributes.BLOCK_LIGHT_TINT, 1.0F));

		return new GameState(
				true,
				menuOpen(client.gui.screen()),
				dimension(level),
				level.dimensionType().hasSkyLight(),
				horizon,
				BiomeFeelResolver.resolve(level, eyes, biome),
				new Rgb(sunrise.x(), sunrise.y(), sunrise.z()),
				sunrise.w() * (0.35F + 0.65F * facingSun),
				Float.isNaN(sunU) ? 0.5F : sunU,
				moonStrength,
				moonU,
				0.45F - 0.35F * Math.max(moonHeight, 0.0F),
				moonlight,
				Rgb.of(probe.getValue(EnvironmentAttributes.WATER_FOG_COLOR, 1.0F)),
				level.getBrightness(LightLayer.SKY, eyes) / 15.0F,
				daylight,
				blockLight,
				blockTint,
				level.getRainLevel(1.0F),
				level.getThunderLevel(1.0F),
				precipitation,
				biome.is(Biomes.DEEP_DARK),
				camera.getFluidInCamera(),
				Math.clamp(player.getHealth() / maxHealth, 0.0F, 1.0F),
				Math.clamp(player.getAirSupply() / (float) Math.max(player.getMaxAirSupply(), 1), 0.0F, 1.0F),
				player.getPercentFrozen(),
				player.displayFireAnimation(),
				player.isSleeping() ? Math.min(player.getSleepTimer() / 100.0F, 1.0F) : 0.0F,
				player.isDeadOrDying() || client.gui.screen() instanceof DeathScreen,
				effects(player),
				player.hasEffect(MobEffects.NIGHT_VISION) ? GameRenderer.nightVisionScale(player, 1.0F) : 0.0F,
				blend(player, MobEffects.DARKNESS),
				blend(player, MobEffects.BLINDNESS),
				blend(player, MobEffects.NAUSEA),
				bossColor(client),
				player.portalEffectIntensity
		);
	}

	// Loading screens and the live preview shouldn't dim the lights
	private static boolean menuOpen(@Nullable Screen screen) {
		return screen != null && !Illuminate.engine().previewing() && !(screen instanceof LevelLoadingScreen);
	}

	private static GameState.Dimension dimension(ClientLevel level) {
		ResourceKey<Level> key = level.dimension();
		if (key == Level.OVERWORLD) {
			return GameState.Dimension.OVERWORLD;
		}
		if (key == Level.NETHER) {
			return GameState.Dimension.NETHER;
		}
		return key == Level.END ? GameState.Dimension.END : GameState.Dimension.OTHER;
	}

	private static List<GameState.ActiveEffect> effects(LocalPlayer player) {
		List<GameState.ActiveEffect> effects = new ArrayList<>();
		for (MobEffectInstance instance : player.getActiveEffects()) {
			Holder<MobEffect> effect = instance.getEffect();
			if (VISION_EFFECTS.contains(effect)) {
				continue;
			}
			MobEffect value = effect.value();
			effects.add(new GameState.ActiveEffect(Rgb.of(value.getColor()), value.getCategory() == MobEffectCategory.HARMFUL, instance.getAmplifier()));
		}
		return List.copyOf(effects);
	}

	private static float blend(LocalPlayer player, Holder<MobEffect> effect) {
		MobEffectInstance instance = player.getEffect(effect);
		return instance == null ? 0.0F : instance.getBlendFactor(player, 1.0F);
	}

	private static @Nullable Rgb bossColor(Minecraft client) {
		for (LerpingBossEvent event : ((BossHealthOverlayAccessor) client.gui.hud.getBossOverlay()).illuminate$events().values()) {
			return switch (event.getColor()) {
				case PINK -> Rgb.of(0xEC00B8);
				case BLUE -> Rgb.of(0x00B7EC);
				case RED -> Rgb.of(0xEC3500);
				case GREEN -> Rgb.of(0x1DEC00);
				case YELLOW -> Rgb.of(0xE9EC00);
				case PURPLE -> Rgb.of(0x7B00EC);
				case WHITE -> Rgb.of(0xECECEC);
			};
		}
		return null;
	}
}
