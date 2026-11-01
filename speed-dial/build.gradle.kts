plugins {
	id("mc-mods.mod")
}

repositories {
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://maven.isxander.dev/releases/")
	maven("https://maven.quiltmc.org/repository/release/")
}

dependencies {
	implementation(libs.yacl)
	compileOnly(libs.modmenu)
	"localRuntime"(libs.modmenu)
}
