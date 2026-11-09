plugins {
	id("dev.kikugie.stonecutter")
	id("mc-mods.root")
}

stonecutter active "26.3-fabric"

stonecutter parameters {
	constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge")

	// 26.1 renamed GUI rendering from render to extract
	replacements.string(current.parsed >= "26.1") {
		replace("GuiGraphics", "GuiGraphicsExtractor")
		replace("renderWidget", "extractWidgetRenderState")
		replace("renderListItems", "extractListItems")
		replace("renderContent", "extractContent")
		replace("renderWithTooltipAndSubtitles", "extractRenderStateWithTooltipAndSubtitles")
		replace("renderOverlayMessage", "extractOverlayMessage")
		replace("renderDefaultSprite", "extractDefaultSprite")
		replace(".renderOutline(", ".outline(")
		replace("renderDeferredElements()V", "extractDeferredElements(IIF)V")
	}
}
