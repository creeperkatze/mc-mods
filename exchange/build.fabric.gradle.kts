plugins {
	id("mc-mods.fabric")
}

dependencies {
	modConfiguration("compileOnly")(mcLibrary("voxy"))
	listOf(libs.sqlite.jdbc, libs.rocksdbjni, libs.aircompressor, libs.xz).forEach {
		implementation(it)
		"include"(it)
	}
	modConfiguration("localRuntime")(mcLibrary("modmenu"))
	modConfiguration("localRuntime")(mcLibrary("voxy"))
	modConfiguration("localRuntime")(mcLibrary("sodium"))
	modConfiguration("localRuntime")(mcLibrary("distanthorizons"))

	// Voxy only runs on 64-bit Windows and Linux
	attributesSchema.attribute(TrimNatives.TRIMMED)
	artifactTypes.named("jar") {
		attributes.attribute(TrimNatives.TRIMMED, false)
	}
	registerTransform(TrimNatives::class) {
		from.attribute(TrimNatives.TRIMMED, false)
		to.attribute(TrimNatives.TRIMMED, true)
		parameters.keptNatives.addAll("/Windows/x86_64/", "/Linux/x86_64/", "librocksdbjni-win64.dll", "librocksdbjni-linux64.so")
	}
}

configurations.named("includeInternal") {
	attributes.attribute(TrimNatives.TRIMMED, true)
}
