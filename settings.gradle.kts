pluginManagement {
	includeBuild("build-logic")
	repositories {
		gradlePluginPortal()
		mavenCentral()
		maven("https://maven.fabricmc.net/")
		maven("https://maven.neoforged.net/releases/")
		maven("https://maven.kikugie.dev/releases/")
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
	id("dev.kikugie.stonecutter") version "0.9.8"
}

rootProject.name = "mc-mods"

val modTargets = mapOf(
	"speed-dial" to listOf("26.3")
)

dependencyResolutionManagement {
	versionCatalogs {
		modTargets.values.flatten().distinct().forEach { minecraft ->
			create("mc${minecraft.replace('.', 'x')}") {
				from(files("gradle/minecraft/$minecraft.versions.toml"))
			}
		}
	}
}

stonecutter {
	create(rootProject) {
		modTargets.forEach { (mod, targets) ->
			branch(mod) {
				targets.forEach { minecraft ->
					listOf("fabric", "neoforge").forEach { loader ->
						version("$minecraft-$loader", minecraft)
							.buildscript("build.$loader.gradle.kts")
					}
				}
			}
		}
		vcsVersion = "${modTargets.values.first().first()}-fabric"
	}
}
