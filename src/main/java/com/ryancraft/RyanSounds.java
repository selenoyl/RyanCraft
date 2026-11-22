package com.ryancraft;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

/** Sound events. The files are original synths in assets/ryancraft/sounds; any resource pack can swap them. */
public final class RyanSounds {
    // Ambience loops
    public static final SoundEvent WIND = reg("ambience.wind");
    public static final SoundEvent WIND_HOWL = reg("ambience.wind_howl");
    public static final SoundEvent FOREST = reg("ambience.forest");
    public static final SoundEvent CRICKETS = reg("ambience.crickets");
    public static final SoundEvent FROGS = reg("ambience.frogs");
    public static final SoundEvent STREAM = reg("ambience.stream");
    public static final SoundEvent OCEAN = reg("ambience.ocean");
    public static final SoundEvent RAIN_LEAVES = reg("ambience.rain_leaves");
    public static final SoundEvent CAVE = reg("ambience.cave");
    // Ambience one-shots
    public static final SoundEvent BIRD = reg("ambience.bird");
    public static final SoundEvent WOODPECKER = reg("ambience.woodpecker");
    public static final SoundEvent CUCKOO = reg("ambience.cuckoo");
    public static final SoundEvent OWL = reg("ambience.owl");
    public static final SoundEvent CROW = reg("ambience.crow");
    public static final SoundEvent WOLF_HOWL = reg("ambience.wolf_howl");
    public static final SoundEvent SEAGULL = reg("ambience.seagull");
    public static final SoundEvent HAWK = reg("ambience.hawk");
    public static final SoundEvent DRIP = reg("ambience.drip");
    public static final SoundEvent RUMBLE = reg("ambience.rumble");
    public static final SoundEvent LEAVES = reg("ambience.leaves");
    public static final SoundEvent BULLFROG = reg("ambience.bullfrog");
    public static final SoundEvent STONE_SLIDE = reg("ambience.stone_slide");
    public static final SoundEvent GUST = reg("ambience.gust");
    public static final SoundEvent CICADAS = reg("ambience.cicadas");
    public static final SoundEvent SPLASH = reg("ambience.splash");
    public static final SoundEvent WATERFALL = reg("ambience.waterfall");
    // Turkey
    public static final SoundEvent TURKEY_GOBBLE = reg("entity.turkey.gobble");
    public static final SoundEvent TURKEY_CLUCK = reg("entity.turkey.cluck");
    public static final SoundEvent TURKEY_HURT = reg("entity.turkey.hurt");
    public static final SoundEvent TURKEY_DEATH = reg("entity.turkey.death");

    // Kentucky critters
    public static final SoundEvent SQUIRREL_CHATTER = reg("entity.squirrel.chatter");
    public static final SoundEvent RACCOON_CHITTER = reg("entity.raccoon.chitter");
    public static final SoundEvent SKUNK_SPRAY = reg("entity.skunk.spray");
    public static final SoundEvent OPOSSUM_HISS = reg("entity.opossum.hiss");
    public static final SoundEvent MINK_SQUEAK = reg("entity.mink.squeak");
    public static final SoundEvent BEAVER_SLAP = reg("entity.beaver.slap");
    public static final SoundEvent SONG_CARDINAL = reg("entity.songbird.cardinal");
    public static final SoundEvent SONG_GOLDFINCH = reg("entity.songbird.goldfinch");
    public static final SoundEvent SONG_ROBIN = reg("entity.songbird.robin");
    public static final SoundEvent SONG_BLUE_JAY = reg("entity.songbird.blue_jay");
    public static final SoundEvent SONG_BLUEBIRD = reg("entity.songbird.bluebird");
    public static final SoundEvent SONG_DOVE = reg("entity.songbird.mourning_dove");
    public static final SoundEvent CHIPMUNK_CHIP = reg("entity.chipmunk.chip");
    public static final SoundEvent COYOTE_YIPS = reg("entity.coyote.yips");
    public static final SoundEvent COYOTE_HOWL = reg("entity.coyote.howl");
    public static final SoundEvent CROW_CAW = reg("entity.crow.caw");
    public static final SoundEvent GOOSE_HONK = reg("entity.goose.honk");
    public static final SoundEvent GOOSE_HISS = reg("entity.goose.hiss");
    public static final SoundEvent HERON_CROAK = reg("entity.heron.croak");
    public static final SoundEvent ORCA_CALL = reg("entity.orca.call");
    public static final SoundEvent WHALE_SONG = reg("entity.blue_whale.song");
    public static final SoundEvent BOBWHITE_CALL = reg("entity.bobwhite.call");
    public static final SoundEvent RATTLESNAKE_RATTLE = reg("entity.rattlesnake.rattle");
    public static final SoundEvent ALLIGATOR_BELLOW = reg("entity.alligator.bellow");
    public static final SoundEvent ELK_BUGLE = reg("entity.elk.bugle");
    public static final SoundEvent ELK_CHIRP = reg("entity.elk.chirp");
    public static final SoundEvent MOOSE_CALL = reg("entity.moose.call");
    public static final SoundEvent PRONGHORN_SNORT = reg("entity.pronghorn.snort");
    public static final SoundEvent BOBCAT_SCREAM = reg("entity.bobcat.scream");
    public static final SoundEvent BOBCAT_GROWL = reg("entity.bobcat.growl");

    private static SoundEvent reg(String name) {
        var id = RyanCraft.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {}

    private RyanSounds() {}
}
