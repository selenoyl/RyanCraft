package com.ryancraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ryancraft.RyanCraft;
import com.ryancraft.client.model.CritterModel;
import com.ryancraft.entity.CritterVisual;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/** Renders every spec-driven critter: picks the texture by variant and handles climbing, playing dead and fish flopping. */
public class CritterRenderer<T extends Mob & CritterVisual> extends MobRenderer<T, CritterRenderState, CritterModel> {
    private final Identifier[] textures;
    private final float scale;
    private final boolean fish;
    private boolean plainBabies;

    public CritterRenderer(EntityRendererProvider.Context ctx, String model, float shadow, float scale, boolean fish, String... textures) {
        super(ctx, new CritterModel(ctx.bakeLayer(CritterModel.layer(model)), model), shadow * scale);
        this.scale = scale;
        this.fish = fish;
        this.textures = new Identifier[textures.length];
        for (int i = 0; i < textures.length; i++) this.textures[i] = RyanCraft.id("textures/entity/" + textures[i] + ".png");
    }

    /** Babies always use the first texture (so young bulls don't sprout antlers). */
    public CritterRenderer<T> plainBabies() { this.plainBabies = true; return this; }

    @Override
    public CritterRenderState createRenderState() {
        return new CritterRenderState();
    }

    @Override
    public void extractRenderState(T entity, CritterRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.variant = entity.getVariant();
        state.texture = entity.textureIndex();
        state.antlers = entity.isBaby() ? -1 : entity.antlerStage();
        state.rack = entity.antlerRack();
        state.flying = entity.isFlyingVisual();
        state.sitting = entity.isSittingVisual();
        state.rising = state.flying && entity.getY() - entity.yo > 0.015;
        state.climbing = entity.isClimbingVisual();
        state.playingDead = entity.isPlayingDead();
        if (state.playingDead) {
            state.deathTime = 21.0f;          // reuse the vanilla "keel over" pose
            state.hasRedOverlay = false;
        }
    }

    @Override
    public Identifier getTextureLocation(CritterRenderState state) {
        if (plainBabies && state.isBaby) return textures[0];
        return textures[Math.floorMod(state.texture, textures.length)];
    }

    @Override
    protected void scale(CritterRenderState state, PoseStack pose) {
        float s = state.isBaby ? scale * 0.5f : scale;
        pose.scale(s, s, s);
    }

    @Override
    protected void setupRotations(CritterRenderState state, PoseStack pose, float bodyRot, float entityScale) {
        super.setupRotations(state, pose, bodyRot, entityScale);
        if (fish) {
            float wobble = (state.isInWater ? 1.0f : 1.7f) * 4.3f * Mth.sin(0.6f * state.ageInTicks);
            pose.mulPose(Axis.YP.rotationDegrees(wobble));
            if (!state.isInWater) {
                pose.translate(0.1f, 0.1f, -0.1f);
                pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
            }
        } else if (state.climbing && !state.playingDead) {
            pose.mulPose(Axis.XP.rotationDegrees(-75.0f));
            pose.translate(0.0f, -0.15f, 0.0f);
        }
    }
}
