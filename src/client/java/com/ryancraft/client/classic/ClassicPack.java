package com.ryancraft.client.classic;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.ryancraft.RyanConfig;
import com.ryancraft.RyanCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Builds the "RyanCraft Classic 1.2.5" resource pack on the player's own machine, from Mojang's
 * official 1.2.5 client jar and pre-1.6 sound assets (the same files the Minecraft launcher downloads).
 * Nothing from Mojang is shipped inside this mod.
 */
public final class ClassicPack {
    public static final String FOLDER = "RyanCraft Classic 1.2.5";
    public static final String PACK_ID = "file/" + FOLDER;
    private static final int BUILD_VERSION = 1;

    private static final String CLIENT_JAR_SHA1 = "4a2fac7504182a97dcbcd7560c6392d7c8139928";
    private static final String CLIENT_JAR_URL = "https://piston-data.mojang.com/v1/objects/" + CLIENT_JAR_SHA1 + "/client.jar";
    private static final String INDEX_SHA1 = "3d8e55480977e32acd9844e545177e69a52f594b";
    private static final String INDEX_URL = "https://piston-meta.mojang.com/v1/packages/" + INDEX_SHA1 + "/pre-1.6.json";
    private static final String OBJECTS_URL = "https://resources.download.minecraft.net/";

    private static volatile boolean running;

    public static void start(Minecraft mc) {
        RyanConfig cfg = RyanConfig.get();
        if (!cfg.classicPack || running) return;
        running = true;
        Thread thread = new Thread(() -> {
            try {
                Path dir = mc.getResourcePackDirectory().resolve(FOLDER);
                String signature = signature(cfg);
                Path marker = dir.resolve("ryancraft_build.txt");
                boolean upToDate = Files.exists(marker) && Files.readString(marker).trim().equals(signature);
                if (!upToDate) {
                    toast(mc, "Building classic 1.2.5 pack...", "Fetching the original files from Mojang");
                    new ClassicPack(mc, cfg).build(dir, signature);
                    RyanCraft.LOGGER.info("Built {}", dir);
                }
                boolean rebuilt = !upToDate;
                mc.execute(() -> enable(mc, cfg, rebuilt));
            } catch (Exception e) {
                RyanCraft.LOGGER.error("Couldn't build the classic 1.2.5 pack", e);
                toast(mc, "Classic pack failed", String.valueOf(e.getMessage()));
            } finally {
                running = false;
            }
        }, "RyanCraft classic pack");
        thread.setDaemon(true);
        thread.start();
    }

    private static String signature(RyanConfig cfg) {
        return "v" + BUILD_VERSION + ";t=" + cfg.classicTextures + ";s=" + cfg.classicSounds + ";m=" + cfg.classicMusic;
    }

    private static void enable(Minecraft mc, RyanConfig cfg, boolean rebuilt) {
        PackRepository repo = mc.getResourcePackRepository();
        repo.reload();
        if (!repo.isAvailable(PACK_ID)) return;
        List<String> selected = new ArrayList<>(repo.getSelectedIds());
        boolean changed = false;
        if (!cfg.classicPackAutoEnabled) {
            if (cfg.classicProgrammerArt && repo.isAvailable("programmer_art") && !selected.contains("programmer_art")) {
                selected.add("programmer_art");
            }
            selected.remove(PACK_ID);
            selected.add(PACK_ID);      // last = highest priority
            changed = true;
            cfg.classicPackAutoEnabled = true;
            cfg.save();
        }
        if (changed) {
            repo.setSelected(selected);
            mc.options.updateResourcePacks(repo);
            toast(mc, "Classic 1.2.5 pack on", "Turn it off any time in Resource Packs");
        } else if (rebuilt && selected.contains(PACK_ID)) {
            mc.reloadResourcePacks();
        }
    }

    private static void toast(Minecraft mc, String title, String body) {
        mc.execute(() -> SystemToast.addOrUpdate(mc.getToastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.literal(title), Component.literal(body)));
    }

    // ------------------------------------------------------------------------------------------

