plugins {
	id("mc-mods.fabric")
}

dependencies {
	modConfiguration("implementation")(mcLibrary("yacl-fabric"))
	modConfiguration("compileOnly")(mcLibrary("modmenu"))
	modConfiguration("localRuntime")(mcLibrary("modmenu"))
}
