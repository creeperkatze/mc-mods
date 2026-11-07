plugins {
	id("mc-mods.fabric")
}

dependencies {
	implementation(mcLibrary("yacl-fabric"))
	compileOnly(mcLibrary("modmenu"))
	"localRuntime"(mcLibrary("modmenu"))
}
