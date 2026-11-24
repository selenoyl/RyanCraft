# RyanCraft changelog

Release jars are on [GitHub Releases](https://github.com/selenoyl/RyanCraft/releases); each version is built and published automatically when `mod_version` is bumped. Plans for later are in [ROADMAP.md](../ROADMAP.md).

## 1.9.1
- **The Abyss**, a new cave layer below the deepslate, down to bedrock at y=−128 (new chunks only; the built-in Deep
  Caves data pack can be switched off for a new world). Five kinds of cavern follow the climate above: Abyssal
  Caverns, Toxic Caves (poisonous sludge under glowing moss), Frozen Caverns, the Sunken Wilds (an underground
  overworld under a bright lumen-moss ceiling) and the Hollow Dark (sculk). Abyssal stone with cobbled, polished and
  brick versions, its own ores and diamond veins, and a lava floor. Vanilla ores stay at their usual heights.
- **The Great Moon Rabbit** gets its own model, drawn from the concept art: half again as big, with a jade crown, buck
  teeth and the Moon Mortar staff. Every move is animated: the overhead strike, a ground slam with a wind-up you can
  jump, a sideways meteor bat, and a summon with the staff's jade top glowing. Below half health comes the eclipse:
  darker fur, blazing eyes and a halo of light. When it dies it drops the staff, loses its crown and topples over.
- **Boats and chest boats** for all fifteen of our woods; door items now match vanilla's size.
- **Clay pots can be dyed**, and fish buckets use the classic bucket.
- **Wildlife**: whales keep to the deep and visibly swim, surfacing now and then; birds and squirrels prefer the
  trees; the zebra gets horse-style eyes and the coyote's eyes are fixed; elk antlers attach properly; tidier
  pegasus wings; every mod mob now has drops; spawn eggs for each mudling kind; black bears a little commoner.
- **Metals**: meteor iron gear is now netherite-strength; cut lead joins cut silver; cleaner ruby and sapphire art,
  with their ores back to the emerald style.
- **Fireflies** come out only once it's properly dark, and can be turned down or off in the config.
- **Skylands**: fall off the islands into the overworld; islands 30% thinner, with fewer flowers and nests; the moon
  portal lights on any night.
- **Fixes**: glass chests no longer flicker at the bottom; white glints back on the new armour and tools; the screw
  pump turns the right way; the Ender Terminal has a scroll bar.
- **A wiki**, with a page for every topic, linked from the front page, plus fresh in-game photos and texture charts.

## 1.9.0
- **The Skylands**, a floating-island dimension: a glowstone portal lit with water, cloud-strata skystone, the sky
  willow, zephyrite ore, tools and armour (boots stop fall damage; the full set glides on sneak), springs and
  waterfalls, zephyr vent updrafts, four sky flowers, and clouds drifting just below the islands. Animals: pegasus
  (white or grey, jumps twice as high and glides; breeds with horses into winged hybrids), cloud sheep, sky rays and
  sky rocs, which nest on high islands and can be raised from an egg and ridden. Its own high-wind ambience. No seasons.
- **The moon**, reached through a jade portal on a full moon: low gravity, no air (Potion of Air, force fields,
  gravity wells), moon rabbits, herbs, lantern moths, dust drifters, crater crabs, lost spacemen, falling meteors and
  jade temples guarded by the Great Moon Rabbit, a boss that drops the jade crown and the Moon Mortar and opens the
  way home. Moon cakes. New moon stone and meteor iron textures drawn from scratch.
- **Metals**: silver, lead, tin, zinc, aluminum and uranium, each with ores, raw metal, ingots, nuggets, blocks and a
  chiseled block of its own. Silver tarnishes through four stages (wax it to keep it shiny). Leaded glass in 17
  colours and two patterns; lead tools and armour; meteor iron gear on par with diamond. Every metal block works in
  a beacon.
- **Gems**: jade, ruby and sapphire, with ores, blocks, and jade and lapis bricks.
- **Regional currency**: desert and savanna villagers trade in rubies, snowy villagers in sapphires, the rest in
  emeralds; the new Money Changer villager (job site: the Coin Scale) swaps one for another, five for four.
- **Mudlings**: little biome soldiers of mud, sand, snow and more, with camps, dye teams, battles and level-ups.
- **Cooking**: a cast-iron cooking pot, over a campfire on a tripod, hung from a crane bar in a fireplace, or set
  right in the fire. Pour in water or milk, add up to six ingredients, and once it comes to the boil it simmers into
  portions of a meal named for its main ingredient (stew, soup, chowder or porridge); the pot shows its colour and
  bubbles. Meals can be set down in bowls, up to four to a block.
- **Campfires burn fuel**, from a furnace-style menu, and go out when it runs dry.
- **Seasonal crops**: tomatoes, peppers, corn, squash, cabbage, onions, beans and rice, each growing only in its own
  seasons outdoors (greenhouses get round it), and dishes from them: roasted corn, baked squash, stuffed peppers,
  cabbage rolls, sandwiches, garden salad and cornbread.
- **Combat**: old combat (no attack cooldown; config `oldCombat`), cannons with six kinds of shot, and muskets.
- **Special chests**: obsidian (blast-proof), diamond (54 slots, see-through) and lead glass.
- **More**: concrete stairs and slabs, shelf mushrooms on tree trunks, new bark patterns, and eight new eastern
  forest biomes placed in-house (TerraBlender is no longer needed).

## 1.8.0
- **The moon's blocks, and a first look at the moon** (no portal yet; operators can visit with
  `/execute in ryancraft:moon run tp @s 0 120 0`):
  - Moon dust (falls like sand), moonstone with cobbled, polished, brick, cracked and chiseled versions, plus
    stairs, slabs and walls.
  - Deep moonstone below about y=32, like deepslate, with its own cobbled, polished and brick blocks.
  - Meteor iron ore (and deep meteor iron ore) in the moon's caves. It glows faintly, so it shows in the dark.
    Smelts from raw meteor iron to ingots, and makes blocks.
  - The moon itself: rolling dusty plains over moonstone, down to bedrock at y=0, under a black sky with the
    stars always out. Caves, wide old lava tubes and ravines run through the stone.
