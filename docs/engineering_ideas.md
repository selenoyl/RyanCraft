# Engineering ideas

A brainstorm of new engineering blocks and items for RyanCraft. Nothing here is built or promised; it's a pool to pick
from. It avoids what already exists or is planned (pipes, engines, tanks, pumps, the Ender Terminal, steel, cannons,
muskets, lead shot, the force field generator, special chests, cages, cooking).

## Design principles

**The tech ceiling.** Muscle, wind, water, weight, springs, fire, plus vanilla's own "magic tech"
(ender pearls, shulkers, end rods, sculk, copper, the crafter, trial chambers). Nothing electric, no screens, no
computers, no fuel more advanced than coal and blaze powder. If a 1700s millwright or a vanilla End city couldn't
plausibly have it, it's out.

**What makes an idea fit:**
- **It plugs into what we have.** The common currency is the *engine pulse*: an engine pushes one stroke into the
  block it faces. New power sources should make pulses too, and new machines should take them. Items go in and out by
  pipe or hopper; fluids by fluid pipe; control is plain redstone.
- **You can see it work.** Gears turn, sails spin, arms swing, items ride. A machine that just sits there with a GUI is
  a worse idea than one you can watch.
- **Vanilla materials, vanilla look.** Planks, logs, cobble, iron, copper, our steel, wool, string, leather. Textures in
  the 16x16 vanilla style, and every block should look fine in a plain survival base.
- **Small, not a tech tree.** Each block does one clear job. Redstone already covers logic, so we don't add logic gates.
- **Copper ages.** Copper parts should oxidise and be waxable, like vanilla copper, and ideally oxidation should matter
  (slower, quieter, or just a look you can pick).

## Ideas

### Power from nature: wind, water and animals

**1. Waterwheel**
- *Pitch:* a big wooden wheel that turns in flowing water and gives steady, slow engine pulses for free.
- *Play:* a 3x3 (or 5x5) wheel placed beside its hub block; flowing water on the paddles turns it. The hub's axle face
  pulses like a redstone engine (slow but never needs fuel). Faster water (more flowing blocks under the wheel) means a
  shorter period. Stops if the water freezes in winter, which ties into seasons.
- *Recipe:* hub from logs and an iron ingot; paddles from planks and sticks, placed in a ring.
- *Why vanilla:* it's just wood and water, and vanilla already has flowing-water mechanics and moving parts to show it off.

**2. Windmill sails**
- *Pitch:* sails on a hub that spin faster the higher and more open they are.
- *Play:* a hub with four wool-and-stick sail arms. Pulse rate scales with height (reusing the altitude wind we
  already have in the ambience) and drops if blocks crowd the sails. Gets a boost in thunderstorms. Wool colour
  is cosmetic.
- *Recipe:* hub from logs and iron; each sail from sticks, string and any wool.
- *Why vanilla:* wool and sticks, and it rewards the same "build a tower" instinct vanilla players already have.

**3. Treadwheel**
- *Pitch:* a big wooden wheel that a mob walks in to make power.
- *Play:* lead a cow, pig, horse, villager or yourself into the wheel; walking turns it and pulses the axle. Stronger
  mobs (horses, our oxen-sized bison) pulse faster. Animals need feeding now and then or they slow down; feeding through
  a hopper keeps it going.
- *Recipe:* planks, logs, a lead.
- *Why vanilla:* it uses leads, animal feeding and mob pathing, all things the game already has.

**4. Axle and gearbox**
- *Pitch:* wooden axles and a gearbox that carry engine pulses around corners and over distance.
- *Play:* axles carry pulses in a straight line (up to 16 blocks, each step losing nothing but turning visibly). A
  gearbox splits or turns the pulse 90 degrees, or steps it up (half the strength, twice as often) or down (twice the
  strength, half as often), set with the wrench. Lets a waterwheel at the river power a blast furnace up the hill.
- *Recipe:* axle from stripped logs; gearbox from planks, iron nuggets and a copper ingot (copper gears). Steel gearbox
  for long runs.
- *Why vanilla:* it's the missing piece between "power source" and "machine", in wood and copper.

