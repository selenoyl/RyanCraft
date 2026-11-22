package com.ryancraft.client.pipes;

import com.ryancraft.pipes.EnderTerminalMenu;
import com.ryancraft.pipes.TerminalPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Ender Terminal: four rows showing everything on the pipe network (scroll with the wheel, type to search), then
 * the two rows where your requests arrive. Left-click an item for a stack, right-click for one. Click the grid while
 * holding something to send it off into storage; shift-click items in your inventory to do the same.
 */
public class EnderTerminalScreen extends AbstractContainerScreen<EnderTerminalMenu> {
    private static final Identifier BG = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int ROWS = 4, GRID_Y = 18;
    private EditBox search;
    private int scroll;

    public EnderTerminalScreen(EnderTerminalMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 222; this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        search = new EditBox(font, leftPos + 82, topPos + 4, 86, 12, Component.translatable("gui.ryancraft.terminal.search"));
        search.setBordered(true);
        addRenderableWidget(search);
    }

    private List<Integer> visible() {
        String q = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < menu.shown.size(); i++)
            if (q.isEmpty() || menu.shown.get(i).getHoverName().getString().toLowerCase(Locale.ROOT).contains(q)) out.add(i);
        return out;
    }

    private int cellAt(double mx, double my) {
        int cx = (int) (mx - leftPos - 8), cy = (int) (my - topPos - GRID_Y);
        if (cx < 0 || cy < 0 || cx >= 9 * 18 || cy >= ROWS * 18) return -1;
        return (scroll + cy / 18) * 9 + cx / 18;
    }

    private static String shortCount(long n) {
        if (n < 1000) return Long.toString(n);
        if (n < 1_000_000) return String.format(Locale.ROOT, n < 10_000 ? "%.1fk" : "%.0fk", n / 1000.0);
        return String.format(Locale.ROOT, "%.1fm", n / 1_000_000.0);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        List<Integer> vis = visible();
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < 9; c++) {
            int k = (scroll + r) * 9 + c;
            if (k >= vis.size()) continue;
            int i = vis.get(k), x = leftPos + 8 + c * 18, y = topPos + GRID_Y + r * 18;
            ItemStack s = menu.shown.get(i);
            g.renderItem(s, x, y);
            g.renderItemDecorations(font, s, x, y, shortCount(menu.counts.get(i)));
        }
        int hover = cellAt(mx, my);
        if (hover >= 0 && hover < vis.size()) {
            int i = vis.get(hover);
            g.setTooltipForNextFrame(font, menu.shown.get(i), mx, my);
        }
        this.renderTooltip(g, mx, my);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = leftPos, y = topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, BG, x, y, 0, 0, imageWidth, 6 * 18 + 17, 256, 256);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, x, y + 6 * 18 + 17, 0, 126, imageWidth, 96, 256, 256);
        g.fill(x + 7, y + GRID_Y - 1, x + 169, y + GRID_Y + ROWS * 18 - 1, 0x40301050);     // ender tint over the network view
        g.fill(x + 7, y + GRID_Y + ROWS * 18, x + 169, y + GRID_Y + ROWS * 18 + 1, 0xFF6A3A9A);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        int rows = (visible().size() + 8) / 9;
        scroll = Math.max(0, Math.min(Math.max(0, rows - ROWS), scroll - (int) Math.signum(dy)));
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        int cell = cellAt(e.x(), e.y());
        if (cell >= 0) {
            if (!menu.getCarried().isEmpty()) { ClientPlayNetworking.send(new TerminalPayloads.DepositCarried()); return true; }
            List<Integer> vis = visible();
            if (cell < vis.size()) {
                ItemStack s = menu.shown.get(vis.get(cell));
                ClientPlayNetworking.send(new TerminalPayloads.Request(s.copyWithCount(1), e.button() == 1 ? 1 : s.getMaxStackSize()));
            }
            return true;
        }
        return super.mouseClicked(e, doubleClick);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent e) {
        if (search != null && search.isFocused() && e.key() != 256) { search.keyPressed(e); scroll = 0; return true; }
        return super.keyPressed(e);
    }
}
