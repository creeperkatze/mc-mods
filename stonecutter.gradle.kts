plugins {
	id("dev.kikugie.stonecutter")
	id("mc-mods.root")
}

stonecutter active "26.3-fabric"

stonecutter parameters {
	constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge")

	// 1.21.11 renamed ResourceLocation to Identifier and moved Util
	replacements.string(current.parsed >= "1.21.11") {
		replace("ResourceLocation", "Identifier")
		replace("import net.minecraft.Util;", "import net.minecraft.util.Util;")
		replace("key.location()", "key.identifier()")
	}

	// Renamed somewhere between 1.21.1 and 1.21.11
	replacements.string(current.parsed >= "1.21.11") {
		replace("camera.getPosition()", "camera.position()")
		replace("camera.getLookVector()", "camera.forwardVector()")
		replace("camera.getLeftVector()", "camera.leftVector()")
		replace("MobEffects.CONFUSION", "MobEffects.NAUSEA")
	}

	// 1.21.1 doesn't ship jspecify, only the JetBrains annotations
	replacements.string(current.parsed >= "1.21.11") {
		replace("org.jetbrains.annotations.Nullable", "org.jspecify.annotations.Nullable")
	}

	// 26.1 renamed GUI rendering from render to extract
	replacements.string(current.parsed >= "26.1") {
		replace("GuiGraphics", "GuiGraphicsExtractor")
		replace("renderWidget", "extractWidgetRenderState")
		replace("renderListItems", "extractListItems")
		replace("renderContent", "extractContent")
		replace("renderBackground", "extractBackground")
		replace("renderWithTooltipAndSubtitles", "extractRenderStateWithTooltipAndSubtitles")
		replace("renderOverlayMessage", "extractOverlayMessage")
		replace("renderDefaultSprite", "extractDefaultSprite")
		replace(".renderOutline(", ".outline(")
		replace("renderDeferredElements()V", "extractDeferredElements(IIF)V")
		replace("graphics.drawString(", "graphics.text(")
		replace("graphics.drawCenteredString(", "graphics.centeredText(")
	}

	// 26.2 moved screens, toasts and the HUD from Minecraft to Gui
	replacements.string(current.parsed >= "26.2") {
		replace("minecraft.setScreen(", "minecraft.gui.setScreen(")
		replace(".getToastManager()", ".gui.toastManager()")
		replace(".gui.getChat()", ".gui.hud.getChat()")
		replace(".gui.getBossOverlay()", ".gui.hud.getBossOverlay()")
		replace(".gui.setOverlayMessage(", ".gui.hud.setOverlayMessage(")
		replace("getMainCamera()", "mainCamera()")
		replace("getNightVisionScale(", "nightVisionScale(")
	}
}
