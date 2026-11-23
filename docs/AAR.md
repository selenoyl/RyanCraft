# RyanCraft After-Action Review

Lessons from playtest feedback, kept as a checklist. Check every new mob, block, texture, sound or tree against it
**before** showing it or shipping it. When feedback turns up a new kind of mistake, add it here.

## Models (mobs)
- [ ] **Limbs attach.** Every arm, leg, wing, fin and head touches or overlaps its parent. Check in a render from the front, side and below.
      (Bullfrog hands floated in front of the body.)
- [ ] **No clipping or z-fighting.** No two boxes share a face plane where they overlap. Offset overlapping boxes by at least 0.5px, or shrink one inside the other.
      (Alligator head flickered into the body.)
- [ ] **Antlers and horns sit on the head.** They don't float above it, and they copy the head's rotation. (Deer antlers floated.)
- [ ] **Proportions read right in game.** The head isn't smaller than a loaf of a body (bobcat), small animals stay small (bullfrog was too big),
      and limbs are long enough. Check against the player model and vanilla mobs side by side.
- [ ] **Faces read.** Eyes are visible pixels (raptor eyes are one black pixel), there's no stray "d-pad" pixel (chipmunk), and mouths make sense (shark: underslung, teeth, gum-coloured gaps).
- [ ] **Wings spread wide when flying.** Wingspan should look real (crow wings were too stubby).
- [ ] **Animation matches motion.** Flyers flap while climbing or taking off, and only glide when level or sinking (eagle rose without flapping).
      Runners run at believable speeds (deer and pronghorn fled comically fast). Keep base speed x flee multiplier at
      about 0.45 or less; a cow's panic is 0.4.
- [ ] **No genders the game can't show.** Minecraft mobs don't show sex, so don't add female/male-only variants (pronghorn doe dropped).
- [ ] **Patterns look organic.** No perfectly straight or tessellated stripes; vary stripe ends (zebra).

## Behaviour
- [ ] **Habitat behaviour.** Water mobs keep to their depth: jellyfish stay deep and never surface. Ducks prefer streams and lakes.
      Climbers climb: squirrels go up trees.
- [ ] **Placement works.** Anything placeable (skull mounts, pots, bowls, buckets) gets tested by actually placing it. Collision checks must not count the wall it hangs on.
- [ ] **Variety.** Where a real species varies, add colour variants (jellyfish colours, butterfly species, antler colours).

## Textures
- [ ] **Tile well but not too well.** Seamless edges: wavy bark cracks must wrap top to bottom (white oak). No obvious repeating grid,
      and no perfectly uniform columns (shagbark hickory). Preview 4x4 tiled before shipping.
- [ ] **Follow the base-game template.** Planks are built on the Programmer Art / classic oak plank layout, recoloured.
      Spawn eggs are the classic two-tone base-game egg, tinted. When in doubt, start from the vanilla (Programmer Art) asset and recolour it.
- [ ] **Items match their neighbours.** Food and items follow the Programmer Art look (no black 1.2.5 outlines, so beef
      sits well next to rabbit and mutton). Build meat and food icons on a vanilla template cut (venison is the mutton cut),
      draw drumsticks diagonally like vanilla items, and avoid perfectly symmetrical natural shapes (walnut kernel).
- [ ] **A few exotic woods.** Most woods stay natural, but some species (cedar, walnut, cherry, poplar) get richer plank colours for builders.
- [ ] **Leaves have enough transparency.** Fancy-leaf holes, see-through gaps in blossom-only leaves, a dark colour on the transparent pixels for Fast graphics.
- [ ] **Transparent where intended.** Cracks in skulls are see-through (alpha 0), not painted.
- [ ] **Animated textures loop without a seam.** Recorded animations (classic water/lava) must not jump when they
      restart: ping-pong still textures, make flowing ones scroll whole tiles per loop and interpolate.
- [ ] **Particles match the plant.** Dogwood drops white petals, not cherry pink.
- [ ] **UI art is crisp.** Upscale low-res classic art with nearest-neighbour to the size vanilla expects (panorama 256 → 1024) so linear filtering doesn't blur it.

## Trees
- [ ] **No logs poking out of leaves.** Limbs end inside the canopy. Internal support logs only go in blobs tall enough to hide them.
- [ ] **Silhouette matches the species.** Tall trees aren't bare poles with a cotton-swab crown (tulip poplar), and small flowering trees aren't stacked flat discs (dogwood).
      Look at a photo and compare.
- [ ] **Crowns are limbs plus clusters, not balls.** Build crowns like vanilla's fancy oak: limbs reaching out from the trunk, each ending in a
      flat-bottomed leaf cluster, inside a species outline. A smooth ellipsoid on a bare pole reads as a lollipop.
- [ ] **Preview in game.** Place each tree with `/place feature` on flat ground, three of each, before committing. Keep test `fill`s under 32,768 blocks.

## Biomes
- [ ] **Keep feature order consistent.** Inside each generation step, features shared between biomes must appear in
      the same relative order in every biome, or the world won't load ("Feature order cycle"). When building a biome
      from a vanilla one, only add features unique to it, or put shared ones where vanilla has them.
- [ ] **Worldgen code never reads the level far away.** A placement filter or feature can only touch nearby chunks;
      to check the biome some distance off, ask the biome source with the climate sampler, not `level.getBiome`
      (that crashed chunk generation).
- [ ] **Feature order matches vanilla.** Within a biome's decoration step, features shared with vanilla biomes keep vanilla's relative order,
      and nothing is listed twice. Otherwise chunk generation crashes with "feature order cycle" (autumn forest leaf litter).
