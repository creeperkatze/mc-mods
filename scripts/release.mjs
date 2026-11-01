import { execFileSync } from 'node:child_process';
import { readFileSync, writeFileSync } from 'node:fs';

// Usage: node scripts/release.mjs <patch|minor|major|x.y.z> [mod] [--dry-run]
const args = process.argv.slice(2);
const dryRun = args.includes('--dry-run');
const [bump, modArgument] = args.filter((argument) => !argument.startsWith('--'));

const git = (...values) => execFileSync('git', values, { encoding: 'utf8' }).trim();
const fail = (message) => {
	console.error(`error: ${message}`);
	process.exit(1);
};

const mods = [
	...readFileSync('settings.gradle.kts', 'utf8').matchAll(/"([\w-]+)"\s+to\s+listOf\(/g),
].map((match) => match[1]);
const mod = modArgument ?? (mods.length === 1 ? mods[0] : undefined);
if (!bump || !mods.includes(mod)) {
	fail(`usage: pnpm release:<patch|minor|major> [mod] [--dry-run]. Mods: ${mods.join(', ')}`);
}

const propertiesPath = `${mod}/mod.properties`;
const changelogPath = `${mod}/CHANGELOG.md`;
const properties = readFileSync(propertiesPath, 'utf8');
const current = properties.match(/^mod_version=(.+)$/m)?.[1].trim();
if (!current) fail(`Missing mod_version in ${propertiesPath}`);

let next;
if (/^\d+\.\d+\.\d+$/.test(bump)) {
	next = bump;
} else if (['patch', 'minor', 'major'].includes(bump)) {
	const [major, minor, patch] = current.split(/[-+]/)[0].split('.').map(Number);
	next =
		bump === 'major'
			? `${major + 1}.0.0`
			: bump === 'minor'
				? `${major}.${minor + 1}.0`
				: `${major}.${minor}.${patch + 1}`;
} else {
	fail(`Unknown version bump: ${bump}`);
}

if (next === current) fail(`mod_version is already ${current}`);
if (git('status', '--porcelain')) fail('Commit or stash your changes first');
if (git('branch', '--show-current') !== 'main') fail('Release from the main branch');

const lines = readFileSync(changelogPath, 'utf8').replace(/\r\n/g, '\n').split('\n');
const start = lines.findIndex((line) => /^## \[?Unreleased\]?\s*$/i.test(line));
if (start < 0) fail(`No ## [Unreleased] section in ${changelogPath}`);
const end = lines.findIndex((line, index) => index > start && line.startsWith('## '));
const notes = lines
	.slice(start + 1, end < 0 ? undefined : end)
	.join('\n')
	.trim();
if (!notes) fail(`Nothing under ## [Unreleased] in ${changelogPath}`);

const tag = `${mod}/v${next}`;
console.log(`${mod}: ${current} -> ${next}, tag ${tag}`);
if (dryRun) process.exit(0);

const date = new Date().toISOString().slice(0, 10);
lines.splice(start, 1, '## [Unreleased]', '', `## [${next}] - ${date}`);
writeFileSync(changelogPath, lines.join('\n'));
writeFileSync(propertiesPath, properties.replace(/^mod_version=.*$/m, `mod_version=${next}`));
execFileSync(process.execPath, ['node_modules/oxfmt/bin/oxfmt', changelogPath], {
	stdio: 'inherit',
});
git('add', propertiesPath, changelogPath);
git('commit', '-m', `chore: release ${mod} ${next}`);
git('tag', tag);
git('push', '--atomic', 'origin', 'HEAD', `refs/tags/${tag}`);
console.log(`Pushed ${tag}. The Release workflow takes it from here.`);
