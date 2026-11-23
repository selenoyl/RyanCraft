package com.ryancraft.client.screen;

import com.ryancraft.food.CampfireMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * The campfire's fuel menu: vanilla's furnace background with only the fuel slot and its flame, moved to the middle.
 * The flame burns down as the current piece of fuel does; hover it to see the time left.
 */
public class CampfireScreen extends AbstractContainerScreen<CampfireMenu> {
    private static final Identifier BG = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier FLAME = Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final int PANEL = 0xFFC6C6C6;

    public CampfireScreen(CampfireMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        int x = leftPos, y = topPos;
        if (mx >= x + 80 && mx < x + 94 && my >= y + 36 && my < y + 50) {
            int s = menu.burnSeconds();
            g.setTooltipForNextFrame(font, s > 0 ? Component.translatable("container.ryancraft.campfire.time_left", s / 60 + ":" + String.format("%02d", s % 60))
                    : Component.translatable("container.ryancraft.campfire.no_fire"), mx, my);
        }
        this.renderTooltip(g, mx, my);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = leftPos, y = topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, BG, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        g.fill(x + 55, y + 16, x + 73, y + 34, PANEL);          // no input slot,
        g.fill(x + 55, y + 35, x + 73, y + 70, PANEL);          // the flame and fuel slot move to the middle,
        g.fill(x + 78, y + 33, x + 104, y + 51, PANEL);         // no arrow,
        g.fill(x + 110, y + 29, x + 138, y + 57, PANEL);        // no output
        g.blit(RenderPipelines.GUI_TEXTURED, BG, x + 79, y + 52, 55, 52, 18, 18, 256, 256);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, x + 80, y + 36, 56, 36, 14, 14, 256, 256);
        int total = menu.totalSeconds(), left = menu.burnSeconds();
        if (total > 0 && left > 0) {
            int h = Math.max(1, (int) Math.ceil(13f * left / total)) + 1;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, FLAME, 14, 14, 0, 14 - h, x + 80, y + 36 + 14 - h, 14, h);
        }
    }
}