- **Ender Terminal**: the search box is gone. A button in the corner sorts by name or by amount.
- **Engines**: each engine animates on its own. Before, every engine drew with one engine's stroke, so they
  skipped, unpowered engines pumped, and warm-up looked like full speed. Powering one engine no longer runs the
  engine next to it.
- **Pumps**:
  - The screw pump drains a pool for real, BuildCraft-style: farthest and edge sources first, so a pond, lake or
    patch of ocean empties slowly from the outside in. Flowing water alone gives nothing.
  - Pumps only feed a fluid pipe that leads to a tank or other store.
  - The screw stops where it is instead of snapping back, and its casing fills with what it's pumping.
  - No more seeing through the world when a screw pump is against a wall.
  - The hand pump's spout meets a fluid pipe cleanly.
- **Pipes**: you can see the far wall and the creeper plates through a pipe's windows, the item looks right in
  hand, and a pipe at the back of an Ender Terminal reaches it.
- **Trees**:
  - New bark for all fifteen species, built on the classic logs and browner.
  - Planks have more shades.
  - Classic-style planks for acacia, dark oak, mangrove, cherry, pale oak, bamboo, crimson and warped.
  - Our trees no longer grow by deserts, badlands, savannas or jungles (no more sycamores on desert riverbanks).
- `/weather duststorm [duration]`, and `/weather clear` ends a dust storm.
- Pronghorns, deer, squirrels, chipmunks and turkeys no longer bolt comically fast when hit.
- The pronghorn has a more vanilla-style coat.
- Jellyfish only live in warm and lukewarm oceans.
- A placed lava bucket gives off light, and placed buckets' handles meet the rim.
- The classic pack's giant mushroom stems were using the wrong texture.

