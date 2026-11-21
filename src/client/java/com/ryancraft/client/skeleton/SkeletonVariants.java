package com.ryancraft.client.skeleton;

import com.mojang.blaze3d.platform.NativeImage;
import com.ryancraft.RyanCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.InputStream;

/**
 * Skeleton looks derived on the player's machine from whatever skeleton texture is active
 * (vanilla, the classic 1.2.5 pack, or any resource pack): cracked skulls, missing ribs, darker aged bones.
 */
public final class SkeletonVariants {
    public static final int NORMAL = 0, CRACKED = 1, MISSING_RIBS = 2, DARK = 3, DARK_CRACKED = 4;
    private static final Identifier VANILLA = Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
    private static final String[] NAMES = {"normal", "cracked", "missing_ribs", "dark", "dark_cracked"};
    private static final Identifier[] IDS = new Identifier[NAMES.length];
    private static boolean built;

    /** Stable per skeleton: chosen from its UUID. */
    public static int pick(java.util.UUID uuid) {
        int r = Math.floorMod(uuid.hashCode() * 31 + 7, 100);
        return r < 55 ? NORMAL : r < 70 ? CRACKED : r < 85 ? MISSING_RIBS : r < 95 ? DARK : DARK_CRACKED;
    }

    /** Called on resource reload: rebuild from the (possibly new) skeleton texture next time it's needed. */
    public static void invalidate() {
        built = false;
    }

    public static Identifier texture(int variant) {
        if (variant <= NORMAL || variant >= NAMES.length) return VANILLA;
        if (!built) build();
        return IDS[variant] != null ? IDS[variant] : VANILLA;
    }

    private static void build() {
        built = true;
        Minecraft mc = Minecraft.getInstance();
        var resource = mc.getResourceManager().getResource(VANILLA);
        if (resource.isEmpty()) return;
        try (InputStream in = resource.get().open(); NativeImage base = NativeImage.read(in)) {
            for (int v = 1; v < NAMES.length; v++) {
                NativeImage img = new NativeImage(base.getWidth(), base.getHeight(), true);
                img.copyFrom(base);
                if (v == DARK || v == DARK_CRACKED) darken(img);
                if (v == CRACKED || v == DARK_CRACKED) crack(img);
                if (v == MISSING_RIBS) removeRibs(img);
                Identifier id = RyanCraft.id("dynamic/skeleton_" + NAMES[v]);
                if (IDS[v] != null) mc.getTextureManager().release(IDS[v]);
                final String label = "ryancraft skeleton " + NAMES[v];
                mc.getTextureManager().register(id, new DynamicTexture(() -> label, img));
                IDS[v] = id;
            }
        } catch (Exception e) {
            RyanCraft.LOGGER.warn("Couldn't build skeleton variants", e);
        }
    }

    // ---- edits, in 64x32 skeleton-layout pixels (scaled for HD packs) ----

    private static void darken(NativeImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int p = img.getPixel(x, y);
                int a = p >>> 24;
                if (a == 0) continue;
                int c1 = (int) (((p >> 16) & 255) * 0.60), c2 = (int) (((p >> 8) & 255) * 0.56), c3 = (int) ((p & 255) * 0.50);
                img.setPixel(x, y, (a << 24) | (c1 << 16) | (c2 << 8) | c3);
            }
        }
    }

    private static void crack(NativeImage img) {
        int dark = 0xFF2E2B28;
        // down the front of the skull, over the top, and onto the right side
        int[][] front = {{5, 0}, {5, 1}, {4, 1}, {4, 2}, {3, 2}, {3, 3}};
        int[][] top = {{5, 7}, {5, 6}, {6, 5}, {6, 4}, {7, 3}};
        int[][] side = {{7, 0}, {6, 1}, {6, 2}};
        paint(img, 8, 8, front, dark);
        paint(img, 8, 0, top, dark);
        paint(img, 0, 8, side, dark);
    }

    private static void removeRibs(NativeImage img) {
        double s = img.getWidth() / 64.0;
        // Rib rows are y=22 and y=24 on the body (front x20-27, back x32-39, sides x16-19 and x28-31).
        // Break them: the lower rib row is gone except for the spine, and half of the upper row is missing.
        for (int x = 16; x < 40; x++) {
            boolean spine = x == 23 || x == 24 || x == 35 || x == 36;
            if (!spine) fill(img, x, 24, s, 0);
            boolean leftHalf = (x >= 25 && x <= 31) || (x >= 32 && x <= 34);
            if (leftHalf) fill(img, x, 22, s, 0);
        }
    }

    private static void paint(NativeImage img, int ox, int oy, int[][] pts, int argb) {
        double s = img.getWidth() / 64.0;
        for (int[] p : pts) fill(img, ox + p[0], oy + p[1], s, argb);
    }

    private static void fill(NativeImage img, int x, int y, double s, int argb) {
        int x0 = (int) Math.round(x * s), y0 = (int) Math.round(y * s), n = Math.max(1, (int) Math.round(s));
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (x0 + i < img.getWidth() && y0 + j < img.getHeight()) img.setPixel(x0 + i, y0 + j, argb);
    }

    private SkeletonVariants() {}
}
