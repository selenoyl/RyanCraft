package com.ryancraft.client.screen;

import com.ryancraft.storage.PotMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** The clay pot's five slots, drawn on the hopper's background. */
public class PotScreen extends AbstractContainerScreen<PotMenu> {
    private static final Identifier BG = Identifier.withDefaultNamespace("textures/gui/container/hopper.png");

    public PotScreen(PotMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        this.renderTooltip(g, mx, my);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = (this.width - this.imageWidth) / 2, y = (this.height - this.imageHeight) / 2;
        g.blit(RenderPipelines.GUI_TEXTURED, BG, x, y, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
    }
}