## 1.7.0
- **The price of an Ender Terminal**: end portal frames come from a stronghold, and taking one costs that stronghold.
  - Mine a frame with a Silk Touch diamond (or netherite) pickaxe and it drops itself. If it had an Eye of Ender
    in it, the eye drops too, separately.
  - Breaking any frame clears the portal it was part of.
  - That stronghold is spent: Eyes of Ender skip it and lead to the next nearest one.
  - `/stronghold` shows operators where the eyes lead.
  - (Frames never dropped before: vanilla gives them no loot table, so their drops are now handled in code.)
- **Dust storms are their own weather**: no longer tied to thunderstorms. They have their own clock, usually 5-10
  days clear and then 5-12 minutes of storm, and they only happen over deserts and badlands.
  `/duststorm [start|stop]` for operators.
- **Drab winter leaves**: our trees can't drop their leaves, so in late autumn and winter they take on a dull
  grey-brown tint. The cedar stays evergreen.
- No bundle holds more than 4 creatures: butterflies now fill a bundle at 4, like the other critters.
- The zebra is in the land field guide, and the Ender Terminal (block and screen) is on the front page.

## 1.6.0
- **Dust storms**: thunderstorms over deserts and badlands become dust storms, with:
  - sandy fog (rust-red over the badlands) that closes in to about 40 blocks
  - fine dust blowing past
  - a howling wind
  - faster thirst for anyone caught outside
- The Ender Terminal makes a soft mechanical clunk when you deposit items, instead of a teleport sound. The items
  travel the pipes.
- Front page: new shots of the pipes and engines, a dust storm, and a clean four-season sequence.
- Roadmap: cannon shot types (round shot, grapeshot, chain shot, canister, heated shot, explosive shell) and a
  brainstorm of vanilla-plus engineering machines.

## 1.5.1
- **Steel decoration blocks**: Cut Steel (with stairs and slab), Steel Plating, Chiseled Steel, a see-through Steel
  Grate, Steel Bars, and a redstone-operated Steel Door and Trapdoor. Most can also be made on the stonecutter
  from a Block of Steel.

## 1.5.0
- **Steel**: point a running engine at a blast furnace and it works like a bellows. While it's stoked and burning
  fuel, iron ingots turn into steel ingots (20 seconds each). Steel Ingot and Block of Steel.
- **Thirst**:
  - Drains about half as fast.
  - Drinks and juicy foods give hydration, thirst's version of saturation, used up before the bar drops.
  - More foods count: honey, beetroot, pumpkin pie and cherry pie.
  - Water bottles go down in three sips, and the bottle's texture and tooltip show how much is left.
- New charts on the front page:
  - the fifteen trees in game
  - field guides for land, air and water
  - the twenty butterflies
- Mudling concept art added to the roadmap.

## 1.4.0
- **Thirst**: a row of water droplets over the hunger bar. It drains a little faster than hunger, faster when you
  sprint, under the sun in hot biomes (deserts, badlands, savanna) and in the Nether.
  - **Drinking**: use water with an empty hand to sip (+1 droplet), or drink a water bottle (+3.5 droplets). Milk,
    stews, honey, melon, berries, apples, carrots and cherries quench a little too.
  - **Running low**: at 3 droplets or fewer you can't sprint. With none left you stop healing naturally and take
    damage, like starving (Easy stops at 5 hearts, Normal at half a heart, Hard can kill).
  - **Settings**: it refills by itself on Peaceful and doesn't drain in Creative. `/thirst` shows yours,
    `/thirst set <0-20>` sets it (operators), and `thirst` in `config/ryancraft.json` turns it off.

## 1.3.2
- **True-to-life fish sizes**: most catches fall in each species' typical range (a bluegill is 4-9 in, a
  largemouth bass 10-20 in, a muskie 28-44 in). About 1 in 50 is a trophy, up to the real-world record.
  About 1 in 1000 is a **Legendary** fish, up to 15% past the record.
