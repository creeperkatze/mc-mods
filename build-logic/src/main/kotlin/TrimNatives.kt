import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

// Drops native libraries that match none of the kept paths
abstract class TrimNatives : TransformAction<TrimNatives.Parameters> {
	interface Parameters : TransformParameters {
		@get:Input
		val keptNatives: ListProperty<String>
	}

	@get:InputArtifact
	abstract val input: Provider<FileSystemLocation>

	override fun transform(outputs: TransformOutputs) {
		val jar = input.get().asFile
		val kept = parameters.keptNatives.get()
		ZipFile(jar).use { zip ->
			val entries = zip.entries().toList()
			val removed = entries.filter { isNative(it.name) && kept.none(it.name::contains) }
			if (removed.isEmpty()) {
				outputs.file(input)
				return
			}
			// Loom names the nested mod after the file
			ZipOutputStream(outputs.file(jar.name).outputStream()).use { out ->
				for (entry in entries - removed.toSet()) {
					out.putNextEntry(ZipEntry(entry.name).apply { time = entry.time })
					zip.getInputStream(entry).use { it.copyTo(out) }
					out.closeEntry()
				}
			}
		}
	}

	private fun isNative(name: String) = NATIVE_EXTENSIONS.any(name::endsWith)

	companion object {
		val TRIMMED: Attribute<Boolean> = Attribute.of("dev.creeperkatze.trimmed-natives", Boolean::class.javaObjectType)
		private val NATIVE_EXTENSIONS = listOf(".dll", ".so", ".dylib", ".jnilib")
	}
}
