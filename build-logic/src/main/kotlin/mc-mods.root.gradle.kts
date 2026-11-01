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

tasks.register("vscode") {
	group = "ide"
	dependsOn(targetProjects.filter { it.name.endsWith("-fabric") }.map { "${it.path}:vscode" })
}