- Releases are published on GitHub (built automatically for each version); the `releases/` folder is gone.

## 1.3.1
- **Fishing loop**: every rod catch is called out with its length ("Largemouth Bass - 18.5 in"), with a fanfare
  for trophies (roughly the top 5% of a species) and a chat note for a new personal best.
- **Angler's Logbook** (book + any fish): your biggest fish of each species, trophies starred.
- **Fish Plaque** (3 wooden slabs over slab, item frame, slab; makes 2): hang it on a wall and use a fish on it
  to mount it, length engraved underneath (gold for trophies). Use it empty-handed to take the fish back.
- Rivers, lakes and swamps give our freshwater fish (plus salmon); oceans give cod, salmon and pufferfish.
  Catfish and walleye bite more after dark.

## 1.3.0
- **Seasons**, Serene Seasons style: a 12-part year (early, mid and late spring, summer, autumn and winter, 8 days
  each, starting in spring). Temperate biomes snow in winter and ponds freeze; in spring the snow melts a layer at a
  time. Grass and foliage colours shift. All 14 broadleaf species turn to autumn colours and back in spring (redbud
  and dogwood back to blossom). Crops stall in winter unless they're under a roof.
- **Season Calendar** (3 paper + a clock), `/season` and `/season set <season> [early|mid|late]`.
- The Autumn Forest biome no longer generates; seasons replace it.
- **Trees rebuilt**: crowns of limbs ending in flat-bottomed leaf clusters (vanilla fancy-oak style), with an
  outline per species, instead of smooth balls on poles.
- **Tanks and pumps**: the glass tank (16 buckets, stacks seamlessly, fluid falls into the tank below), the
  iron-hooped vat (8 buckets, collects rain, fills bottles), the Archimedes screw pump (engine-driven, spinning
  screw, intake tube; infinite water stays infinite, lava drains) and the hand pump (draws groundwater from up to 16
  blocks down and pours from its spout into an adjacent tank or pipe; by hand or engine; fills a held bucket).
- Pipes get clean corners and junctions; engines rebuilt on BuildCraft's layout in piston materials.
- Every RyanCraft mob has a spawn placement rule (ground and daylight on land, water for fish and ocean animals).
- Fish spawn weights cut to vanilla-like levels.
- Recipe audit: every craftable item has a recipe.
- Moobloom: dandelions sit in the hide; milk it with a bowl for suspicious stew.
- Classic water and lava animations loop without a visible jump.

## 1.2.1
- See-through pipes, piston-styled engines that warm up, the Ender Pipe, an end-portal-frame Ender Terminal, and a
  Stirling engine that runs on fuel alone and glows while lit.

## 1.2.0
- **Pipes, engines, pump, tank and the Ender Terminal** (Tekkit style, items visibly flowing).
- Autumn Forest biome (since replaced by seasons in 1.3.0).
- A first pass at a fishing loop with new game fish.
- Crows sit on shoulders and when told to stay; bullfrog and rattlesnake heads turn; every animal breeds on a
  sensible food.
- Canada geese fight back again.

## 1.1.0
- Reindeer; elk and black-tailed deer grow staged antler racks (with rare non-typical racks).
- Moobloom, swans (rare black swan), five frog and toad species.
- The 1.2.5 rose is back; classic deep-blue water and bright lava; crisp classic title panorama.
- Planks, leaves and spawn eggs follow the base-game templates; exotic planks for cedar, walnut, cherry and poplar.
- Squirrels climb trees, jellyfish stay deep and come in six colours, spawn eggs work on cages.
- Many fixes: no more z-fighting heads, attached limbs, reshaped dogwood and tulip poplar, seamless bark.

## 1.0.0
- First release: the classic 1.2.5 pack inside the mod, MAtmos-style ambience, Kentucky wildlife, deer antlers and
  skull mounts, fifteen eastern trees, the Beta biomes and the classic tweaks.
