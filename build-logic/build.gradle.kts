plugins {
	`kotlin-dsl`
}

dependencies {
	implementation(libs.loom.plugin)
	implementation(libs.moddev.plugin)
	implementation(libs.mod.publish.plugin)
}

kotlin {
	jvmToolchain(25)
}
