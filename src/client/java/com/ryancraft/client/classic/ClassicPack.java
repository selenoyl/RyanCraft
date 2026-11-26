package com.ryancraft.client.classic;

import com.ryancraft.RyanConfig;
import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * The classic 1.2.5 look and sound, shipped inside the mod as a built-in resource pack (resourcepacks/classic):
 * the original textures, sounds and music, rearranged for today's game. On by default; switch it off in Resource Packs
 * (or with "classicPack": false in config/ryancraft.json).
 */
public final class ClassicPack {
    public static final String PACK_ID = RyanCraft.MOD_ID + ":classic";

    /** Registers the built-in pack (client init). */
    public static void register() {
        if (!RyanConfig.get().classicPack) return;
        FabricLoader.getInstance().getModContainer(RyanCraft.MOD_ID).ifPresent(mod ->
                ResourceLoader.registerBuiltinPack(RyanCraft.id("classic"), mod, Component.literal("RyanCraft Classic 1.2.5"), PackActivationType.DEFAULT_ENABLED));
    }

    /** First launch only: put Programmer Art underneath it (as the classic set used), with the classic pack on top. */
    public static void start(Minecraft mc) {
        RyanConfig cfg = RyanConfig.get();
        if (!cfg.classicPack || cfg.classicPackAutoEnabled) return;
        PackRepository repo = mc.getResourcePackRepository();
        if (!repo.isAvailable(PACK_ID)) return;
        List<String> selected = new ArrayList<>(repo.getSelectedIds());
        if (cfg.classicProgrammerArt && repo.isAvailable("programmer_art") && !selected.contains("programmer_art")) selected.add("programmer_art");
        selected.remove(PACK_ID);
        selected.add(PACK_ID);          // last = highest priority
        cfg.classicPackAutoEnabled = true;
        cfg.save();
        mc.options.menuBackgroundBlurriness().set(0);   // the classic panorama has its own soft 1.2.5 blur baked in
        mc.options.save();
        repo.setSelected(selected);
        mc.options.updateResourcePacks(repo);
    }

    private ClassicPack() {}
}
