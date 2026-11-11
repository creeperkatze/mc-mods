# Gather

[![Discord](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-singular_vector.svg)](https://discord.gg/Qds5QMrnDv)
[![Ko-fi](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/donate/kofi-singular_vector.svg)](https://ko-fi.com/creeperkatze)

Gather turns Open to LAN into a real way to host your world for friends. Invite more players, name your server, decide who gets in and keep playing together even without an internet connection.

## ✨ Features

**📡 Offline mode**

Turn off Online Mode so friends can still join when there's no internet connection, like at a LAN party.

**👥 More players**

Raise the player limit from vanilla's 8 up to 50.

**🏷️ Server name**

Give your world its own name in your friends' LAN server list. Use `&` codes for colors and styles, like `&6` for gold or `&l` for bold.

**📋 Whitelist and bans**

The whitelist, bans and IP bans work like on a real server and are saved with each world. You can use their commands even without cheats.

**💤 AFK kick**

Kick players who haven't moved for a while. You are never kicked from your own world.

**🔁 Open on load**

Open your world to LAN every time you load it, with the same settings as last time.

## ⚙️ Settings

On 26.3, the settings are in the **Multiplayer** section of **World Options** in the pause menu. On 1.21.11 and 26.2, press **Host Settings...** on the **Open to LAN** screen. Every world keeps its own settings.

| Option       | Description                                                                                                             | Default |
| ------------ | ----------------------------------------------------------------------------------------------------------------------- | ------- |
| Online Mode  | When off, players can join without signing in. Anyone on your network can then use any name, so consider the whitelist. | On      |
| Max Players  | How many players can join, from 2 to 50.                                                                                | 8       |
| Server Name  | The name your friends see in their LAN server list. Leave it empty to use your name and the world name, like vanilla.   | Empty   |
| Whitelist    | Only players on the whitelist can join.                                                                                 | Off     |
| AFK Kick     | Kicks players who haven't moved for this long, up to 60 minutes.                                                        | Off     |
| Open on Load | Opens the world to LAN with the same settings every time you load it.                                                   | Off     |

## 💬 Commands

As the host, you can use these commands even when cheats are off.

| Command                 | Description                                              |
| ----------------------- | -------------------------------------------------------- |
| `/whitelist`            | Add and remove players or turn the whitelist on and off. |
| `/ban`, `/ban-ip`       | Ban a player or an IP address from your world.           |
| `/pardon`, `/pardon-ip` | Lift a ban.                                              |
| `/banlist`              | List all bans.                                           |
| `/kick`                 | Kick a player.                                           |
| `/setidletimeout`       | Change the AFK kick time.                                |

## 🌐 Translating

Translations are managed on [Crowdin](https://crowdin.com/project/creeperkatze-mc-mods). You can contribute without any technical knowledge, just pick your language and start translating.
