import { globSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { basename, dirname, join } from 'node:path';

// Removes untranslated strings and empty files from the Crowdin download
// Also renames files like de_DE.json to de_de.json because Minecraft only loads lowercase names
for (const path of globSync('*/src/main/resources/assets/*/lang/*.json')) {
	if (basename(path) === 'en_us.json') continue;
	const strings = JSON.parse(readFileSync(path, 'utf8'));
	const translated = Object.fromEntries(
		Object.entries(strings).filter(([, value]) => value !== ''),
	);
	rmSync(path);
	if (Object.keys(translated).length === 0) continue;
	writeFileSync(
		join(dirname(path), basename(path).toLowerCase()),
		JSON.stringify(translated, null, '\t') + '\n',
	);
}
