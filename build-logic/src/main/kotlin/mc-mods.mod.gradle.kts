import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
	`java-library`
}

val modId = modProperty("mod_id")
val javaVersion = catalog.version("java").toInt()
val releaseVersion = "${modProperty("mod_version")}+$minecraftVersion"

group = modProperty("maven_group")
version = "$releaseVersion-$loader"
base.archivesName = modId

java {
	toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
	withSourcesJar()
}

repositories {
	mavenCentral()
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://maven.isxander.dev/releases/")
	maven("https://maven.quiltmc.org/repository/release/")
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
	from(modDirectory.file("LICENSE")) {
		rename { licenseName }
	}
}

tasks.named<ProcessResources>("processResources") {
	val properties = mapOf(
		"version" to releaseVersion,
		"mod_id" to modId,
		"neoforge_mod_id" to modId.replace('-', '_'),
		"mod_name" to modProperty("mod_name"),
		"mod_description" to modProperty("mod_description"),
		"mod_license" to modProperty("mod_license"),
		"minecraft_version" to minecraftVersion,
		"fabric_loader_version" to mcVersion("fabric-loader"),
		"neoforge_version" to mcVersion("neoforge"),
		"yacl_version" to mcVersion("yacl-$loader").removeSuffix("-$loader")
	)
	inputs.properties(properties)
	filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
		expand(properties)
	}
}
