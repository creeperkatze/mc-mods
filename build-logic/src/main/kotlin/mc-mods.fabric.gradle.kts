import org.gradle.language.jvm.tasks.ProcessResources

plugins {
	id("net.fabricmc.fabric-loom")
	id("mc-mods.mod")
}

val modId = modProperty("mod_id")
val modName = modProperty("mod_name")
val targetMinecraft = minecraftVersion

dependencies {
	"minecraft"("com.mojang:minecraft:$minecraftVersion")
	implementation(mcLibrary("fabric-loader"))
	implementation(mcLibrary("fabric-api"))
}

loom {
	mods {
		register(modId) {
			sourceSet(sourceSets["main"])
		}
	}
	runs {
		if (optionalModProperty("mod_environment") == "client") {
			remove(getByName("server"))
		}
		configureEach {
			isIdeConfigGenerated = true
			configName = "$modName Fabric ${name.replaceFirstChar { it.titlecase() }}"
			ideConfigFolder.set(modName)
			runDir(rootProject.file("run/$modId/$targetMinecraft/fabric/$name").relativeTo(project.projectDir).invariantSeparatorsPath)
		}
		named("client") {
			programArgs("--quickPlaySingleplayer", "New World")
		}
	}
}

tasks.named<ProcessResources>("processResources") {
	exclude("META-INF/neoforge.mods.toml")
}
