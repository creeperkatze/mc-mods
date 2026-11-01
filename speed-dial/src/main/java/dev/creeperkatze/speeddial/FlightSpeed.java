package dev.creeperkatze.speeddial;

import dev.creeperkatze.speeddial.config.SpeedDialConfig;
import dev.creeperkatze.speeddial.config.SpeedDisplay;
import dev.creeperkatze.speeddial.config.StepMode;
import dev.creeperkatze.speeddial.mixin.HudAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public final class FlightSpeed {
	public static final float DEFAULT_SPEED = 0.05F;
	public static final float VANILLA_STEP = 0.005F;
	public static final float VANILLA_MAX = 0.2F;
	/**
	 * Horizontal flight settles at 0.98 * speed / 0.09 blocks per tick. Vertical flight settles at 3 * speed / 0.4.
	 * Scaling the vertical boost by this makes both match.
	 */
	public static final float VERTICAL_FACTOR = (0.98F * 0.4F) / (0.09F * 3.0F);
	/** Horizontal blocks per second at a flying speed of 1 without sprinting. */
	private static final float BLOCKS_PER_SECOND = 20.0F * 0.98F / 0.09F;
	/** A speed change first accelerates by 0.98 * change blocks per tick squared. This turns a change into g. */
	private static final float G_FORCE = 0.98F * 20.0F * 20.0F / 9.81F;
	private static final float SPEED_OF_SOUND = 343.0F;
	/** Vanilla shows an action bar message for this many ticks. */
	private static final int MESSAGE_TICKS = 60;

	private static boolean restorePending;
	private static boolean rememberedSpeedChanged;
	private static boolean creativeBoost;
	private static boolean creativeMessageShown;
	private static boolean wasSpectator;
	private static @Nullable Component lastMessage;

	private FlightSpeed() {
	}

	/** The speed message currently or last shown in the action bar. */
	public static @Nullable Component lastMessage() {
		return lastMessage;
	}

	/** Only worlds you host are affected. Flying on other servers stays vanilla. */
	public static boolean isActive(@Nullable LocalPlayer player) {
		return player != null && (player.isSpectator() || player.isCreative()) && Minecraft.getInstance().hasSingleplayerServer();
	}

	/** In creative the speed only changes while flying with the modifier held. */
	public static boolean canChange(@Nullable LocalPlayer player) {
		return isActive(player) && (player.isSpectator() || (player.getAbilities().flying && SpeedDial.isModifierDown()));
	}

	/** Positive steps are faster. */
	public static float scroll(float speed, int steps) {
		SpeedDialConfig config = SpeedDial.config();
		if (config.stepMode == StepMode.LINEAR) {
			speed += steps * Math.max(config.linearStep, 0.1F) * DEFAULT_SPEED;
		} else {
			for (int i = 0; i < Math.abs(steps); i++) {
				speed = steps > 0 ? faster(speed) : slower(speed);
			}
		}
		return Mth.clamp(speed, 0.0F, maxSpeed());
	}

	private static float faster(float speed) {
		return speed < VANILLA_MAX ? Math.min(speed + VANILLA_STEP, VANILLA_MAX) : speed * progressiveFactor();
	}

	private static float slower(float speed) {
		return speed > VANILLA_MAX ? Math.max(speed / progressiveFactor(), VANILLA_MAX) : Math.max(speed - VANILLA_STEP, 0.0F);
	}

	/** Each step above the vanilla maximum multiplies the speed by this. */
	private static float progressiveFactor() {
		return Math.max(SpeedDial.config().progressiveMultiplier, 1.01F);
	}

	private static float maxSpeed() {
		return Math.max(SpeedDial.config().maxSpeed, 0.0F) * DEFAULT_SPEED;
	}

	public static void change(LocalPlayer player, int steps) {
		set(player, scroll(player.getAbilities().getFlyingSpeed(), steps));
	}

	public static void reset(LocalPlayer player) {
		set(player, DEFAULT_SPEED);
	}

	private static void set(LocalPlayer player, float speed) {
		float oldSpeed = player.getAbilities().getFlyingSpeed();
		player.getAbilities().setFlyingSpeed(speed);
		onChanged(oldSpeed, speed);
	}

	public static void onChanged(float oldSpeed, float speed) {
		SpeedDialConfig config = SpeedDial.config();
		if (config.rememberSpeed) {
			config.rememberedSpeed = speed / DEFAULT_SPEED;
			rememberedSpeedChanged = true;
		}
		showSpeed(oldSpeed, speed);
	}

	/**
	 * Every abilities packet resets the flying speed. The game mode can change right after it.
	 * The remembered speed is restored on the next tick to see the new game mode.
	 */
	public static void onAbilitiesUpdated() {
		restorePending = true;
	}

	public static void tick(@Nullable LocalPlayer player) {
		SpeedDialConfig config = SpeedDial.config();
		if (restorePending) {
			restorePending = false;
			if (config.rememberSpeed && isActive(player) && player.isSpectator()) {
				player.getAbilities().setFlyingSpeed(rememberedSpeed());
			}
		}

		boolean spectator = player != null && player.isSpectator();
		if (wasSpectator && !spectator) {
			hideMessage();
		}
		wasSpectator = spectator;

		updateCreativeBoost(player);
	}

	/** Creative flight goes back to normal when the modifier is released. */
	public static void updateCreativeBoost(@Nullable LocalPlayer player) {
		SpeedDialConfig config = SpeedDial.config();
		boolean boost = isActive(player) && player.isCreative() && SpeedDial.isModifierDown();
		if (boost != creativeBoost) {
			creativeBoost = boost;
			if (player != null) {
				player.getAbilities().setFlyingSpeed(boost && config.rememberSpeed ? rememberedSpeed() : DEFAULT_SPEED);
			}
		}
		updateCreativeMessage(player, boost && player.getAbilities().flying);
	}

	/** The speed stays on screen the whole time the modifier is held while flying. */
	private static void updateCreativeMessage(@Nullable LocalPlayer player, boolean show) {
		HudAccessor hud = (HudAccessor) Minecraft.getInstance().gui.hud;
		boolean ownMessage = lastMessage != null && hud.speedDial$getOverlayMessage() == lastMessage;
		if (show && !creativeMessageShown) {
			float speed = player.getAbilities().getFlyingSpeed();
			showSpeed(speed, speed);
		} else if (show && ownMessage) {
			hud.speedDial$setOverlayMessageTime(MESSAGE_TICKS);
		} else if (!show && creativeMessageShown) {
			hideMessage();
		}
		creativeMessageShown = show;
	}

	/** Hides the speed message right away, unless something else has replaced it. */
	private static void hideMessage() {
		HudAccessor hud = (HudAccessor) Minecraft.getInstance().gui.hud;
		if (lastMessage != null && hud.speedDial$getOverlayMessage() == lastMessage) {
			hud.speedDial$setOverlayMessageTime(0);
		}
		lastMessage = null;
	}

	private static float rememberedSpeed() {
		return Mth.clamp(SpeedDial.config().rememberedSpeed * DEFAULT_SPEED, 0.0F, maxSpeed());
	}

	public static void saveRememberedSpeed() {
		if (rememberedSpeedChanged) {
			rememberedSpeedChanged = false;
			SpeedDial.config().save();
		}
	}

	private static void showSpeed(float oldSpeed, float speed) {
		SpeedDisplay display = SpeedDial.config().speedDisplay;
		if (display == SpeedDisplay.OFF) {
			return;
		}
		LocalPlayer player = Minecraft.getInstance().player;
		float sprint = player != null && player.isSprinting() ? 2.0F : 1.0F;
		float blocksPerSecond = speed * BLOCKS_PER_SECOND * sprint;
		String multiplier = String.format(Locale.ROOT, "%.1f", speed / DEFAULT_SPEED);
		Component message = switch (display) {
			case BLOCKS_PER_SECOND -> speedMessage(display, format(blocksPerSecond));
			case KILOMETERS_PER_HOUR -> speedMessage(display, format(blocksPerSecond * 3.6F));
			case MACH -> speedMessage(display, String.format(Locale.ROOT, "%.2f", blocksPerSecond / SPEED_OF_SOUND));
			case CHUNKS_PER_SECOND -> speedMessage(display, String.format(Locale.ROOT, "%.1f", blocksPerSecond / 16.0F));
			case G_FORCE -> speedMessage(display, multiplier, format(Math.abs(speed - oldSpeed) * sprint * G_FORCE));
			case MULTIPLIER, OFF -> speedMessage(SpeedDisplay.MULTIPLIER, multiplier);
		};
		lastMessage = message;
		Minecraft.getInstance().gui.hud.setOverlayMessage(message, false);
	}

	private static Component speedMessage(SpeedDisplay display, Object... args) {
		return Component.translatable("speed-dial.speed." + display.key(), args);
	}

	private static String format(float value) {
		return String.format(Locale.ROOT, value < 100.0F ? "%.1f" : "%.0f", value);
	}
}
