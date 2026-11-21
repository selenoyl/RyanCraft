package com.ryancraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * config/ryancraft.json. Every classic tweak can be switched off here.
 */
public final class RyanConfig {
    /** Don't create a chat-signing key, so your messages can't be reported. Servers that require signed chat will refuse you. */
    public boolean disableChatReporting = true;
    /** Turn off the text/profanity filter (client and server side). */
    public boolean disableProfanityFilter = true;
    /** Default skins come only from Steve and Alex, picked the classic way from the player UUID. */
    public boolean classicDefaultSkins = true;
    /** Hide the Report button in the Social Interactions screen. */
    public boolean hideReportButton = true;

    // ---- Classic 1.2.5 pack (built on your machine from Mojang's official 1.2.5 files) ----
    /** Build and use the "RyanCraft Classic 1.2.5" resource pack. */
    public boolean classicPack = true;
    /** 1.2.5 block, item, mob, armor and font textures (incl. the old gravel) and old Steve. */
    public boolean classicTextures = true;
    /** 1.2.5 sound effects: steps, mobs, damage, caves, rain, doors... */
    public boolean classicSounds = true;
    /** Only the original C418 tracks from 1.2.5 (calm, hal, nuance, piano), everywhere. */
    public boolean classicMusic = true;
    /** Also enable the built-in Programmer Art pack under it, so blocks newer than 1.2.5 match. */
    public boolean classicProgrammerArt = true;
    /** Set once the pack has been switched on automatically; after that it's your choice in Resource Packs. */
    public boolean classicPackAutoEnabled = false;

    // ---- Atmospheric ambience (MAtmos-style) ----
    /** Environment-driven ambience: birds, crickets, owls, wind, streams, waves, caves... */
    public boolean ambience = true;
    /** Extra volume multiplier on top of the game's Ambient/Environment slider. */
    public float ambienceVolume = 1.0f;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static RyanConfig instance;

    public static synchronized RyanConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static RyanConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("ryancraft.json");
        RyanConfig config = new RyanConfig();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                RyanConfig read = GSON.fromJson(reader, RyanConfig.class);
                if (read != null) {
                    config = read;
                }
            } catch (Exception e) {
                RyanCraft.LOGGER.warn("Couldn't read {}, using defaults", path, e);
            }
        }
        config.save();
        return config;
    }

    public synchronized void save() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("ryancraft.json");
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            RyanCraft.LOGGER.warn("Couldn't write {}", path, e);
        }
    }
}
