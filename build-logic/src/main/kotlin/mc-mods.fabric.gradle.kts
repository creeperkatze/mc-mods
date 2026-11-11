import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.language.jvm.tasks.ProcessResources

apply(plugin = if (isObfuscated) "net.fabricmc.fabric-loom-remap" else "net.fabricmc.fabric-loom")
apply(plugin = "mc-mods.mod")

val modId = modProperty("mod_id")
val modName = modProperty("mod_name")
val targetMinecraft = minecraftVersion
val loom = extensions.getByType<LoomGradleExtensionAPI>()

dependencies {
	"minecraft"("com.mojang:minecraft:$minecraftVersion")
	if (isObfuscated) {
		"mappings"(loom.officialMojangMappings())
	}
	modConfiguration("implementation")(mcLibrary("fabric-loader"))
	modConfiguration("implementation")(mcLibrary("fabric-api"))
}

loom.apply {
	mods {
		register(modId) {
			sourceSet(project.extensions.getByType<SourceSetContainer>()["main"])
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
			// Shared by all mods, so settings and worlds carry over between them
			runDir(rootProject.file("run/$targetMinecraft/fabric/$name").relativeTo(project.projectDir).invariantSeparatorsPath)
		}
	}
}

tasks.named<ProcessResources>("processResources") {
	exclude("META-INF/neoforge.mods.toml")
}
