[← Wiki home](Home.md)

# Settings and Commands

## The config file

Options are in `config/ryancraft.json`. Edit it with the game closed.

| Option | Default | What it does |
|---|---|---|
| `classicPack` | `true` | Uses the built-in classic 1.2.5 texture pack |
| `classicTextures`, `classicSounds`, `classicMusic`, `classicProgrammerArt` | `true` | Each part of the classic pack |
| `ambience` | `true` | RyanCraft's ambient sounds |
| `ambienceVolume` | `1.0` | How loud they are |
| `fireflies` | `true` | Fireflies on warm nights |
| `fireflyAmount` | `1.0` | How many fireflies (0.5 is half as many). Lower it if they slow your game |
| `thirst` | `true` | The thirst bar |
| `oldCombat` | `true` | No attack cooldown |
| `disableChatReporting`, `hideReportButton`, `disableProfanityFilter` | `true` | Classic-style chat |
| `classicDefaultSkins` | `true` | Steve and Alex only |

## Commands

| Command | Who | What it does |
|---|---|---|
| `/season` | Anyone | Shows the season |
| `/season set <season> [early\|mid\|late]` | Operators | Sets the season |
| `/thirst` | Anyone | Shows your thirst |
| `/thirst set <0-20>` | Operators | Sets thirst |
| `/weather duststorm [duration]` | Operators | Starts a dust storm (`/weather clear` ends it) |
| `/duststorm [start\|stop]` | Operators | Same as above |
| `/stronghold` | Operators | Shows where Eyes of Ender lead |
