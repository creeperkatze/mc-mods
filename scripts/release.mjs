import { execFileSync } from 'node:child_process';
import { existsSync, readFileSync, writeFileSync } from 'node:fs';

// Usage: node scripts/release.mjs <patch|minor|major|x.y.z> <mod> [--dry-run]
const args = process.argv.slice(2);
const dryRun = args.includes('--dry-run');
const [bump, mod] = args.filter((argument) => !argument.startsWith('--'));

const git = (...values) => execFileSync('git', values, { encoding: 'utf8' }).trim();
const fail = (message) => {
	console.error(`error: ${message}`);
	process.exit(1);
};

const propertiesPath = `${mod}/mod.properties`;
const changelogPath = `${mod}/CHANGELOG.md`;
if (!bump || !mod || !existsSync(propertiesPath)) {
	fail('usage: pnpm release <patch|minor|major|x.y.z> <mod> [--dry-run]');
}

const properties = readFileSync(propertiesPath, 'utf8');
const current = properties.match(/^mod_version=(.+)$/m)?.[1].trim();
if (!current) fail(`Missing mod_version in ${propertiesPath}`);

const [major, minor, patch] = current.split('.').map(Number);
const next =
	{
		major: `${major + 1}.0.0`,
		minor: `${major}.${minor + 1}.0`,
		patch: `${major}.${minor}.${patch + 1}`,
	}[bump] ?? bump;
if (!/^\d+\.\d+\.\d+$/.test(next)) fail(`Unknown version bump: ${bump}`);
if (next === current) fail(`mod_version is already ${current}`);
if (git('status', '--porcelain')) fail('Commit or stash your changes first');
if (git('branch', '--show-current') !== 'main') fail('Release from the main branch');

const changelog = readFileSync(changelogPath, 'utf8').replace(/\r\n/g, '\n');
const unreleased = changelog.split(/^## /m).find((section) => section.startsWith('[Unreleased]'));
const notes = unreleased?.slice('[Unreleased]'.length).trim();
if (!notes) fail(`Nothing under ## [Unreleased] in ${changelogPath}`);

const tag = `${mod}/v${next}`;
console.log(`${mod}: ${current} -> ${next}, tag ${tag}`);
if (dryRun) process.exit(0);

const date = new Date().toISOString().slice(0, 10);
writeFileSync(
	changelogPath,
	changelog.replace('## [Unreleased]', `## [Unreleased]\n\n## [${next}] - ${date}`),
);
writeFileSync(propertiesPath, properties.replace(/^mod_version=.*$/m, `mod_version=${next}`));
git('add', propertiesPath, changelogPath);
git('commit', '-m', `chore(${mod}): release ${next}`);
git('tag', tag);
git('push', '--atomic', 'origin', 'HEAD', `refs/tags/${tag}`);
console.log(`Pushed ${tag}. The Release workflow takes it from here.`);
