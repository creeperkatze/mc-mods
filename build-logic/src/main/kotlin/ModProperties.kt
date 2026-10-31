import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

/** A property from the mod's gradle.properties that has to be set. */
fun Project.modProperty(key: String): String =
	requireNotNull(findProperty(key)?.toString()?.takeIf(String::isNotBlank)) {
		"Missing $key in ${project.name}/gradle.properties"
	}

fun Project.optionalModProperty(key: String): String = findProperty(key)?.toString().orEmpty()

/** A comma separated property as a list. */
fun Project.modPropertyList(key: String): List<String> =
	optionalModProperty(key).split(',').map(String::trim).filter(String::isNotEmpty)

/** The gradle/libs.versions.toml catalog. Precompiled plugins can't use the generated libs accessor. */
val Project.catalog: VersionCatalog
	get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

val Project.minecraftVersion: String
	get() = catalog.version("minecraft")
