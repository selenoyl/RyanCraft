package com.ryancraft.client.pipes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Draws a box of fluid (all six faces) with a fluid's sprite and tint. Coordinates in block units. */
final class FluidBoxes {
    static void box(PoseStack.Pose pose, VertexConsumer vc, TextureAtlasSprite sp, int color, int light,
                    float x0, float y0, float z0, float x1, float y1, float z1) {
        int a = 210, r = (color >> 16) & 255, g = (color >> 8) & 255, b = color & 255;
        float u0 = sp.getU0(), u1 = sp.getU1(), v0 = sp.getV0(), v1 = sp.getV1();
        // up
        quad(pose, vc, light, r, g, b, a, 0, 1, 0, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, u0, v0, u1, v1);
        // down
        quad(pose, vc, light, r, g, b, a, 0, -1, 0, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, u0, v0, u1, v1);
        // north
        quad(pose, vc, light, r, g, b, a, 0, 0, -1, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0, u0, v0, u1, v1);
        // south
        quad(pose, vc, light, r, g, b, a, 0, 0, 1, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1, u0, v0, u1, v1);
        // west
        quad(pose, vc, light, r, g, b, a, -1, 0, 0, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1, u0, v0, u1, v1);
        // east
        quad(pose, vc, light, r, g, b, a, 1, 0, 0, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0, u0, v0, u1, v1);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer vc, int light, int r, int g, int b, int a, float nx, float ny, float nz,
                             float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz,
                             float u0, float v0, float u1, float v1) {
        vc.addVertex(pose, ax, ay, az).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, bx, by, bz).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, cx, cy, cz).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, dx, dy, dz).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    private FluidBoxes() {}
}