- [ ] **New biomes get spawns.** Add them to every spawn selector their neighbours use, and to the relevant biome tags (is_forest, is_overworld).

## Sounds
- [ ] **Natural, not robotic.** Bird calls need pitch glides, amplitude envelopes, slight randomness per call, and some noise and harmonics; no pure steady sine beeps.
- [ ] **The right sound for the action.** Swimming must not sound like walking (classic pack maps swim to a quiet splash).

## Blocks and renderers
- [ ] **Open or partial blocks set `noOcclusion()`.** Any block whose model isn't a full opaque cube (frames, pumps,
      engines, pipes) must not hide its neighbours' faces, or you see straight through the world (the screw pump
      against a wall showed the void).
- [ ] **Never move shared model parts in a block entity renderer.** 1.21.11 draws block entities after extracting
      them all, so setting `part.y`/`xRot`/`yScale` in `submit` makes every block draw with the last one's pose
      (engines skipped and idle engines pumped). Put per-block motion into the PoseStack instead.
- [ ] **See-through models need inward faces.** Faces only draw from the front; anything you can see into (pipes
      with windows) needs inward-facing twins of its walls.
- [ ] **Things that join line up.** A spout, arm or socket that meets a pipe sits on the pipe's centre line and
      reaches the block edge (the hand pump spout and the Ender Terminal's back both fell short).
- [ ] **Animations loop and stop cleanly.** No snapping back to zero when something stops (the screw pump).
- [ ] **Very dark fog colours get brightened** to full brightness by the game: use pure black (#000000) for a black
      sky's fog, not a near-black (the moon's sky came out lavender).

## Wiring
- [ ] **Every new critter model is registered.** Add its name to the model-layer list in `RyanCraftClient`. A missing layer makes
      resource loading hang on a black screen (swan). Each new mob also needs an entity type, attributes, renderer, spawn egg, egg colour, loot, lang and spawns.
- [ ] **Spawns in the wild, in the right places.** Every mob has biome spawn entries and a spawn placement rule
      (RyanSpawning: ground and daylight for land animals, water for fish). Test with a creative/survival player, not
      spectator: spectators don't trigger ongoing spawning (fish, water animals), only chunk-generation spawns.
- [ ] **Breedable, with sensible food.** Every animal breeds on something it would really eat (swans: grain and seagrass; frogs: slimeballs, as vanilla).

## Testing
- [ ] **Test scenes are safe.** Keep the test player on solid ground, with no deep water or drops beside them. A player
      that drifts into deep water drowns, and drowning damage looks like a bug in whatever you're testing (thirst).
- [ ] **Time server logic by the world clock** (`level.getGameTime()`), not an entity's tick counter.

## Licensing
- [ ] **Check every borrowed asset's source, file by file.** A permissive code license doesn't cover bundled media. The
      MAtmos sound pack turned out to be Half-Life 2 audio (Valve's), so it can't be used. Only ship CC0/public domain,
      or CC-BY with credits in docs/CREDITS.md; otherwise make our own.

## Release
- [ ] **Bump `mod_version` and add a changelog section** after a round of changes. The Release workflow builds the jar and
      publishes a GitHub Release (tag v<version>) with that section as notes. Never commit jars to the repo.

## Test harness
- Vanilla models often name textures without the namespace ("block/nether_portal", not "minecraft:block/nether_portal"). When renaming a copied vanilla model, replace both spellings, or it quietly keeps vanilla's texture (the Skylands portal showed vanilla purple for weeks).
- `execute if entity @e[...]` counts across every dimension; add `distance=0..` (with `execute in`) to count one dimension.
- If the test player last logged out in another dimension, the overworld chunks aren't loaded when the first function runs: `fill`/`setblock` there silently do nothing. Teleport first, then build after a delay.
- `/summon` with any NBT skips finalizeSpawn, so biome/dimension-picked variants never get chosen; summon without NBT to test them.
- Commands in scheduled functions don't move a spectator camera reliably; teleport the camera in the first (tick-run) function.
- 1.21.11 renamed gamerules to snake_case (`random_tick_speed`, not `randomTickSpeed`). A function with one bad line fails to load at all, and a tick function that calls it fails too, so the test silently never starts. Grep the log for "Failed to load function" in the waiting loop.
- Random-tick speed in the thousands stalls a software-rendered test client; a few hundred ages blocks fast enough.
- A block entity renderer that draws see-through glass with a translucent render type writes depth, so items it draws "inside" vanish behind the glass (submit order and `order(n)` didn't help). Draw such glass cutout and double-sided (`entityCutoutNoCull`) with clear panes and solid glints, the way vanilla glass works.
- Mod chest textures go in `assets/<ns>/textures/entity/chest/`: the chest atlas reads that folder in every namespace, so `Sheets.CHEST_MAPPER.apply(id)` and the `minecraft:chest` special item model both find them, and vanilla's ChestModel draws them, lid and all.
- The test wait loop must not grep for "error:": ALSA prints it at startup and the screenshot catches the loading screen.
- Villager trade screens can be tested headless: put the test player in creative facing a NoAI villager (give it `Xp:1` so it keeps its profession without a job site), then `xdotool windowactivate` the Minecraft window and `xdotool click 3`.
- Item display rotations apply Z first, then Y, then X. Adding 180 on X while negating Z gives the same pose, so it won't flip a held item. To turn a side-view item upright, flip the sign of the Y turn (e.g. [0, 90, z] instead of [0, -90, z]).
- Block model faces only render from the front, so a hollow model (a pot, a tub) needs separate thin inner-wall elements, or you see straight through its walls from inside. Ledges where one box sticks out of another need their own up and down faces too.