**5. Clockwork spring**
- *Pitch:* a wind-up spring that stores pulses and lets them out later.
- *Play:* feed it pulses (from any engine, wheel or treadwheel) and the spring winds up, shown by a visible coil. When
  redstone tells it to, it unwinds, delivering the stored pulses in a burst. Up to a few hundred pulses stored. You can
  also wind it by hand by right-clicking (slowly). A redstone comparator reads how wound it is.
- *Recipe:* steel ingots, an iron block, a copper ingot.
- *Why vanilla:* it's a battery without electricity, and the comparator readout works like a composter or a chest.

### Clockwork and logistics

**6. Mechanical sorter (bin sorter)**
- *Pitch:* a wooden hopper-like box with an item frame on each side that drops matching items out that side.
- *Play:* items piped or hoppered in exit the face whose item frame shows that item, otherwise straight through. Each
  pulse from an engine or axle speeds it up; unpowered it works at hopper speed.
- *Recipe:* chest, hopper, planks, a comparator.
- *Why vanilla:* item frames as filters are already how players label things, so it reads instantly.

**7. Escapement clock**
- *Pitch:* a grandfather clock that gives a redstone pulse at set times of day.
- *Play:* two blocks tall with a swinging pendulum. Set an hour with the wrench (or right-click to cycle dawn, noon,
  dusk, midnight) and it pulses redstone at that time. Also chimes. Doubles as a decorative block that shows the time
  on its face, like the vanilla clock item.
- *Recipe:* a vanilla clock, planks, gold nuggets, glass.
- *Why vanilla:* it's the clock item turned into a block, and a daylight sensor's big brother.

