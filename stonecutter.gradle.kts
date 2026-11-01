plugins {
	id("dev.kikugie.stonecutter")
	id("mc-mods.root")
}

stonecutter active "26.3-fabric"

stonecutter parameters {
	constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge")
}