    private final Minecraft mc;
    private final RyanConfig cfg;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL).build();
    private final Path cache;
    private Path out;

    private ClassicPack(Minecraft mc, RyanConfig cfg) {
        this.mc = mc;
        this.cfg = cfg;
        this.cache = mc.gameDirectory.toPath().resolve("ryancraft").resolve("cache");
    }

    private void build(Path dir, String signature) throws Exception {
        Files.createDirectories(cache);
        Path tmp = dir.resolveSibling(FOLDER + ".building");
        deleteTree(tmp);
        Files.createDirectories(tmp);
        this.out = tmp;

        write("pack.mcmeta", ("{\n  \"pack\": {\n    \"description\": \"§6Minecraft 1.2.5, the way it was§r\\nBuilt locally by RyanCraft\",\n"
                + "    \"min_format\": [75, 0],\n    \"max_format\": [75, 0]\n  }\n}\n").getBytes(StandardCharsets.UTF_8));

        if (cfg.classicTextures) {
            Path jar = fetch(CLIENT_JAR_URL, CLIENT_JAR_SHA1);
            try (ZipFile zip = new ZipFile(jar.toFile())) {
                textures(zip);
            }
        }
        if (cfg.classicSounds || cfg.classicMusic) {
            Path indexFile = fetch(INDEX_URL, INDEX_SHA1);
            JsonObject objects = JsonParser.parseString(Files.readString(indexFile)).getAsJsonObject().getAsJsonObject("objects");
            sounds(objects);
        }
        write("ryancraft_build.txt", signature.getBytes(StandardCharsets.UTF_8));

        deleteTree(dir);
        Files.move(tmp, dir, StandardCopyOption.ATOMIC_MOVE);
    }

    // ---------------- textures ----------------

    /** terrain.png tile index -> modern block texture names. */
    private static final Map<Integer, String[]> TERRAIN = new LinkedHashMap<>();
    /** gui/items.png tile index -> modern item texture names. */
    private static final Map<Integer, String[]> ITEMS = new LinkedHashMap<>();

    private static void t(int i, String... n) { TERRAIN.put(i, n); }
    private static void it(int i, String... n) { ITEMS.put(i, n); }

    static {
        t(0, "grass_block_top"); t(1, "stone"); t(2, "dirt"); t(3, "grass_block_side"); t(4, "oak_planks");
        t(5, "smooth_stone_slab_side"); t(6, "smooth_stone"); t(7, "bricks"); t(8, "tnt_side"); t(9, "tnt_top");
        t(10, "tnt_bottom"); t(11, "cobweb"); t(12, "poppy"); t(13, "dandelion"); t(15, "oak_sapling");
        t(16, "cobblestone"); t(17, "bedrock"); t(18, "sand"); t(19, "gravel"); t(20, "oak_log");
        t(21, "oak_log_top", "spruce_log_top", "birch_log_top", "jungle_log_top"); t(22, "iron_block"); t(23, "gold_block");
        t(24, "diamond_block"); t(28, "red_mushroom"); t(29, "brown_mushroom");
        t(32, "gold_ore"); t(33, "iron_ore"); t(34, "coal_ore"); t(35, "bookshelf"); t(36, "mossy_cobblestone");
        t(37, "obsidian"); t(38, "grass_block_side_overlay"); t(39, "short_grass"); t(43, "crafting_table_top");
        t(44, "furnace_front"); t(45, "furnace_side"); t(46, "dispenser_front");
        t(48, "sponge"); t(49, "glass"); t(50, "diamond_ore"); t(51, "redstone_ore");
        t(52, "oak_leaves", "birch_leaves", "jungle_leaves"); t(54, "stone_bricks"); t(55, "dead_bush"); t(56, "fern");
        t(59, "crafting_table_side"); t(60, "crafting_table_front"); t(61, "furnace_front_on"); t(62, "furnace_top", "dispenser_front_vertical");
        t(63, "spruce_sapling");
        t(64, "white_wool"); t(65, "spawner"); t(66, "snow"); t(67, "ice"); t(68, "grass_block_snow");
        t(69, "cactus_top"); t(70, "cactus_side"); t(71, "cactus_bottom"); t(72, "clay"); t(73, "sugar_cane");
        t(74, "note_block", "jukebox_side"); t(75, "jukebox_top"); t(76, "lily_pad"); t(77, "mycelium_side");
        t(78, "mycelium_top"); t(79, "birch_sapling");
        t(80, "torch"); t(81, "oak_door_top"); t(82, "iron_door_top"); t(83, "ladder"); t(84, "oak_trapdoor");
        t(85, "iron_bars"); t(86, "farmland_moist"); t(87, "farmland");
        for (int s = 0; s < 8; s++) t(88 + s, "wheat_stage" + s);
        t(96, "lever"); t(97, "oak_door_bottom"); t(98, "iron_door_bottom"); t(99, "redstone_torch");
        t(100, "mossy_stone_bricks"); t(101, "cracked_stone_bricks"); t(102, "pumpkin_top"); t(103, "netherrack");
        t(104, "soul_sand"); t(105, "glowstone"); t(106, "piston_top_sticky"); t(107, "piston_top");
        t(108, "piston_side"); t(109, "piston_bottom"); t(110, "piston_inner"); t(111, "pumpkin_stem", "melon_stem");
        t(112, "rail_corner"); t(113, "black_wool"); t(114, "gray_wool"); t(115, "redstone_torch_off");
        t(116, "spruce_log"); t(117, "birch_log"); t(118, "pumpkin_side"); t(119, "carved_pumpkin");
        t(120, "jack_o_lantern"); t(121, "cake_top"); t(122, "cake_side"); t(123, "cake_inner"); t(124, "cake_bottom");
        t(125, "red_mushroom_block"); t(126, "brown_mushroom_block"); t(127, "attached_pumpkin_stem", "attached_melon_stem");
        t(128, "rail"); t(129, "red_wool"); t(130, "pink_wool"); t(131, "repeater"); t(132, "spruce_leaves");
        t(136, "melon_side"); t(137, "melon_top"); t(138, "cauldron_top"); t(139, "cauldron_inner");
        t(141, "mushroom_block_inside"); t(142, "mushroom_stem");
        t(144, "lapis_block"); t(145, "green_wool"); t(146, "lime_wool"); t(147, "repeater_on");
        t(148, "glass_pane_top"); t(153, "jungle_log"); t(154, "cauldron_side"); t(155, "cauldron_bottom");
        t(156, "brewing_stand_base"); t(157, "brewing_stand"); t(158, "end_portal_frame_top"); t(159, "end_portal_frame_side");
        t(160, "lapis_ore"); t(161, "brown_wool"); t(162, "yellow_wool"); t(163, "powered_rail");
        t(166, "enchanting_table_top"); t(167, "dragon_egg"); t(174, "end_portal_frame_eye"); t(175, "end_stone");
        t(176, "sandstone_top"); t(177, "blue_wool"); t(178, "light_blue_wool"); t(179, "powered_rail_on");
        t(182, "enchanting_table_side"); t(183, "enchanting_table_bottom");
        t(192, "sandstone"); t(193, "purple_wool"); t(194, "magenta_wool"); t(195, "detector_rail");
        t(198, "spruce_planks"); t(199, "jungle_planks");
        t(208, "sandstone_bottom"); t(209, "cyan_wool"); t(210, "orange_wool"); t(211, "redstone_lamp");
        t(212, "redstone_lamp_on"); t(213, "chiseled_stone_bricks"); t(214, "birch_planks");
        t(224, "nether_bricks"); t(225, "light_gray_wool"); t(226, "nether_wart_stage0"); t(227, "nether_wart_stage1");
        t(228, "nether_wart_stage2"); t(229, "chiseled_sandstone"); t(230, "cut_sandstone");
        for (int s = 0; s < 10; s++) t(240 + s, "destroy_stage_" + s);

        it(1, "chainmail_helmet"); it(2, "iron_helmet"); it(3, "diamond_helmet"); it(4, "golden_helmet");
        it(5, "flint_and_steel"); it(6, "flint"); it(7, "coal"); it(8, "string"); it(9, "wheat_seeds");
        it(10, "apple"); it(11, "golden_apple"); it(12, "egg"); it(13, "sugar"); it(14, "snowball");
        it(17, "chainmail_chestplate"); it(18, "iron_chestplate"); it(19, "diamond_chestplate"); it(20, "golden_chestplate");
        it(21, "bow"); it(22, "brick"); it(23, "iron_ingot"); it(24, "feather"); it(25, "wheat"); it(26, "painting");
        it(27, "sugar_cane"); it(28, "bone"); it(29, "cake"); it(30, "slime_ball");
        it(33, "chainmail_leggings"); it(34, "iron_leggings"); it(35, "diamond_leggings"); it(36, "golden_leggings");
        it(37, "arrow"); it(39, "gold_ingot"); it(40, "gunpowder"); it(41, "bread"); it(42, "oak_sign");
        it(43, "oak_door"); it(44, "iron_door"); it(46, "fire_charge");
        it(49, "chainmail_boots"); it(50, "iron_boots"); it(51, "diamond_boots"); it(52, "golden_boots");
        it(53, "stick"); it(55, "diamond"); it(56, "redstone"); it(57, "clay_ball"); it(58, "paper"); it(59, "book");
        it(61, "pumpkin_seeds"); it(62, "melon_seeds");
        it(64, "wooden_sword"); it(65, "stone_sword"); it(66, "iron_sword"); it(67, "diamond_sword"); it(68, "golden_sword");
        it(69, "fishing_rod"); it(71, "bowl"); it(72, "mushroom_stew"); it(73, "glowstone_dust"); it(74, "bucket");
        it(75, "water_bucket"); it(76, "lava_bucket"); it(77, "milk_bucket"); it(78, "ink_sac"); it(79, "gray_dye");
        it(80, "wooden_shovel"); it(81, "stone_shovel"); it(82, "iron_shovel"); it(83, "diamond_shovel"); it(84, "golden_shovel");
        it(85, "fishing_rod_cast"); it(86, "repeater"); it(87, "porkchop"); it(88, "cooked_porkchop"); it(89, "cod");
        it(90, "cooked_cod"); it(91, "rotten_flesh"); it(92, "cookie"); it(93, "shears"); it(94, "red_dye"); it(95, "pink_dye");
        it(96, "wooden_pickaxe"); it(97, "stone_pickaxe"); it(98, "iron_pickaxe"); it(99, "diamond_pickaxe"); it(100, "golden_pickaxe");
        it(101, "bow_pulling_0"); it(103, "leather"); it(104, "saddle"); it(105, "beef"); it(106, "cooked_beef");
        it(107, "ender_pearl"); it(108, "blaze_rod"); it(109, "melon_slice"); it(110, "green_dye"); it(111, "lime_dye");
        it(112, "wooden_axe"); it(113, "stone_axe"); it(114, "iron_axe"); it(115, "diamond_axe"); it(116, "golden_axe");
        it(117, "bow_pulling_1"); it(121, "chicken"); it(122, "cooked_chicken"); it(123, "ghast_tear"); it(124, "gold_nugget");
        it(125, "nether_wart"); it(126, "cocoa_beans"); it(127, "yellow_dye");
        it(128, "wooden_hoe"); it(129, "stone_hoe"); it(130, "iron_hoe"); it(131, "diamond_hoe"); it(132, "golden_hoe");
        it(133, "bow_pulling_2"); it(135, "minecart"); it(136, "oak_boat"); it(137, "glistering_melon_slice");
        it(138, "fermented_spider_eye"); it(139, "spider_eye"); it(140, "glass_bottle"); it(142, "lapis_lazuli");
        it(143, "light_blue_dye"); it(151, "chest_minecart"); it(155, "ender_eye"); it(157, "blaze_powder");
        it(158, "purple_dye"); it(159, "magenta_dye");
    }

    /** Old file in the 1.2.5 jar -> modern texture path(s); copied only when the modern one is the same size. */
    private static final String[][] SAME_SIZE = {
            {"mob/creeper.png", "entity/creeper/creeper.png"},
            {"mob/skeleton.png", "entity/skeleton/skeleton.png"},
            {"mob/spider.png", "entity/spider/spider.png"},
            {"mob/spider_eyes.png", "entity/spider_eyes.png"},
            {"mob/cavespider.png", "entity/spider/cave_spider.png"},
            {"mob/slime.png", "entity/slime/slime.png"},
            {"mob/enderman.png", "entity/enderman/enderman.png"},
            {"mob/enderman_eyes.png", "entity/enderman/enderman_eyes.png"},
            {"mob/sheep.png", "entity/sheep/sheep.png"},
            {"mob/sheep_fur.png", "entity/sheep/sheep_wool.png"},
            {"mob/pig.png", "entity/pig/temperate_pig.png"},
            {"mob/cow.png", "entity/cow/temperate_cow.png"},
            {"mob/chicken.png", "entity/chicken/temperate_chicken.png"},
            {"mob/squid.png", "entity/squid/squid.png"},
            {"mob/ghast.png", "entity/ghast/ghast.png"},
            {"mob/ghast_fire.png", "entity/ghast/ghast_shooting.png"},
            {"mob/wolf.png", "entity/wolf/wolf.png"},
            {"mob/wolf_tame.png", "entity/wolf/wolf_tame.png"},
            {"mob/wolf_angry.png", "entity/wolf/wolf_angry.png"},
            {"mob/silverfish.png", "entity/silverfish.png"},
            {"mob/ozelot.png", "entity/cat/ocelot.png"},
            {"mob/villager_golem.png", "entity/iron_golem/iron_golem.png"},
            {"mob/snowman.png", "entity/snow_golem.png"},
            {"armor/chain_1.png", "entity/equipment/humanoid/chainmail.png"},
            {"armor/chain_2.png", "entity/equipment/humanoid_leggings/chainmail.png"},
            {"armor/iron_1.png", "entity/equipment/humanoid/iron.png"},
            {"armor/iron_2.png", "entity/equipment/humanoid_leggings/iron.png"},
            {"armor/gold_1.png", "entity/equipment/humanoid/gold.png"},
            {"armor/gold_2.png", "entity/equipment/humanoid_leggings/gold.png"},
            {"armor/diamond_1.png", "entity/equipment/humanoid/diamond.png"},
            {"armor/diamond_2.png", "entity/equipment/humanoid_leggings/diamond.png"},
            {"font/default.png", "font/ascii.png"},
            {"environment/clouds.png", "environment/clouds.png"},
            {"environment/rain.png", "environment/rain.png"},
            {"environment/snow.png", "environment/snow.png"},
            {"misc/water.png", "misc/underwater.png"},
            {"misc/pumpkinblur.png", "misc/pumpkinblur.png"},
            {"misc/vignette.png", "misc/vignette.png"},
            {"misc/grasscolor.png", "colormap/grass.png"},
            {"misc/foliagecolor.png", "colormap/foliage.png"},
            {"item/sign.png", "entity/signs/oak.png"},
            {"item/arrows.png", "entity/projectiles/arrow.png"},
            {"item/xporb.png", "entity/experience_orb.png"},
            {"item/book.png", "entity/enchanting_table_book.png"},
    };

    private void textures(ZipFile zip) throws IOException {
        try (NativeImage terrain = read(zip, "terrain.png")) {
            int tile = terrain.getWidth() / 16;
            for (var e : TERRAIN.entrySet()) {
                int i = e.getKey();
                try (NativeImage img = crop(terrain, (i % 16) * tile, (i / 16) * tile, tile, tile)) {
                    for (String name : e.getValue()) png("assets/minecraft/textures/block/" + name + ".png", img);
                }
            }
        }
        try (NativeImage items = read(zip, "gui/items.png")) {
            int tile = items.getWidth() / 16;
            for (var e : ITEMS.entrySet()) {
                int i = e.getKey();
                try (NativeImage img = crop(items, (i % 16) * tile, (i / 16) * tile, tile, tile)) {
                    for (String name : e.getValue()) png("assets/minecraft/textures/item/" + name + ".png", img);
                }
            }
        }
        for (String[] map : SAME_SIZE) {
            if (zip.getEntry(map[0]) == null) continue;
            try (NativeImage old = read(zip, map[0])) {
                int[] modern = vanillaSize(map[1]);
                if (modern != null && modern[0] == old.getWidth() && modern[1] == old.getHeight()) {
                    png("assets/minecraft/textures/" + map[1], old);
                }
            }
        }
        // Old Steve and zombie: 64x32 legacy skins, converted to the 64x64 layout
        legacySkin(zip, "mob/char.png", "entity/player/wide/steve.png");
        legacySkin(zip, "mob/zombie.png", "entity/zombie/zombie.png");
        // The 1.2.5 title-screen panorama
        for (int i = 0; i < 6; i++) {
            String from = "title/bg/panorama" + i + ".png";
            if (zip.getEntry(from) != null) {
                try (NativeImage img = read(zip, from)) { png("assets/minecraft/textures/gui/title/background/panorama_" + i + ".png", img); }
            }
        }
        gui(zip);
    }

    /** HUD bits and buttons cut from the old gui.png / icons.png sheets. */
    private void gui(ZipFile zip) throws IOException {
        String s = "assets/minecraft/textures/gui/sprites/";
        try (NativeImage gui = read(zip, "gui/gui.png")) {
            cut(gui, 0, 0, 182, 22, s + "hud/hotbar.png");
            cut(gui, 0, 22, 24, 23, s + "hud/hotbar_selection.png");
            cut(gui, 0, 66, 200, 20, s + "widget/button.png");
            cut(gui, 0, 86, 200, 20, s + "widget/button_highlighted.png");
            cut(gui, 0, 46, 200, 20, s + "widget/button_disabled.png");
            String nine = "{\"gui\":{\"scaling\":{\"type\":\"nine_slice\",\"width\":200,\"height\":20,\"border\":{\"left\":20,\"top\":4,\"right\":20,\"bottom\":4}}}}";
            for (String b : new String[]{"button", "button_highlighted", "button_disabled"}) {
                write(s + "widget/" + b + ".png.mcmeta", nine.getBytes(StandardCharsets.UTF_8));
            }
        }
        try (NativeImage icons = read(zip, "gui/icons.png")) {
            cut(icons, 0, 0, 15, 15, s + "hud/crosshair.png");
            cut(icons, 16, 0, 9, 9, s + "hud/heart/container.png");
            cut(icons, 52, 0, 9, 9, s + "hud/heart/full.png");
            cut(icons, 61, 0, 9, 9, s + "hud/heart/half.png");
            cut(icons, 16, 9, 9, 9, s + "hud/armor_empty.png");
            cut(icons, 25, 9, 9, 9, s + "hud/armor_half.png");
            cut(icons, 34, 9, 9, 9, s + "hud/armor_full.png");
            cut(icons, 16, 18, 9, 9, s + "hud/air.png");
            cut(icons, 25, 18, 9, 9, s + "hud/air_bursting.png");
            cut(icons, 16, 27, 9, 9, s + "hud/food_empty.png");
            cut(icons, 52, 27, 9, 9, s + "hud/food_full.png");
            cut(icons, 61, 27, 9, 9, s + "hud/food_half.png");
            cut(icons, 0, 64, 182, 5, s + "hud/experience_bar_background.png");
            cut(icons, 0, 69, 182, 5, s + "hud/experience_bar_progress.png");
        }
    }

    private void cut(NativeImage src, int x, int y, int w, int h, String path) throws IOException {
        try (NativeImage img = crop(src, x, y, w, h)) { png(path, img); }
    }

    /** Same conversion vanilla applies to old 64x32 player skins. */
    private void legacySkin(ZipFile zip, String from, String to) throws IOException {
        if (zip.getEntry(from) == null) return;
        try (NativeImage old = read(zip, from); NativeImage img = new NativeImage(64, 64, true)) {
            for (int x = 0; x < old.getWidth(); x++)
                for (int y = 0; y < old.getHeight(); y++) img.setPixel(x, y, old.getPixel(x, y));
            img.copyRect(4, 16, 16, 32, 4, 4, true, false);
            img.copyRect(8, 16, 16, 32, 4, 4, true, false);
            img.copyRect(0, 20, 24, 32, 4, 12, true, false);
            img.copyRect(4, 20, 16, 32, 4, 12, true, false);
            img.copyRect(8, 20, 8, 32, 4, 12, true, false);
            img.copyRect(12, 20, 16, 32, 4, 12, true, false);
            img.copyRect(44, 16, -8, 32, 4, 4, true, false);
            img.copyRect(48, 16, -8, 32, 4, 4, true, false);
            img.copyRect(40, 20, 0, 32, 4, 12, true, false);
            img.copyRect(44, 20, -8, 32, 4, 12, true, false);
            img.copyRect(48, 20, -16, 32, 4, 12, true, false);
            img.copyRect(52, 20, -8, 32, 4, 12, true, false);
            png("assets/minecraft/textures/" + to, img);
        }
    }

    private int[] vanillaSize(String texturePath) {
        try {
            var supplier = mc.getVanillaPackResources().getResource(PackType.CLIENT_RESOURCES,
                    Identifier.withDefaultNamespace("textures/" + texturePath));
            if (supplier == null) return null;
            try (InputStream in = supplier.get(); NativeImage img = NativeImage.read(in)) {
                return new int[]{img.getWidth(), img.getHeight()};
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static NativeImage read(ZipFile zip, String name) throws IOException {
        ZipEntry entry = zip.getEntry(name);
        if (entry == null) throw new IOException("missing " + name + " in 1.2.5 jar");
        try (InputStream in = zip.getInputStream(entry)) {
            return NativeImage.read(in);
        }
    }

    private static NativeImage crop(NativeImage src, int x0, int y0, int w, int h) {
        NativeImage img = new NativeImage(w, h, true);
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++) img.setPixel(x, y, src.getPixel(x0 + x, y0 + y));
        return img;
    }

    // ---------------- sounds & music ----------------

    /** Modern sound event -> the 1.2.5 files it used (paths under newsound/, without .ogg). */
    private static final Map<String, String[]> EVENTS = new LinkedHashMap<>();
    private static void ev(String event, String... files) { EVENTS.put(event, files); }

    static {
        ev("entity.player.hurt", "damage/hurtflesh1", "damage/hurtflesh2", "damage/hurtflesh3");
        ev("entity.player.hurt_drown", "damage/hurtflesh1", "damage/hurtflesh2", "damage/hurtflesh3");
        ev("entity.player.hurt_on_fire", "damage/hurtflesh1", "damage/hurtflesh2", "damage/hurtflesh3");
        ev("entity.player.big_fall", "damage/fallbig1", "damage/fallbig2");
        ev("entity.generic.big_fall", "damage/fallbig1", "damage/fallbig2");
        ev("entity.hostile.big_fall", "damage/fallbig1", "damage/fallbig2");
        ev("entity.chicken.ambient", "mob/chicken1", "mob/chicken2", "mob/chicken3");
        ev("entity.chicken.hurt", "mob/chickenhurt1", "mob/chickenhurt2");
        ev("entity.chicken.death", "mob/chickenhurt1", "mob/chickenhurt2");
        ev("entity.chicken.egg", "mob/chickenplop");
        ev("entity.cow.ambient", "mob/cow1", "mob/cow2", "mob/cow3", "mob/cow4");
        ev("entity.cow.hurt", "mob/cowhurt1", "mob/cowhurt2", "mob/cowhurt3");
        ev("entity.cow.death", "mob/cowhurt1", "mob/cowhurt2", "mob/cowhurt3");
        ev("entity.pig.ambient", "mob/pig1", "mob/pig2", "mob/pig3");
        ev("entity.pig.hurt", "mob/pig1", "mob/pig2", "mob/pig3");
        ev("entity.pig.death", "mob/pigdeath");
        ev("entity.sheep.ambient", "mob/sheep1", "mob/sheep2", "mob/sheep3");
        ev("entity.sheep.hurt", "mob/sheep1", "mob/sheep2", "mob/sheep3");
        ev("entity.sheep.death", "mob/sheep1", "mob/sheep2", "mob/sheep3");
        ev("entity.creeper.hurt", "mob/creeper1", "mob/creeper2", "mob/creeper3", "mob/creeper4");
        ev("entity.creeper.death", "mob/creeperdeath");
        ev("entity.skeleton.ambient", "mob/skeleton1", "mob/skeleton2", "mob/skeleton3");
        ev("entity.skeleton.hurt", "mob/skeletonhurt1", "mob/skeletonhurt2", "mob/skeletonhurt3", "mob/skeletonhurt4");
        ev("entity.skeleton.death", "mob/skeletondeath");
        ev("entity.spider.ambient", "mob/spider1", "mob/spider2", "mob/spider3", "mob/spider4");
        ev("entity.spider.hurt", "mob/spider1", "mob/spider2", "mob/spider3", "mob/spider4");
        ev("entity.spider.death", "mob/spiderdeath");
        ev("entity.slime.jump", "mob/slime1", "mob/slime2", "mob/slime3", "mob/slime4", "mob/slime5");
        ev("entity.slime.squish", "mob/slime1", "mob/slime2", "mob/slime3", "mob/slime4", "mob/slime5");
        ev("entity.slime.hurt", "mob/slime1", "mob/slime2", "mob/slime3", "mob/slime4", "mob/slime5");
        ev("entity.slime.death", "mob/slime1", "mob/slime2", "mob/slime3", "mob/slime4", "mob/slime5");
        ev("entity.slime.attack", "mob/slimeattack1", "mob/slimeattack2");
        ev("entity.wolf.ambient", "mob/wolf/bark1", "mob/wolf/bark2", "mob/wolf/bark3");
        ev("entity.wolf.growl", "mob/wolf/growl1", "mob/wolf/growl2", "mob/wolf/growl3");
        ev("entity.wolf.hurt", "mob/wolf/hurt1", "mob/wolf/hurt2", "mob/wolf/hurt3");
        ev("entity.wolf.death", "mob/wolf/death");
        ev("entity.wolf.pant", "mob/wolf/panting");
        ev("entity.wolf.whine", "mob/wolf/whine");
        ev("entity.zombie.ambient", "mob/zombie1", "mob/zombie2", "mob/zombie3");
        ev("entity.zombie.hurt", "mob/zombiehurt1", "mob/zombiehurt2");
        ev("entity.zombie.death", "mob/zombiedeath");
        ev("entity.zombified_piglin.ambient", "mob/zombiepig/zpig1", "mob/zombiepig/zpig2", "mob/zombiepig/zpig3", "mob/zombiepig/zpig4");
        ev("entity.zombified_piglin.angry", "mob/zombiepig/zpigangry1", "mob/zombiepig/zpigangry2", "mob/zombiepig/zpigangry3", "mob/zombiepig/zpigangry4");
        ev("entity.zombified_piglin.hurt", "mob/zombiepig/zpighurt1", "mob/zombiepig/zpighurt2");
        ev("entity.zombified_piglin.death", "mob/zombiepig/zpigdeath");
    }

    private static final String[] MUSIC_EVENTS = {
            "music.game", "music.creative", "music.menu", "music.under_water",
            "music.overworld.badlands", "music.overworld.bamboo_jungle", "music.overworld.cherry_grove",
            "music.overworld.deep_dark", "music.overworld.desert", "music.overworld.dripstone_caves",
            "music.overworld.flower_forest", "music.overworld.forest", "music.overworld.frozen_peaks",
            "music.overworld.grove", "music.overworld.jagged_peaks", "music.overworld.jungle",
            "music.overworld.lush_caves", "music.overworld.meadow", "music.overworld.old_growth_taiga",
            "music.overworld.snowy_slopes", "music.overworld.sparse_jungle", "music.overworld.stony_peaks",
            "music.overworld.swamp", "music.nether.basalt_deltas", "music.nether.crimson_forest",
            "music.nether.nether_wastes", "music.nether.soul_sand_valley", "music.nether.warped_forest"};

    private void sounds(JsonObject objects) throws Exception {
        JsonObject soundsJson = new JsonObject();
        if (cfg.classicSounds) {
            Map<String, String> wanted = new HashMap<>();
            for (String key : objects.keySet()) {
                if (key.startsWith("newsound/") && key.endsWith(".ogg") && !key.contains(" ")) {
                    wanted.put(key, "assets/minecraft/sounds/" + key.substring("newsound/".length()));
                }
            }
            for (var e : wanted.entrySet()) {
                write(e.getValue(), Files.readAllBytes(object(objects, e.getKey())));
            }
            for (var e : EVENTS.entrySet()) {
                JsonArray list = new JsonArray();
                for (String f : e.getValue()) list.add(f);
                JsonObject entry = new JsonObject();
                entry.addProperty("replace", true);
                entry.add("sounds", list);
                soundsJson.add(e.getKey(), entry);
            }
        }
        if (cfg.classicMusic) {
            JsonArray tracks = new JsonArray();
            for (String key : objects.keySet()) {
                if ((key.startsWith("music/") || key.startsWith("newmusic/")) && key.endsWith(".ogg")) {
                    String name = key.substring(key.indexOf('/') + 1, key.length() - 4);
                    write("assets/minecraft/sounds/classic/music/" + name + ".ogg", Files.readAllBytes(object(objects, key)));
                    JsonObject track = new JsonObject();
                    track.addProperty("name", "classic/music/" + name);
                    track.addProperty("stream", true);
                    tracks.add(track);
                }
            }
            for (String event : MUSIC_EVENTS) {
                JsonObject entry = new JsonObject();
                entry.addProperty("replace", true);
                entry.add("sounds", tracks.deepCopy());
                soundsJson.add(event, entry);
            }
        }
        if (soundsJson.size() > 0) {
            write("assets/minecraft/sounds.json", soundsJson.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private Path object(JsonObject objects, String key) throws Exception {
        String hash = objects.getAsJsonObject(key).get("hash").getAsString();
        return fetch(OBJECTS_URL + hash.substring(0, 2) + "/" + hash, hash);
    }

    // ---------------- io ----------------

    /** Download once into the cache, verified by SHA-1. */
    private Path fetch(String url, String sha1) throws Exception {
        Path file = cache.resolve(sha1);
        if (Files.exists(file) && sha1.equals(sha1(Files.readAllBytes(file)))) return file;
        Exception last = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                HttpResponse<byte[]> resp = http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(60)).GET().build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                if (resp.statusCode() != 200) throw new IOException("HTTP " + resp.statusCode() + " for " + url);
                byte[] body = resp.body();
                if (!sha1.equals(sha1(body))) throw new IOException("Checksum mismatch for " + url);
                Path tmp = cache.resolve(sha1 + ".part");
                Files.write(tmp, body);
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
                return file;
            } catch (Exception e) {
                last = e;
                Thread.sleep(1000L * (attempt + 1));
            }
        }
        throw last;
    }

    private static String sha1(byte[] data) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
    }

    private void write(String path, byte[] data) throws IOException {
        Path p = out.resolve(path);
        Files.createDirectories(p.getParent());
        Files.write(p, data);
    }

    private void png(String path, NativeImage img) throws IOException {
        Path p = out.resolve(path);
        Files.createDirectories(p.getParent());
        img.writeToFile(p);
    }

    private static void deleteTree(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);
        }
    }
}
