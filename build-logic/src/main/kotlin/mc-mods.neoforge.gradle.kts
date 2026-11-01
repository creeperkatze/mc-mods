import org.gradle.language.jvm.tasks.ProcessResources

plugins {
	id("net.neoforged.moddev")
	id("mc-mods.mod")
}

val modId = modProperty("mod_id")
val modName = modProperty("mod_name")
val targetMinecraft = minecraftVersion

neoForge {
	version = mcVersion("neoforge")
	mods {
		register(modId.replace('-', '_')) {
			sourceSet(sourceSets["main"])
		}
	}
	runs {
		configureEach {
			ideName = "$modName NeoForge ${name.replaceFirstChar { it.titlecase() }}"
			ideFolderName = modName
		}
		register("client") {
			client()
			gameDirectory = rootProject.layout.projectDirectory.dir("run/$modId/$targetMinecraft/neoforge/client")
			programArguments.addAll("--quickPlaySingleplayer", "New World")
		}
		if (optionalModProperty("mod_environment") != "client") {
			register("server") {
				server()
				gameDirectory = rootProject.layout.projectDirectory.dir("run/$modId/$targetMinecraft/neoforge/server")
			}
		}
	}
}

tasks.named<ProcessResources>("processResources") {
	exclude("fabric.mod.json")
}
