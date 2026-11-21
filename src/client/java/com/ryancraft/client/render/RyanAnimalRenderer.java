package com.ryancraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ryancraft.RyanCraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** One renderer for all three animals: one model, one texture, babies drawn at half size. */
public class RyanAnimalRenderer<T extends Mob> extends MobRenderer<T, LivingEntityRenderState, EntityModel<LivingEntityRenderState>> {
    private final Identifier texture;

    public RyanAnimalRenderer(EntityRendererProvider.Context ctx, EntityModel<LivingEntityRenderState> model, float shadow, String name) {
        super(ctx, model, shadow);
        this.texture = RyanCraft.id("textures/entity/" + name + ".png");
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return texture;
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }
}