**8. Copper bell-crank relay**
- *Pitch:* copper arms that pass a redstone pulse along a long way without dust, and age as they do.
- *Play:* a thin copper rod on a pivot. Push a pulse in one end and it tips the next crank, and so on, with a click you
  can hear down the line. Oxidised cranks are a tick slower each; waxed cranks keep their speed. Lets players run
  signals through walls and ceilings in old-mansion style (like servants' bell pulls).
- *Recipe:* copper ingots and a lightning rod.
- *Why vanilla:* it's a mechanical cousin of the copper bulb and the lightning rod, and uses oxidation as a feature.

**9. Steel crafter press**
- *Pitch:* a vanilla crafter in a steel frame that crafts whenever an engine pulses it.
- *Play:* takes the crafter's slots and recipe, but each engine pulse fires one craft and pushes the result into an
  adjacent pipe. Faster engines craft faster, so this is where the Stirling engine's speed pays off. Can also compress
  (nine nuggets into an ingot) in bulk.
- *Recipe:* a crafter surrounded by steel ingots and a piston.
- *Why vanilla:* it's literally the 1.21 crafter, just driven by our power instead of redstone ticks.

### Ender and shulker contraptions

**10. Shulker conveyor (lid lift)**
- *Pitch:* stacked shulker-shell blocks that lift items straight up like the shulker's own levitation.
- *Play:* a column of these blocks lifts dropped items (and gently, players and mobs) upwards, a block per tick, with
  faint levitation particles. Stops at a solid block or an inventory, which it inserts into. Redstone turns it off.
  A cheap, magic alternative to a vertical pipe for loose items.
- *Recipe:* a shulker shell, a popped chorus fruit, purpur blocks.
- *Why vanilla:* it's shulker levitation, harnessed, in purpur, the same way an End city would build an elevator.

**11. Ender relay pipe pair**
- *Pitch:* two linked pipe ends that teleport items between them, like a cheap one-way ender chest.
- *Play:* craft two relays at once; they're linked. Items piped into one pop out of the other within the same
  dimension, up to a range set by how many ender pearls went in (or unlimited with an eye of ender). Each item has a
  small chance to "lose a pearl" and need refuelling, so it's a cost, not free logistics. Shows ender particles.
- *Recipe:* ender pipes, ender pearls, an end rod.
- *Why vanilla:* pearls teleport things; this just teleports items.

**12. Chorus graft (living conduit)**
- *Pitch:* a chorus-plant block that grows along a path and carries items through its stalks.
- *Play:* plant on end stone or our "End soil", then guide its growth by placing end rods as stakes. Items put into
  the root ride the stalks to the tip, slower than a pipe but self-extending. Chorus flowers on the tip pop items out.
  Break any segment and the rest above drops, like real chorus.
- *Recipe:* chorus flower, ender pearl, end stone.
- *Why vanilla:* chorus already grows and teleports you when eaten; this is the plant used as plumbing.

**13. Sculk listening post**
- *Pitch:* a calibrated sculk sensor in a frame that listens over a long range and remembers what it heard.
- *Play:* tuned with the wrench to one vibration type (footsteps, chest opening, explosions, pipe items arriving). It
  emits redstone when that type happens within 32 blocks. Pairs with cannons as an early-warning system, or with the
  Ender Terminal to sound a bell when something arrives.
- *Recipe:* calibrated sculk sensor, an echo shard, steel ingots, a spyglass.
- *Why vanilla:* it's the calibrated sculk sensor with a longer ear, so it plays by rules players already know.

**14. Vault key cutter**
- *Pitch:* a bench that cuts your own "trial keys", so you can lock a block to a key.
- *Play:* put an iron or steel ingot and a name tag in, get a key with a name. Lock a special chest, a door or an
  Ender Terminal by using the key on it; only that key (or a copy cut from it) opens it. Copies need the original.
  Doors open with an iron door sound.
- *Recipe:* a grindstone, iron ingots, a trial key.
- *Why vanilla:* trial keys and vaults already exist; this gives players the same idea for their own base.

**15. End rod beam lantern**
- *Pitch:* an end rod in a steel housing that throws a long straight beam of light and ender particles.
- *Play:* lights blocks along a line up to 24 blocks (placing invisible light sources), turnable with a wrench. When
  struck by a redstone pulse it flashes, so it can signal between bases at night. Pairs with defences as a searchlight.
- *Recipe:* end rod, steel ingots, glass, a shulker shell.
- *Why vanilla:* end rods already glow and drift particles; this just aims one.

### Building and lifting

**16. Treadwheel crane**
- *Pitch:* a wooden jib crane that lifts and swings whole blocks.
- *Play:* place the crane base and a jib of beams; a chain hangs from the tip. Click a block with the hook to pick it
  up (as a falling-block entity), then turn and lower it with controls on the base, or drive it from a treadwheel or
  engine. Lifts heavy blocks like anvils, bells and tanks into place.
- *Recipe:* logs, chains, an iron block for the hook, a lead.
- *Why vanilla:* chains, anvils and falling blocks are all vanilla, and medieval cathedral builders did exactly this.

**17. Counterweight lift**
- *Pitch:* a two-shaft elevator balanced with a weight, no power needed to go down.
- *Play:* a platform on chains with a counterweight on the other side. Load the counterweight with stone (more weight
  means faster rise). Ride up by pulling a lever; going down lifts the weight back up. An engine or treadwheel can winch
  it if the weight's too light.
- *Recipe:* chain, pulleys from planks and iron nuggets, a smooth stone slab platform.
- *Why vanilla:* chains and weight; it feels like a mineshaft lift out of a ruined mineshaft.

**18. Pile driver**
- *Pitch:* a tall wooden frame that drops a steel weight to break blocks straight down.
- *Play:* each engine pulse lifts the weight and drops it, breaking one block below it per hit and moving down, like a
  slow vertical quarry one column wide. Drops go into an adjacent chest or pipe. Can't break obsidian or bedrock.
- *Recipe:* logs, chains, a steel block, a piston.
- *Why vanilla:* it's a heavy anvil on a rope, which vanilla anvils already do when they fall.

### Farming machines

**19. Bellows**
- *Pitch:* a leather bellows that speeds up any furnace, smoker or blast furnace it blows into.
- *Play:* pump it by hand (right-click) or let an engine pulse it. Each puff makes the furnace cook faster for a
  moment and kicks up flame particles. Works on campfires too (cooks food faster) and can relight a doused campfire.
  A cheap, early-game version of what our engines do to blast furnaces.
- *Recipe:* planks, leather, an iron nugget.
- *Why vanilla:* it's leather and wood, and the "pump it and see the fire flare" moment is very vanilla.

**20. Millstone**
- *Pitch:* a stone grinding wheel that turns wheat into flour and bones into more bonemeal.
- *Play:* turn by hand or with an engine, waterwheel or treadwheel through an axle. Grinds wheat to flour (for our
  Farmer's Delight baking), bones to more bonemeal than crafting, cobble to gravel, gravel to sand, and sugar cane to
  more sugar. Output drops into a pipe or chest below.
- *Recipe:* two smooth stone, a grindstone, a stick.
- *Why vanilla:* it's the grindstone's bigger sibling, and "grind cobble into sand" has always been the dream.

**21. Seed drill**
- *Pitch:* a horse-drawn (or lead-drawn) cart that plants seeds as it moves.
- *Play:* hitch it to a horse, donkey or mule with a lead and ride over farmland. It plants whatever seeds are in its
  hopper slot and tills the dirt ahead if fitted with a hoe. A harvest rake attachment cuts ripe crops and fills a chest.
- *Recipe:* minecart-style body of planks, a hopper, iron hoes.
- *Why vanilla:* it uses leads and horses, and it's the farm equivalent of the boat-with-a-chest.

**22. Copper sprinkler**
- *Pitch:* a copper head on a fluid pipe that waters crops around it.
- *Play:* connect a fluid pipe carrying water. While fed, it sprays a 9x9 circle, keeping farmland moist and nudging
  crop growth slightly. Puts out fires. Oxidised heads spray a smaller circle; waxed keeps full range. Also melts snow
  off crops in winter, which helps with seasons.
- *Recipe:* copper ingots, a dispenser.
- *Why vanilla:* copper is already a "water and weather" metal in vanilla, and it hooks straight into our fluid pipes.

### Defence that pairs with cannons

**23. Gunpowder mill (incorporating mill)**
- *Pitch:* a heavy stone-wheel mill that makes gunpowder more efficiently from charcoal, saltpetre and bonemeal.
- *Play:* powered by axle, engine or wheel. Takes charcoal, sugar or bonemeal, and (when salt arrives) saltpetre, and
  turns out gunpowder. Gives the cannon and musket supply chain a real production step instead of only creeper farms.
  A small chance to "flash" (a harmless puff of smoke and burnt powder) if run unattended without a water bucket nearby.
- *Recipe:* two smooth stone, a grindstone, steel ingots.
- *Why vanilla:* gunpowder is a vanilla item, and this just gives it a crafting route that fits the era.

**24. Ballista and portcullis**
- *Pitch:* a winch-loaded giant crossbow for walls, and a steel portcullis that drops on a redstone signal.
- *Play:* the ballista fires a bolt (an arrow with a flint-and-steel head) far and hard, loaded by turning a crank or by
  one engine pulse. Rotated by hand. The portcullis is a multi-block steel gate that slides down when powered, crushing
  nothing but blocking all mobs; raised again with a winch. Pairs naturally with cannons on a castle wall.
- *Recipe:* ballista from logs, a crossbow, string, steel; portcullis from steel bars and chains.
- *Why vanilla:* the crossbow and iron bars are vanilla; these are just bigger, castle-sized versions.

**25. Wind ward**
- *Pitch:* a breeze-rod and copper "ward" that pushes mobs back from a doorway like a wind charge.
- *Play:* when a mob crosses its line (detected by a sculk sensor input or tripwire), it gusts a wind charge outward,
  knocking the mob back a few blocks. Players can pass if sneaking. Uses one breeze rod per 64 gusts.
- *Recipe:* breeze rod, copper block, a dispenser.
- *Why vanilla:* wind charges already do this; it's a turret built from trial chamber loot.

## Best first five

1. **Waterwheel.** The most nostalgic power source of all, free and visible, and it just emits the engine pulse we
   already have, so every pipe, pump and blast furnace works with it on day one.
2. **Axle and gearbox.** Small to build but it multiplies the value of everything else: it lets power reach where it's
   needed and makes the waterwheel, windmill and treadwheel practical.
3. **Bellows.** Cheap, early-game, very hands-on, and it fills the gap below the Stirling engine for players who don't
   have power yet.
4. **Millstone.** Gives a reason to build power before steel (flour for the planned Farmer's Delight, sand from cobble),
   and works by hand, by engine or by wheel.
5. **Shulker conveyor.** The best End-themed idea: a striking, easy-to-understand use of shulker levitation that solves
   a real problem (getting items up a tower) without competing with pipes.
