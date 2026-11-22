package com.ryancraft.client;

import com.ryancraft.RyanCraft;
import com.ryancraft.thirst.Thirst;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** The thirst bar (a row of droplets over the hunger bar), drinking from water with an empty hand, no sprinting when parched. */
public final class ThirstHud {
    private static final Identifier ID = RyanCraft.id("thirst_bar");
    private static final Identifier FULL = RyanCraft.id("hud/thirst_full"), HALF = RyanCraft.id("hud/thirst_half"), EMPTY = RyanCraft.id("hud/thirst_empty");
    private static int sipCooldown;

    private static boolean shows(Player p) {
        Minecraft mc = Minecraft.getInstance();
        return p != null && mc.gameMode != null && mc.gameMode.canHurtPlayer() && p.getVehicle() == null && p.hasAttached(Thirst.DATA);
    }

    public static void init() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, ID, (g, tick) -> {
            Minecraft mc = Minecraft.getInstance();
            Player p = mc.player;
            if (!shows(p) || mc.options.hideGui) return;
            int level = p.getAttached(Thirst.DATA).level();
            int right = g.guiWidth() / 2 + 91, y = g.guiHeight() - 49;
            long t = mc.gui.getGuiTicks();
            for (int i = 0; i < 10; i++) {
                int x = right - i * 8 - 9, dy = 0;
                if (level <= 4 && ((t + i * 3) / 2) % 9 == 0) dy = (i % 2 == 0) ? -1 : 1;   // a parched bar trembles, like hunger
                g.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY, x, y + dy, 9, 9);
                if (i * 2 + 1 < level) g.blitSprite(RenderPipelines.GUI_TEXTURED, FULL, x, y + dy, 9, 9);
                else if (i * 2 + 1 == level) g.blitSprite(RenderPipelines.GUI_TEXTURED, HALF, x, y + dy, 9, 9);
            }
        });
        HudStatusBarHeightRegistry.addRight(ID, p -> shows(p) ? 10 : 0);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            Player p = mc.player;
            if (sipCooldown > 0) sipCooldown--;
            if (p == null || !p.hasAttached(Thirst.DATA)) return;
            int level = p.getAttached(Thirst.DATA).level();
            if (level <= 6 && p.isSprinting() && !p.getAbilities().mayfly) p.setSprinting(false);      // too parched to sprint
            // drink from water: use with an empty hand while looking at it
            if (mc.screen == null && sipCooldown == 0 && level < Thirst.MAX && mc.options.keyUse.isDown() && p.getMainHandItem().isEmpty()) {
                HitResult hit = p.pick(p.blockInteractionRange(), 0f, true);
                if (hit instanceof BlockHitResult bh && hit.getType() == HitResult.Type.BLOCK && p.level().getFluidState(bh.getBlockPos()).is(FluidTags.WATER)) {
                    ClientPlayNetworking.send(new Thirst.Drink());
                    sipCooldown = 12;
                }
            }
        });
    }

    private ThirstHud() {}
}
