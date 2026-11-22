# RyanCraft roadmap

Ideas queued for later, roughly grouped. Struck-through items are built; see [docs/CHANGELOG.md](docs/CHANGELOG.md).

## Next up
- ~~**Seasons**~~ (built in 1.3.0). Still to do: antler shedding in late winter.
- ~~**Thirst bar**~~ (built in 1.4.0): drink from water or bottles; some foods give a little water.
- **Deep layer below deepslate** (Alex's Caves style): regular, toxic, frozen, underground-overworld and deep-dark caverns.
- ~~**Fishing loop**~~ (built): catch-and-measure fish, trophies, personal bests in the Angler's Logbook, fish plaques.
- **Squirrels that really climb**: run up trunks, cling sideways and hang upside down on bark.
- ~~**Old-school spawn eggs**~~ (built): redo the egg icons to look like the classic two-tone spotted eggs, cleaner and calmer.
- **Chipmunk eyes**: they're tiny, so the eyes may need to move to the sides of the head; check in game.

## Wildlife
**Deer family**
- ~~**Elk** and **moose**~~ (built)
- ~~**Black-tailed deer** (western coastal forests).~~ (built)
- ~~**Pronghorn**~~ (built)

**Predators and big game**
- ~~**Grizzly bear**~~ (built)
- **Elephants**, including big **war elephants** you can saddle and armour.
- ~~**Bobcat**~~ (built)
- ~~**Alligator** (swamps)~~ (built)

**Birds**
- ~~**Bald eagle** and **golden eagle**~~ (built)
- ~~**Hawks** (the ambience already has hawk calls to hook into)~~ (built)
- ~~**Vultures** circling high over open country~~ (built)

**Ocean**
- ~~**Whales**~~ (built), each with its own behaviour: **orcas** (hunt in pods), **blue whales** (huge, slow, surface to breathe and spout).
- ~~**Sharks**~~ (built)
- ~~**Manta rays**~~ (built)
- **Schools of ocean fish** that move together.

~~**Batch 2 prototypes**~~ (all built; docs/prototypes_2.png): zebra (a horse variant), American bison, cougar, river otter, groundhog,
porcupine, eastern box turtle, timber rattlesnake, bullfrog, great horned owl, great blue heron, Canada goose,
ruby-throated hummingbird, pileated woodpecker, northern bobwhite, jellyfish.

**Small life**
- ~~**Butterflies**~~ (built): many species like the tropical fish (wing patterns x colours); 20 eastern species prototyped in docs/butterflies.png
- ~~**Fireflies**~~ (built): drifting over plains and through the woods on summer nights (the game only has the firefly bush).
- ~~**Chipmunks**~~ (built)

**Canines**
- ~~**Coyotes**~~ (built): a smaller, untameable wolf. Like the black bear, mostly leaves you alone unless you get close;
  travels in loose packs the way vanilla wolves do.

**Songbirds**
- ~~**Mourning doves**~~ (built), with their iconic soft cooing call (in the songbird family and the ambience).

## Hunting
- **Hunting gear**: game calls (turkey and deer calls that draw animals in), a tree stand, and the like.
- **No pelt system for now**: vanilla mobs stay as they are, and coyotes drop no leather. Deer give buckskin
  leather like a cow. Wolf and coyote pelts might come later, but it's unlikely.
- **Taxidermy** (later): stuff and mount animals; right-click a mount to cycle through poses, the way the
  copper golem statue switches poses.

## Cold and warm clothing
- **Cold in snowy biomes**: you get cold out in the snow without warm clothing.
- **Coats and capes** made from leather and wool. The trade-off: they take the armor slots, so to stay warm in the
  cold you take your armor off (no extra slot).

## Mudlings: biome soldiers you collect and battle
Our own take on the old Clay Soldiers idea, and the main "fight your friends" game.

**Finding them**
- Rare little squads of **mudlings** live in the wild, each biome with its own kind, made from that biome's stuff.
- **Rarity: relatively rare, never teeming, never unobtainable.** Mudlings only live at their camps (they don't
  spawn freely like animals). Aim for roughly one camp every 400-600 blocks of a given biome, generated as a small
  world structure, with 3-6 mudlings per camp. A camp doesn't refill once it's emptied, so filling a bundle of 16
  means finding a few camps. Rare colour variants are about 1 in 20 mudlings.
- They patrol a tiny camp (a pebble fort, a twig lean-to) and scatter if you charge in, so sneaking and camo help.
- Rare colour variants (gilded, mossy, charred) to collect.

**Collecting**
- Scoop them up with a **vanilla bundle**: right-click a mudling and it goes in. A bundle holds **up to 16 mudlings**
  (mudling items stack to 16, which makes 16 fill a bundle exactly). One full bundle = one squad of one type.
- Empty the bundle to deploy them; they hop out and form up.

**Biome mudlings, their style, and what they crumble into**

| Biome | Mudling | Style | Breaks down into |
|---|---|---|---|
| Plains | Sod soldier | All-rounder | Dirt, seeds, a flower |
| Forest | Twig ranger | Quick, light hits | Sticks, saplings, leaves |
| Taiga | Bark warden | Sturdy | Spruce bark, sweet berries |
| Swamp | Mud slogger | Slow, slows enemies | Mud, clay, lily pad |
| Desert | Sandstone legionary | Tough, shield wall | Sand, sandstone, dead bush |
| Badlands | Terracotta brute | Heavy hitter | Coloured terracotta |
| Snowy | Snowpack scout | Throws snowballs | Snow, ice, packed ice |
| Jungle | Vine skirmisher | Climbs, ambushes | Vines, cocoa beans, bamboo |
| Mountains | Gravel grenadier | Knocks foes back | Gravel, flint, stone |
| Ocean/beach | Shell guard | Defensive | Sand, shells, kelp |
| Caves | Pebble miner | Digs in | Cobblestone, coal, rare ore nugget |
| Nether | Cinder imp | Sets things alight | Netherrack, quartz, blaze powder |
| Sky dimension | Cloud kite | Flies | Sky materials |

**Battles**
- Set two armies down (on a battle table or anywhere) and they fight on their own while you watch.
- **Type matchups**: simple counters (mud beats sand, sand beats snow, snow beats twig, and so on).
- **Gear**: drop items on the table before a fight: leather for armour, a feather to glide, deer antler for a
  lance, a coonskin cap for morale.
- **Losers are toast**: they crumble into their biome's materials and are gone for good, so you go find more.
- **Winners level up**: survivors get tougher with each fight they win.

## Creature companions (maybe later)
If we still want them alongside mudlings: rare biome creatures you raise from an egg or befriend by feeding, that
follow you or live at a den, help out day to day (ore-sniffing, crop growth, fetching fish, light at night) and grow
through visible stages. The sparring side is covered by mudlings.

## Trees
**Built:** all 15 species, generating in the world and growing from saplings, each with log, wood, stripped log and
wood, planks, slab, stairs, fence, gate, door, trapdoor, button, pressure plate, leaves and sapling. Every tree rolls
its own height, limbs, crown size and occasional forks, so no two match, and crowns are built from limbs and leaf
clusters like vanilla's fancy oak. Still to do:
- **Signs, hanging signs and boats** for each species (these need their own wood type, entity and textures).
- ~~**Fall colours**~~ (built with seasons; all 14 broadleaf species).
- **Bare winter branches** (optional): leaves drop in winter and come back in spring.
- A rougher pass on the plate-style bark (sugar maple, redbud, buckeye).

**More trees (ideas)**
- **Redwood**: a 2x2-trunk giant like the jungle tree, but taller.
- **Olive** and **cypress**. (Sakura is covered by vanilla's cherry tree.)

**Berry bushes** out in the world (blackberry, raspberry, blueberry, elderberry, pawpaw...).

## Storage and inventory (ideas)
- **Backpack slot**: an extra inventory slot for a backpack.
- ~~**Clay pots, placeable bowls and buckets**~~ (built). Next: fired clay bowls and crocks, food spoilage.
- **Bowls**: a little smaller, and up to 4 per block (Vintage Story style).
- **Buckets**: can be placed diagonally (rotated 45 degrees).
- **Wooden bucket**: crafted from 3 planks and 1 iron ingot; holds water/milk and looks good set down in a house.
- **Special chests**: diamond, obsidian and lead glass.

## Blocks and backports
- **Concrete slabs and stairs**, backported from newer versions.
- **Shelf mushrooms** (the newer shelf fungus that grows on tree trunks).
- **Ruby and sapphire blocks**, alongside the emerald block.

## Villagers
- **Trade rework around emeralds, rubies and sapphires**: give the new gems a real use as currency tiers, each
  with a storage block.

## Food
- **Our own Farmer's Delight**: cooking, with plated foods you can set down as 3D meals.
- **More crops**: tomatoes, onions, cabbage, corn, peppers, rice, beans, squash and the like, growing with the
  seasons (some only in spring or summer).
- **More foods** to go with them: stews, soups, pies, roasts, sandwiches, plus dishes built on our game (venison,
  turkey, duck, fish fillets) and orchard fruit (cherries, walnuts).

## Weather
- **Dust storms**: rare, in deserts and badlands.

## Combat
Technology tops out around the early age of gunpowder: nothing past a muzzle-loader.
- **Old combat**: no attack cooldown, as in 1.8 and before.
- **Cannons**: fire lead (or iron) cannonballs.
- **Muskets**: very rudimentary guns, slow to load (powder, ball, ramrod), loud and smoky, inaccurate past close
  range. They fire lead balls.
- **Lead finally gets a use**: cast lead into musket balls and cannonballs.

## Mobs: vanilla variants
- **Creeper variants**: biome-specific creepers, with the original still by far the most common.

## Power and automation (vanilla-friendly)
- Light-touch power generation and automation, not a full industrial tech tree. Redstone already covers a lot, so
  this would fill the gaps (simple generators, machines that fit the vanilla look).
- ~~**Pipes, engines and the Ender Terminal**~~ (built in 1.2.0). ~~**Tanks and pumps**~~ (glass tank, vat, screw
  pump and hand pump; built in 1.3.0).
- **Ender Terminal GUI redesign**.
- **Force field generator**, fuelled by nether stars.
- **Cage upgrades**.

## Vehicles (primitive, Flan's-style)
- **A little biplane**: basically a boat with wings, fuelled with coal, with that primitive early-mod look.
- Other simple vehicles in the same style.

## A sky dimension (our own, not the Aether)
A dimension of floating islands above the clouds, in the spirit of the old Aether mod but with its own blocks,
creatures and way in (no glowstone portal, holystone, skyroot, moas or aerwhales).
- **Getting there**: a portal of our own, e.g. a frame that has to be charged by a lightning strike, or riding a
  strong updraft from a mountain peak.
- **The land**: floating islands of grassy meadow, cloud blocks you can stand on (some bouncy, some that drop you),
  updraft columns that carry you between islands, waterfalls pouring off the edges into nothing.
- **Great winged creatures**: big gliding sky rays that drift in flocks, huge soaring birds with nests on the high
  islands, maybe a rideable one you have to raise from an egg.
- **Its own materials**: sky-only wood and stone, a light ore for gear suited to falling and gliding.
- **Ambience**: high winds, the creak of floating rock, distant calls of the big birds.

## Ores and the world underground (very Tekkit)
- **Salt**, found in **salt caves**
- **Aluminum, tin, zinc, lead, silver, uranium**
- **Silver and lead ages**: tool and gear tiers built on the new metals.
- **Ruby and sapphire**

## NPCs beyond villagers
Built around **places you find** rather than a populated world: camps generate as their own structures, with
their people living there, so they work in any normal world.
- **Bandit camps**: hostile, silent (no chat), with tents, a campfire, loot to take. You fight your way in.
- **Tribal camps**: their own settlements and people.
- **Wanderers**: now and then a couple of these NPCs walking around in a village, so the world feels less empty.
- If tribes are based on real Native American peoples, they should be done with care and respect: neutral or
  friendly people with their own camps and trade, not enemies or set dressing. (Could also be original,
  fictional groups instead.)
