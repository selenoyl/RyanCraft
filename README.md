# RyanCraft

A Fabric mod for **Minecraft 1.21.11**: the feel of 2012, Tekkit-era Minecraft 1.2.5, plus a woods full of Kentucky wildlife.

![Kentucky wildlife on a 1.2.5-textured platform](docs/kentucky.png)

## Classic 1.2.5 pack
On first launch the mod downloads the **official 1.2.5 files from Mojang's servers** (the same ones the launcher uses) and builds a resource pack on your machine called *RyanCraft Classic 1.2.5*, then switches it on. No Mojang assets are shipped in this mod.
- **Textures**: every 1.2.5 block and item (yes, *that* gravel), the old crack animation, mobs and armor where the layout still matches, old Steve and zombie, the 1.2.5 font, clouds, rain, snow, and the old grass/foliage colours.
- **GUI**: the 1.2.5 hotbar, hearts, hunger, armor, XP bar, crosshair and buttons, and the original title-screen panorama.
- **Sounds**: the 1.2.5 sound set: footsteps, digging, damage, the old mob sounds, caves, rain, doors, explosions.
- **Music**: only the original C418 tracks (calm 1-3, hal 1-4, nuance 1-2, piano 1-3), everywhere.
- **Programmer Art** is switched on underneath so blocks added after 1.2.5 still look the part.

Turn it off any time in *Resource Packs*. Each part can be toggled in `config/ryancraft.json` (`classicTextures`, `classicSounds`, `classicMusic`, `classicProgrammerArt`).

![Title screen](docs/title.png)

## Atmosphere (MAtmos-style)
The mod reads your surroundings every second (biome, time of day, how many trees are around, running water, caves, altitude, rain) and mixes:
- **Beds that fade in and out**: wind (stronger high up, on mountains and in storms), howling wind in the snow, leaves rustling and distant twitter in forests, cricket chorus on warm nights, frogs in swamps and ponds, a babbling brook placed where the running water actually is, surf at the coast, rain pattering on leaves, deep cave air.
- **Calls placed around you in 3D**: songbirds (loudest at dawn), woodpeckers, cuckoos, crows, hawks over open country, gulls at the beach, owls and distant wolf howls at night, bullfrogs, cave drips and rumbles, gusts through the leaves.
- Wild turkeys **shock-gobble** back at owls, crows and hawks, like the real thing.

All of it plays on the *Ambient/Environment* volume slider and has subtitles. `ambience` / `ambienceVolume` in the config. The sounds are original synths (see `tools/synth_ambience.py`). Any resource pack can replace them with real recordings, since the files live under `assets/ryancraft/sounds/ambience/`.

## Animals
| Animal | Where | What it does |
|---|---|---|
| **Wild Turkey** | Hardwood forests, meadows, plains | Gobbles at dawn and at thunder; glides down; drops turkey and feathers |
| **Black Bear** (sometimes cinnamon) | Forests, taiga | Leaves you be, eats dropped berries and fish; fights back hard; mothers charge near cubs |
| **Raccoon** | Forests, swamps | Climbs trees, keeps its distance, steals food left on the ground; tail drops for a **Coonskin Cap** |
| **Striped Skunk** | Forest edges, plains, meadows | Crowd it (or a wolf/fox does) and it sprays: nausea and blindness. Sneak past |
| **Opossum** | Forests, swamps, plains | Hisses; hit it and it **plays possum** for a few seconds |
| **Beaver** | Rivers, swamps | Swims, slaps its tail in alarm, leaves gnawed sticks |
| **Mink** | Rivers, swamps | Swims and hunts fish |
| **Eastern Gray / Fox Squirrel** | Woods (gray) and open woods/edges (fox) | Chatters, flees, runs straight up tree trunks |
| **Songbirds** | Woods and fields | Northern Cardinal, American Goldfinch, American Robin, Blue Jay, Eastern Bluebird, each with its own song |
| **Bluegill, Crappie, Largemouth Bass, Channel Catfish** | Rivers, swamps | Bucket them, or catch them on a rod; cook for a fish fry |
| **Boar, Deer, Duck** | Forests, plains, rivers | Boar fights back; deer flee unless you sneak; ducks drop feathers |

Breeding foods: turkey and songbirds take seeds; bears take berries, honey or fish; raccoons take berries, eggs or fish; squirrels take berries or seeds; beavers take saplings.

## Biomes
Rainforest, Seasonal Forest, Shrubland and Tundra (the Beta 1.7 set), placed by **[TerraBlender](https://modrinth.com/mod/terrablender)**. Without TerraBlender everything else still works.

## Other classic tweaks
All on by default; each can be switched off in `config/ryancraft.json`:
- `disableChatReporting`: never creates a chat-signing key, so your messages can't be reported. Vanilla servers with `enforce-secure-profile=true` will refuse you while this is on.
- `hideReportButton`, `disableProfanityFilter`, `classicDefaultSkins` (Steve or Alex only).
- No "Java Edition" badge, and Alpha/Beta-style splash text.

## Install
1. [Fabric Loader](https://fabricmc.net/use/) 0.20.1+ for 1.21.11.
2. Put `ryancraft-1.0.0.jar`, [Fabric API](https://modrinth.com/mod/fabric-api) and (for the biomes) TerraBlender in `.minecraft/mods`.
3. First launch needs internet once to build the classic pack (about 25 MB, cached in `.minecraft/ryancraft/cache`).

## Build
Needs Java 21: `./gradlew build` (jar in `build/libs/`), `./gradlew runClient`.

Regenerating art and sound: `python3 tools/critters.py` (models + textures from one spec), `python3 tools/synth_ambience.py` (needs numpy, scipy, ffmpeg), `python3 tools/resources.py` (JSON data).
