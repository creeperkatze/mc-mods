![Illuminate](https://raw.githubusercontent.com/creeperkatze/mc-mods/main/illuminate/media/banner.png)

[![Discord](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-singular_vector.svg)](https://discord.gg/Qds5QMrnDv)
[![Ko-fi](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/donate/kofi-singular_vector.svg)](https://ko-fi.com/creeperkatze)

Illuminate turns the RGB lights around your desk into part of the game. They glow with the sunset, flash when lightning strikes and react to every hit you take.

![Showcase](https://raw.githubusercontent.com/creeperkatze/mc-mods/main/illuminate/media/screenshots/variants/portal.webp)

## ✨ Features

### World

Your lights follow the sky, biome and time of day, darken in caves and flash with lightning.

### Effects

Your lights react when you take damage, burn, freeze, drown or drink a potion.

### Direction

Lights placed to your left or right react to hits from that side.

## 🔧 Setup

### SignalRGB

1. In the Illuminate settings, open **Devices** and press **Install** next to **SignalRGB Effect**. This copies the effect to `Documents\WhirlwindFX\Effects`.
2. Restart SignalRGB and select the **Illuminate** effect.
3. Place your devices on the SignalRGB canvas where they are around your screen.

### OpenRGB

1. Start OpenRGB, open the **SDK Server** tab and press **Start Server**.
2. Enable OpenRGB in the Illuminate settings under **Devices**. The default address is `127.0.0.1:6742`.
3. Set where each device sits under **Device Positions** in the **OpenRGB** section of the **Devices** tab, so directional effects land on the right side.

You can use SignalRGB and OpenRGB at the same time, as long as each device is controlled by only one of them. A device controlled by both flickers. Turn it off in SignalRGB or set it to **Off** under **Device Positions** in the OpenRGB section.

## ⚙️ Settings

Open the config screen from the Mods screen (Mod Menu on Fabric). The **Preview** tab plays every effect on your lights so you can test them.

### General

| Option          | Description                                                                                                                                                                                          | Default  |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| Enabled         | Controls your lights from Minecraft. When off, your lights go back to their own software.                                                                                                            | On       |
| Brightness      | Overall brightness of your lights, from 5% to 100%.                                                                                                                                                  | 100%     |
| Saturation      | How colorful your lights are, from 0% to 200%. Above 100% makes colors stronger on LEDs.                                                                                                             | 125%     |
| Smoothing       | How slowly the lights change between biomes, caves and times of day. Flashes are not affected.                                                                                                       | 50%      |
| Frame Rate      | How often the lights update, from 10 to 60 FPS. Lower it if your devices lag behind.                                                                                                                 | 30 FPS   |
| Reduce Flashing | Makes damage, lightning and explosion flashes softer and slower.                                                                                                                                     | Off      |
| Dim in Menus    | Dims your lights while the pause menu, your inventory or another screen is open in a world.                                                                                                          | On       |
| Main Menu       | What your lights do in the main menu, the world list and the server list. _Panorama_ shows the colors of the panorama, _Color_ shows the Main Menu Color and _Off_ lets your RGB software take over. | Panorama |
| Main Menu Color | The color shown when Main Menu is set to Color.                                                                                                                                                      | White    |

### Effects

| Option                      | Description                                                                                                      | Default |
| --------------------------- | ---------------------------------------------------------------------------------------------------------------- | ------- |
| Biomes                      | Lights the room based on the biome, the time of day, the sun and the moon. Caves take the color of nearby light. | On      |
| Weather                     | Rain, snow, Nether embers and End particles in the room.                                                         | On      |
| Underwater and Lava         | Colors the room while your head is in water, lava or powder snow.                                                | On      |
| Portals                     | A purple swirl while you stand in a Nether portal.                                                               | On      |
| Boss Bars                   | Tints the top of the room in the color of the boss bar.                                                          | On      |
| Status Effects              | Tints the room in the colors of your status effects.                                                             | On      |
| Low Health                  | A red heartbeat that gets faster as your health drops.                                                           | On      |
| Fire, Freezing and Drowning | Lights up when you burn, freeze or run out of air.                                                               | On      |
| Sleep                       | Dims the lights as you fall asleep.                                                                              | On      |
| Damage                      | Flashes when you take damage, stronger on the side the hit came from.                                            | On      |
| World Events                | Flashes for nearby lightning and explosions, totems of undying and level ups.                                    | On      |
| Fireworks                   | Shows nearby fireworks in their colors, on the side and at the height they explode.                              | On      |
| Event Intensity             | How strong damage flashes and world events are, from 10% to 150%.                                                | 100%    |

### Devices

| Option                      | Description                                                                                                                 | Default   |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------- | --------- |
| Use SignalRGB               | Sends the lighting to the Illuminate effect in SignalRGB. Only available on Windows.                                        | On        |
| Switch Effect Automatically | Switches SignalRGB to the Illuminate effect when Minecraft starts sending lighting.                                         | On        |
| SignalRGB Port              | Port of the SignalRGB canvas API.                                                                                           | 16034     |
| Use OpenRGB                 | Controls your devices through the OpenRGB SDK server.                                                                       | Off       |
| OpenRGB Address             | Address of the OpenRGB SDK server.                                                                                          | 127.0.0.1 |
| OpenRGB Port                | Port of the OpenRGB SDK server.                                                                                             | 6742      |
| Device Positions            | Where each OpenRGB device is in your room: _Left_, _Center_, _Right_, _Above_, _Below_, _Surround_, _Whole Scene_ or _Off_. | Center    |

## 🌐 Translating

Translations are managed on [Crowdin](https://crowdin.com/project/creeperkatze-mc-mods). You can contribute without any technical knowledge, just pick your language and start translating.
