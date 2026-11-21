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
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            RyanCraft.LOGGER.warn("Couldn't write {}", path, e);
        }
        return config;
    }
}
