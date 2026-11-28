package dev.creeperkatze.exchange;

import com.mojang.serialization.Dynamic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class CopyFlow {
	private static final int EDIT_BUTTON_WIDTH = 200;

	private static boolean loading;

	private CopyFlow() {
	}

	// Returns true when it takes over and runs load later
	public static boolean beforeWorldLoad(LevelStorageSource.LevelStorageAccess access, WorldStem stem, Runnable load) {
		if (loading || !LodMod.VOXY.isLoaded() && !LodMod.DISTANT_HORIZONS.isLoaded()) {
			return false;
		}
		try {
			LodWorld world = LodWorld.of(access, stem);
			List<LodMod> offered = targets(world).stream().filter(world::shouldOffer).toList();
			if (offered.isEmpty()) {
				return false;
			}
			Map<LodMod, Long> missing = MissingAreas.count(world);
			// So the next open doesn't count again until the source grows
			world.remember(offered.stream().filter(target -> missing.get(target) == 0).toList());
			List<LodMod> targets = useful(offered, missing);
			if (targets.isEmpty()) {
				return false;
			}
			Runnable then = () -> load(load);
			prompt(world, targets, missing, Component.translatable("exchange.prompt.skip"), () -> {
				world.remember(targets);
				then.run();
			}, then);
			return true;
		} catch (Exception e) {
			Exchange.LOGGER.error("Failed to check the world for LODs", e);
		}
		return false;
	}

	// Only the target mod has to be installed
	private static List<LodMod> targets(LodWorld world) {
		List<LodMod> targets = new ArrayList<>();
		for (LodMod target : LodMod.values()) {
			if (target.isLoaded() && world.hasLods(target.other())) {
				targets.add(target);
			}
		}
		return targets;
	}

	// Most missing chunks first
	private static List<LodMod> useful(List<LodMod> targets, Map<LodMod, Long> missing) {
		return targets.stream()
			.filter(target -> missing.get(target) > 0)
			.sorted(Comparator.comparing(missing::get).reversed())
			.toList();
	}

	public static Button editScreenButton(Screen editScreen, LevelStorageSource.LevelStorageAccess access) {
		Button button = Button.builder(Component.translatable("exchange.edit.copy"), pressed -> copyFromEditScreen(editScreen, access))
			.width(EDIT_BUTTON_WIDTH)
			.build();
		if (!LodMod.VOXY.isLoaded() && !LodMod.DISTANT_HORIZONS.isLoaded()) {
			button.active = false;
			button.setTooltip(Tooltip.create(Component.translatable("exchange.edit.copy.needs_mod")));
		}
		return button;
	}

	// Asks even if nothing changed since the last copy or skip
	private static void copyFromEditScreen(Screen editScreen, LevelStorageSource.LevelStorageAccess access) {
		Minecraft minecraft = Minecraft.getInstance();
		Runnable back = () -> minecraft.gui.setScreen(editScreen);
		LodWorld world;
		Map<LodMod, Long> missing;
		try {
			world = loadWorld(minecraft, access);
			missing = MissingAreas.count(world);
		} catch (Exception e) {
			Exchange.LOGGER.error("Failed to read the world for copying LODs", e);
			failed(back);
			return;
		}
		List<LodMod> targets = targets(world);
		if (targets.isEmpty()) {
			minecraft.gui.setScreen(new AlertScreen(back, Component.translatable("exchange.edit.none.title"), Component.translatable("exchange.edit.none.message")));
			return;
		}
		List<LodMod> useful = useful(targets, missing);
		if (useful.isEmpty()) {
			minecraft.gui.setScreen(new AlertScreen(back, Component.translatable("exchange.edit.same.title"), Component.translatable("exchange.edit.same.message")));
			return;
		}
		prompt(world, useful, missing, CommonComponents.GUI_CANCEL, back, back);
	}

	// Loads the world's registries the way Optimize World does
	private static LodWorld loadWorld(Minecraft minecraft, LevelStorageSource.LevelStorageAccess access) throws Exception {
		Dynamic<?> unfixedDataTag = access.getUnfixedDataTagWithFallback();
		int dataVersion = NbtUtils.getDataVersion(unfixedDataTag);
		if (DataFixers.getFileFixer().requiresFileFixing(dataVersion)) {
			throw new IllegalStateException("The world has to be upgraded by opening it first");
		}
		Dynamic<?> dataTag = DataFixTypes.LEVEL.updateToCurrentVersion(DataFixers.getDataFixer(), unfixedDataTag, dataVersion);
		WorldOpenFlows worldOpenFlows = minecraft.createWorldOpenFlows();
		PackRepository packRepository = ServerPacksSource.createPackRepository(access);
		try (WorldStem stem = worldOpenFlows.loadWorldStem(access, dataTag, false, packRepository)) {
			return LodWorld.of(access, stem);
		}
	}

	private static void prompt(LodWorld world, List<LodMod> targets, Map<LodMod, Long> missing, Component skipLabel, Runnable onSkip, Runnable then) {
		Minecraft.getInstance().gui.setScreen(new PromptScreen(targets, missing, skipLabel, target -> copy(world, target, then), onSkip, then));
	}

	private static void copy(LodWorld world, LodMod target, Runnable then) {
		LodCopy job = new LodCopy(world, target);
		Minecraft.getInstance().gui.setScreen(new CopyScreen(job, finished -> finish(world, finished, then)));
		job.start();
	}

	private static void finish(LodWorld world, LodCopy job, Runnable then) {
		Minecraft minecraft = Minecraft.getInstance();
		LodMod target = job.target();
		switch (job.state()) {
			case COMPLETED -> {
				// Copying straight back would add nothing either
				world.remember(List.of(LodMod.values()));
				Component title = Component.translatable("exchange.done.title", target.displayName());
				Component message = target.other().isLoaded() ? Component.translatable("exchange.done.message", target.other().displayName()) : null;
				SystemToast.add(minecraft.gui.toastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION, title, message);
				then.run();
			}
			case FAILED -> failed(then);
			default -> then.run();
		}
	}

	private static void failed(Runnable then) {
		Minecraft.getInstance().gui.setScreen(new AlertScreen(
			then, Component.translatable("exchange.failed.title"), Component.translatable("exchange.failed.message")
		));
	}

	private static void load(Runnable load) {
		loading = true;
		try {
			load.run();
		} finally {
			loading = false;
		}
	}
}
