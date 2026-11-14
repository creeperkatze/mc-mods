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
//? if >=1.21.11 && <26.3 {
/*import net.minecraft.util.ARGB;
*///?}
import net.minecraft.resources.ResourceKey;
//? if >=1.21.11 {
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.MoonPhase;
//?} else {
/*import net.minecraft.world.level.levelgen.Heightmap;
*///?}
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
//? if >=1.21.11 {
import org.joml.Vector4fc;
//?}
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
			MenuPanorama.tick(client);
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
			GameEvents.post(new GameEvent.LevelUp());
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
		BlockPos eyes = BlockPos.containing(camera.position());
		Holder<Biome> biome = level.getBiome(eyes);
		//? if >=1.21.11 {
		EnvironmentAttributeProbe probe = camera.attributeProbe();
		float sunAngle = (float) Math.toRadians(probe.getValue(EnvironmentAttributes.SUN_ANGLE, 1.0F));
		//? if >=26.3 {
		Vector4fc sunrise = probe.getValue(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, 1.0F);
		//?} else {
		/*Vector4fc sunrise = ARGB.vector4fFromARGB32(probe.getValue(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, 1.0F));
		*///?}
		Rgb sunriseColor = new Rgb(sunrise.x(), sunrise.y(), sunrise.z());
		float sunriseAlpha = sunrise.w();
		float moonAngle = (float) Math.toRadians(probe.getValue(EnvironmentAttributes.MOON_ANGLE, 1.0F));
		float night = Math.clamp(probe.getValue(EnvironmentAttributes.STAR_BRIGHTNESS, 1.0F) * 2.0F, 0.0F, 1.0F);
		MoonPhase phase = probe.getValue(EnvironmentAttributes.MOON_PHASE, 1.0F);
		int phaseIndex = phase.index();
		int phaseCount = MoonPhase.COUNT;
		Biome.Precipitation precipitation = level.precipitationAt(eyes);
		Rgb horizon = Rgb.of(probe.getValue(EnvironmentAttributes.FOG_COLOR, 1.0F));
		float daylight = probe.getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, 1.0F);
		Rgb waterFog = Rgb.of(probe.getValue(EnvironmentAttributes.WATER_FOG_COLOR, 1.0F));
		//?} else {
		/*// Before environment attributes, the same values come from the time of day, the dimension and the biome
		float timeOfDay = level.getTimeOfDay(1.0F);
		float sunAngle = level.getSunAngle(1.0F);
		float[] sunrise = level.effects().getSunriseColor(timeOfDay, 1.0F);
		Rgb sunriseColor = sunrise == null ? Rgb.BLACK : new Rgb(sunrise[0], sunrise[1], sunrise[2]);
		float sunriseAlpha = sunrise == null ? 0.0F : sunrise[3] * (1.0F - level.getRainLevel(1.0F));
		float moonAngle = sunAngle + (float) Math.PI;
		float night = Math.clamp(level.getStarBrightness(1.0F) * 2.0F, 0.0F, 1.0F);
		int phaseIndex = level.getMoonPhase();
		int phaseCount = 8;
		Biome.Precipitation precipitation = precipitationAt(level, eyes, biome);
		Rgb horizon = horizon(level, biome, timeOfDay);
		float daylight = level.getSkyDarken(1.0F);
		Rgb waterFog = Rgb.of(biome.value().getWaterFogColor());
		*///?}
		float sunX = Math.sin(sunAngle) > 0.0 ? -1.0F : 1.0F;
		float facingSun = Math.max(0.0F, camera.forwardVector().x() * sunX);
		float sunU = GameEvents.horizontalPosition(camera, sunX, 0.0);

		// The moon moves on a circle from east to west through straight overhead
		float moonHeight = (float) Math.cos(moonAngle);
		float moonX = (float) -Math.sin(moonAngle);
		Vector3fc forward = camera.forwardVector();
		Vector3fc left = camera.leftVector();
		float moonU = 0.5F - 0.5F * (moonX * left.x() + moonHeight * left.y());
		float facingMoon = Math.max(0.0F, moonX * forward.x() + moonHeight * forward.y());
		float moonVisible = Math.clamp((moonHeight + 0.05F) / 0.25F, 0.0F, 1.0F);
		// A full moon gives the most light and a new moon none
		float phaseLight = 1.0F - Math.min(phaseIndex, phaseCount - phaseIndex) / (phaseCount / 2.0F);
		float moonlight = night * moonVisible * (1.0F - level.getRainLevel(1.0F)) * phaseLight;
		// The moon only lights the room while you look at it
		float moonStrength = moonlight * (float) Math.pow(facingMoon, 4.0);

		float maxHealth = Math.max(player.getMaxHealth(), 1.0F);
		float blockLight = level.getBrightness(LightLayer.BLOCK, eyes) / 15.0F;
		//? if >=26.2 {
		Rgb blockTint = Rgb.of(probe.getValue(EnvironmentAttributes.BLOCK_LIGHT_TINT, 1.0F));
		//?} else {
		/*// Older versions have no tint attribute. This is its default in 26.2.
		Rgb blockTint = Rgb.of(0xFFD88C);
		*///?}
		//? if >=26.2 {
		Screen screen = client.gui.screen();
		//?} else {
		/*Screen screen = client.screen;
		*///?}

		return new GameState(
				true,
				menuOpen(screen),
				dimension(level),
				level.dimensionType().hasSkyLight(),
				horizon,
				BiomeFeelResolver.resolve(level, eyes, biome),
				sunriseColor,
				sunriseAlpha * (0.35F + 0.65F * facingSun),
				Float.isNaN(sunU) ? 0.5F : sunU,
				moonStrength,
				moonU,
				0.45F - 0.35F * Math.max(moonHeight, 0.0F),
				moonlight,
				waterFog,
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
				player.isDeadOrDying() || screen instanceof DeathScreen,
				effects(player),
				player.hasEffect(MobEffects.NIGHT_VISION) ? GameRenderer.nightVisionScale(player, 1.0F) : 0.0F,
				blend(player, MobEffects.DARKNESS),
				blend(player, MobEffects.BLINDNESS),
				blend(player, MobEffects.NAUSEA),
				bossColor(client),
				//? if >=1.21.2 {
				player.portalEffectIntensity
				//?} else {
				/*player.spinningEffectIntensity
				*///?}
		);
	}

	//? if <1.21.11 {
	/*// Matches Level.precipitationAt from later versions
	private static Biome.Precipitation precipitationAt(ClientLevel level, BlockPos pos, Holder<Biome> biome) {
		if (!level.isRaining() || !level.canSeeSky(pos) || level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY()) {
			return Biome.Precipitation.NONE;
		}
		return biome.value().getPrecipitationAt(pos);
	}

	// The biome's fog colour, darkened at night and in bad weather like the sky behind it
	private static Rgb horizon(ClientLevel level, Holder<Biome> biome, float timeOfDay) {
		float brightness = Math.clamp((float) Math.cos(timeOfDay * Math.PI * 2.0) * 2.0F + 0.5F, 0.0F, 1.0F);
		Vec3 fog = level.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biome.value().getFogColor()), brightness);
		float weather = (1.0F - level.getRainLevel(1.0F) * 0.5F) * (1.0F - level.getThunderLevel(1.0F) * 0.5F);
		return new Rgb((float) fog.x * weather, (float) fog.y * weather, (float) fog.z * weather);
	}
	*///?}

	// Loading screens, the death screen and the live preview shouldn't dim the lights
	private static boolean menuOpen(@Nullable Screen screen) {
		return screen != null && !Illuminate.engine().previewing() && !(screen instanceof LevelLoadingScreen)
				&& !(screen instanceof DeathScreen);
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
