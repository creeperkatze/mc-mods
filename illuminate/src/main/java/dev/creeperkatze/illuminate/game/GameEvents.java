package dev.creeperkatze.illuminate.game;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import dev.creeperkatze.illuminate.scene.Rgb;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// Collects events on the game thread and hands them to the lighting thread
public final class GameEvents {
	private static final int MAX_QUEUED = 64;
	private static final double FIREWORK_RANGE = 96.0;
	private static final Queue<GameEvent> QUEUE = new ConcurrentLinkedQueue<>();

	private GameEvents() {
	}

	public static void post(GameEvent event) {
		if (QUEUE.size() < MAX_QUEUED) {
			QUEUE.add(event);
		}
	}

	public static List<GameEvent> drain() {
		List<GameEvent> events = new ArrayList<>();
		GameEvent event;
		while ((event = QUEUE.poll()) != null) {
			events.add(event);
		}
		return events;
	}

	public static void onDamage(ClientboundDamageEventPacket packet) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		ClientLevel level = client.level;
		if (player == null || level == null || packet.entityId() != player.getId()) {
			return;
		}
		String type = packet.sourceType().unwrapKey().map(key -> key.identifier().getPath()).orElse("generic");
		Vec3 source = packet.getSource(level).getSourcePosition();
		post(new GameEvent.Damage(type, source == null ? Float.NaN : horizontalPosition(source)));
	}

	public static void onExplosion(Vec3 center, float radius) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		double distance = player.position().distanceTo(center);
		float strength = (float) Math.clamp(1.0 - distance / (radius * 4.0 + 16.0), 0.0, 1.0);
		if (strength > 0.0F) {
			post(new GameEvent.Explosion(strength, horizontalPosition(center)));
		}
	}

	public static void onFirework(Vec3 position, List<FireworkExplosion> explosions) {
		Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
		double distance = camera.position().distanceTo(position) / FIREWORK_RANGE;
		// Falls off slowly at first, so fireworks nearby burst at almost full strength
		float strength = (float) Math.clamp(1.0 - distance * distance, 0.0, 1.0);
		if (strength <= 0.0F) {
			return;
		}
		List<Rgb> colors = new ArrayList<>();
		List<Rgb> fadeColors = new ArrayList<>();
		for (FireworkExplosion explosion : explosions) {
			explosion.colors().forEach(color -> colors.add(Rgb.of(color)));
			explosion.fadeColors().forEach(color -> fadeColors.add(Rgb.of(color)));
		}
		if (colors.isEmpty()) {
			return;
		}
		float u = horizontalPosition(position);
		float v = 0.5F - (float) Math.clamp((position.y - camera.position().y) / 20.0, -1.0, 1.0) * 0.4F;
		post(new GameEvent.Firework(List.copyOf(colors), List.copyOf(fadeColors), strength, Float.isNaN(u) ? 0.5F : u, v));
	}

	public static void onTotem() {
		post(new GameEvent.Totem());
	}

	// Maps a world position to where it appears from your left to your right
	public static float horizontalPosition(Vec3 target) {
		Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
		return horizontalPosition(camera, target.x - camera.position().x, target.z - camera.position().z);
	}

	public static float horizontalPosition(@Nullable Camera camera, double dx, double dz) {
		double length = Math.sqrt(dx * dx + dz * dz);
		if (camera == null || length < 0.5) {
			return Float.NaN;
		}
		Vector3fc left = camera.leftVector();
		double right = -(dx * left.x() + dz * left.z()) / length;
		return (float) (0.5 + 0.5 * right);
	}
}
