import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.util.Properties

// Mod properties live in the mod folder, the parent of every Minecraft and loader version project
val Project.modDirectory: Directory
	get() = requireNotNull(parent).layout.projectDirectory

fun Project.readModProperties(directory: Directory = modDirectory): Map<String, String> {
	val contents = providers.fileContents(directory.file("mod.properties")).asText.get()
	val properties = Properties().apply { contents.reader().use(::load) }
	return properties.stringPropertyNames().associateWith(properties::getProperty)
}

fun Project.modProperty(key: String): String =
	requireNotNull(optionalModProperty(key).takeIf(String::isNotBlank)) {
		"Missing $key in ${requireNotNull(parent).name}/mod.properties"
	}

fun Project.optionalModProperty(key: String): String = readModProperties()[key].orEmpty()

fun Project.modPropertyList(key: String): List<String> =
	optionalModProperty(key).split(',').map(String::trim).filter(String::isNotEmpty)

// Version projects are named <minecraft>-<loader>
val Project.minecraftVersion: String
	get() = name.substringBeforeLast('-')

val Project.loader: String
	get() = name.substringAfterLast('-')

// Precompiled plugins can't use the generated libs accessor
val Project.catalog: VersionCatalog
	get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

val Project.minecraftCatalog: VersionCatalog
	get() = extensions.getByType(VersionCatalogsExtension::class.java)
		.named("mc${minecraftVersion.replace('.', 'x')}")

fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

fun Project.mcVersion(alias: String): String = minecraftCatalog.version(alias)

fun Project.mcLibrary(alias: String): Provider<MinimalExternalModuleDependency> =
	minecraftCatalog.findLibrary(alias).orElseThrow {
		IllegalArgumentException("Missing dependency '$alias' for Minecraft $minecraftVersion")
	}
