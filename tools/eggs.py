"""Regenerates every RyanCraft spawn egg from the classic base-game egg (tools/classic_spawn_egg*.png)."""
import critters, big_game, wildlife, fish2
from critters import egg_icon, save_item

EXTRA = {"reindeer": ("#e8e2d6", "#6a5a48"), "butterfly": ("#e08a2a", "#1a1414"), "moobloom": ("#f2c626", "#fbf2d0"), "swan": ("#f4f4f0", "#e8902a"), "mudling": ("#7a5638", "#6aa83a"), "sky_ray": ("#b8d4f0", "#ffffff")}

if __name__ == "__main__":
    eggs = dict(critters.EGGS); eggs.update(big_game.EGGS)
    eggs.update({n: e[2] for n, e in wildlife.entries().items()}); eggs.update(fish2.EGGS); eggs.update(EXTRA)
    for mob, (b, s) in eggs.items(): save_item(f"{mob}_spawn_egg", egg_icon(b, s))
    print(len(eggs), "eggs")
