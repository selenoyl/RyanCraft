package com.ryancraft.client.pipes;

import com.ryancraft.pipes.DiamondPipeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Six filter rows, colour-coded like the old diamond pipe: down black, up white, north red, south blue, west green, east yellow. */
public class DiamondPipeScreen extends AbstractContainerScreen<DiamondPipeMenu> {
    private static final Identifier BG = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int[] ROW = {0xFF202020, 0xFFF0F0F0, 0xFFC03030, 0xFF3050C0, 0xFF30A040, 0xFFE0C030};

    public DiamondPipeScreen(DiamondPipeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 222; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override public void render(GuiGraphics g, int mx, int my, float pt) { super.render(g, mx, my, pt); this.renderTooltip(g, mx, my); }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = (width - imageWidth) / 2, y = (height - imageHeight) / 2;
        g.blit(RenderPipelines.GUI_TEXTURED, BG, x, y, 0, 0, imageWidth, 6 * 18 + 17, 256, 256);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, x, y + 6 * 18 + 17, 0, 126, imageWidth, 96, 256, 256);
        for (int r = 0; r < 6; r++) g.fill(x + 3, y + 18 + r * 18, x + 6, y + 34 + r * 18, ROW[r]);
    }
}
