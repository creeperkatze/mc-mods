import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider

fun Project.modProperty(key: String): String =
	requireNotNull(findProperty(key)?.toString()?.takeIf(String::isNotBlank)) {
		"Missing $key in $name/gradle.properties"
	}

fun Project.optionalModProperty(key: String): String = findProperty(key)?.toString().orEmpty()

fun Project.modPropertyList(key: String): List<String> =
	optionalModProperty(key).split(',').map(String::trim).filter(String::isNotEmpty)

// Precompiled plugins can't use the generated libs accessor
val Project.catalog: VersionCatalog
	get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> = findLibrary(alias).get()

val Project.minecraftVersion: String
	get() = catalog.version("minecraft")
