plugins {
	id("mc-mods.fabric")
}

dependencies {
	modConfiguration("compileOnly")(mcLibrary("voxy"))
	compileOnly(libs.xz)
	compileOnly(libs.lwjgl.zstd)
	modConfiguration("localRuntime")(mcLibrary("modmenu"))
	modConfiguration("localRuntime")(mcLibrary("voxy"))
	modConfiguration("localRuntime")(mcLibrary("sodium"))
	modConfiguration("localRuntime")(mcLibrary("distanthorizons"))
}
