import { readFileSync, writeFileSync } from "node:fs";

// Reads the mods and Minecraft versions from settings.gradle.kts into the pick lists of .vscode/tasks.json
const settings = readFileSync("settings.gradle.kts", "utf8");
const targets = [...settings.matchAll(/"([\w-]+)"\s+to\s+listOf\(([^)]*)\)/g)].map(([, mod, versions]) => ({
	mod,
	versions: [...versions.matchAll(/"([^"]+)"/g)].map((match) => match[1]),
}));

const options = {
	mod: targets.map((target) => target.mod),
	target: targets.flatMap((target) => target.versions).flatMap((version) => [`${version}-fabric`, `${version}-neoforge`]),
};

const path = ".vscode/tasks.json";
const tasks = JSON.parse(readFileSync(path, "utf8"));
for (const input of tasks.inputs) {
	input.options = [...new Set(options[input.id])];
	input.default = input.options[0];
}
writeFileSync(path, JSON.stringify(tasks, null, "\t") + "\n");
