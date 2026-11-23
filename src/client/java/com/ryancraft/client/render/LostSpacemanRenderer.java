package com.ryancraft.client.render;

import com.ryancraft.RyanCraft;
import com.ryancraft.entity.LostSpaceman;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Zombie;

/** The vanilla zombie, in a lost spaceman's suit (one skin per suit colour). */
public class LostSpacemanRenderer extends ZombieRenderer {
    private static final RenderStateDataKey<Integer> SUIT = RenderStateDataKey.create(() -> "ryancraft:suit");
    private static final Identifier[] SKINS = new Identifier[LostSpaceman.SUITS.length];
    static { for (int i = 0; i < SKINS.length; i++) SKINS[i] = RyanCraft.id("textures/entity/lost_spaceman/" + LostSpaceman.SUITS[i] + ".png"); }

    public LostSpacemanRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override
    public void extractRenderState(Zombie z, ZombieRenderState s, float partial) {
        super.extractRenderState(z, s, partial);
        ((FabricRenderState) s).setData(SUIT, z instanceof LostSpaceman l ? l.suit() : 0);
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState s) {
        Integer i = ((FabricRenderState) s).getData(SUIT);
        return SKINS[i == null ? 0 : Math.floorMod(i, SKINS.length)];
    }
}
