import { readFileSync, writeFileSync } from 'node:fs';

// Reads the mods, Minecraft versions and loaders from settings.gradle.kts into the pick lists of .vscode/tasks.json
const settings = readFileSync('settings.gradle.kts', 'utf8');
const strings = (list) => [...list.matchAll(/"([^"]+)"/g)].map((match) => match[1]);

const targets = [...settings.matchAll(/"([\w-]+)"\s+to\s+listOf\(([^)]*)\)/g)].map(
	([, mod, versions]) => ({
		mod,
		versions: strings(versions),
	}),
);
const loaders = strings(settings.match(/listOf\(([^)]*)\)\.forEach \{ loader ->/)[1]);

const options = {
	mod: targets.map((target) => target.mod),
	// Newest first, so the newest version is the default
	minecraft: [...new Set(targets.flatMap((target) => target.versions))].sort((a, b) =>
		b.localeCompare(a, undefined, { numeric: true }),
	),
	loader: loaders,
};

const path = '.vscode/tasks.json';
const tasks = JSON.parse(readFileSync(path, 'utf8'));
for (const input of tasks.inputs) {
	input.options = options[input.id];
	input.default = input.options[0];
}
writeFileSync(path, JSON.stringify(tasks, null, '\t') + '\n');
