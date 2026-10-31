plugins {
	id("mc-mods.mod")
}

repositories {
	maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
	compileOnly(libs.modmenu)
	"localRuntime"(libs.modmenu)
}
