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
import java.util.List;
import java.util.Set;

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
			Set<LodMod> answered = world.answered();
			List<LodMod> targets = targets(world).stream().filter(target -> !answered.contains(target)).toList();
			if (targets.isEmpty()) {
				return false;
			}
			Runnable then = () -> load(load);
			prompt(world, targets, Component.translatable("exchange.prompt.skip"), () -> {
				world.markAnswered(targets);
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

	// Asks even if the world was already copied or skipped
	private static void copyFromEditScreen(Screen editScreen, LevelStorageSource.LevelStorageAccess access) {
		Minecraft minecraft = Minecraft.getInstance();
		Runnable back = () -> minecraft.gui.setScreen(editScreen);
		LodWorld world;
		try {
			world = loadWorld(minecraft, access);
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
		prompt(world, targets, CommonComponents.GUI_CANCEL, back, back);
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

	private static void prompt(LodWorld world, List<LodMod> targets, Component skipLabel, Runnable onSkip, Runnable then) {
		Minecraft.getInstance().gui.setScreen(new PromptScreen(targets, skipLabel, target -> copy(world, target, then), onSkip, then));
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
				// Both mods have the LODs now
				world.markAnswered(List.of(LodMod.values()));
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
