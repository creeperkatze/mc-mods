val targetProjects = rootProject.childProjects.values.flatMap { it.childProjects.values }

tasks.register("build") {
	group = "build"
	dependsOn(targetProjects.map { "${it.path}:build" })
}

tasks.register<Delete>("clean") {
	group = "build"
	delete(layout.buildDirectory)
	dependsOn(targetProjects.map { "${it.path}:clean" })
}

tasks.register("genSources") {
	group = "ide"
	dependsOn(targetProjects.filter { it.name.endsWith("-fabric") }.map { "${it.path}:genSources" })
}

// Runs the client of the first mod for a target, which loads every other mod for it too
tasks.register("runAll") {
	group = "run"
	val target = providers.gradleProperty("runAll").orNull
		?: throw GradleException("Pass a target, e.g. ./gradlew runAll -PrunAll=26.3-fabric")
	val host = targetProjects(target).firstOrNull()
		?: throw GradleException("No mod supports $target")
	dependsOn("${host.path}:runClient")
}

tasks.register("vscode") {
	group = "ide"
	dependsOn(targetProjects.filter { it.name.endsWith("-fabric") }.map { "${it.path}:vscode" })
}
