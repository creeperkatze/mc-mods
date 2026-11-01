import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
	`java-library`
	id("net.fabricmc.fabric-loom")
}

val modId = modProperty("mod_id")
val javaVersion = catalog.version("java").toInt()

group = modProperty("maven_group")
version = "${modProperty("mod_version")}+$minecraftVersion"
base.archivesName = modId

java {
	toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
	withSourcesJar()
}

repositories {
	mavenCentral()
}

dependencies {
	"minecraft"("com.mojang:minecraft:$minecraftVersion")
	implementation(catalog.library("fabric-loader"))
	implementation(catalog.library("fabric-api"))
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
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release = javaVersion
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

tasks.withType<Jar>().configureEach {
	val licenseName = "LICENSE_$modId"
	from("LICENSE") {
		rename { licenseName }
	}
}

tasks.named<ProcessResources>("processResources") {
	val properties = mapOf(
		"version" to version.toString(),
		"mod_id" to modId,
		"mod_name" to modProperty("mod_name"),
		"mod_description" to modProperty("mod_description"),
		"mod_license" to modProperty("mod_license"),
		"minecraft_version" to minecraftVersion,
		"fabric_loader_version" to catalog.version("fabric-loader")
	)
	inputs.properties(properties)
	filesMatching("fabric.mod.json") {
		expand(properties)
	}
}

// Applied last, it needs the jar tasks registered above
pluginManager.apply("mc-mods.publish")
