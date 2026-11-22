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
      Runners run at believable speeds (deer fled comically fast).
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
- [ ] **A few exotic woods.** Most woods stay natural, but some species (cedar, walnut, cherry, poplar) get richer plank colours for builders.
- [ ] **Leaves have enough transparency.** Fancy-leaf holes, see-through gaps in blossom-only leaves, a dark colour on the transparent pixels for Fast graphics.
- [ ] **Transparent where intended.** Cracks in skulls are see-through (alpha 0), not painted.
- [ ] **Particles match the plant.** Dogwood drops white petals, not cherry pink.
- [ ] **UI art is crisp.** Upscale low-res classic art with nearest-neighbour to the size vanilla expects (panorama 256 → 1024) so linear filtering doesn't blur it.

## Trees
- [ ] **No logs poking out of leaves.** Limbs end inside the canopy. Internal support logs only go in blobs tall enough to hide them.
- [ ] **Silhouette matches the species.** Tall trees aren't bare poles with a cotton-swab crown (tulip poplar), and small flowering trees aren't stacked flat discs (dogwood).
      Look at a photo and compare.
- [ ] **Preview in game.** Place each tree with `/place feature` on flat ground, three of each, before committing. Keep test `fill`s under 32,768 blocks.

## Sounds
- [ ] **Natural, not robotic.** Bird calls need pitch glides, amplitude envelopes, slight randomness per call, and some noise and harmonics; no pure steady sine beeps.
- [ ] **The right sound for the action.** Swimming must not sound like walking (classic pack maps swim to a quiet splash).

## Wiring
- [ ] **Every new critter model is registered.** Add its name to the model-layer list in `RyanCraftClient`. A missing layer makes
      resource loading hang on a black screen (swan). Each new mob also needs an entity type, attributes, renderer, spawn egg, egg colour, loot, lang and spawns.
- [ ] **Breedable, with sensible food.** Every animal breeds on something it would really eat (swans: grain and seagrass; frogs: slimeballs, as vanilla).

## Release
- [ ] **Rebuild the release jar** in `releases/` after a round of changes, so the downloadable jar matches the code.
