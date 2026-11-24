package com.ryancraft.client.moon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ryancraft.RyanCraft;
import com.ryancraft.entity.AngryMoonRabbit;
import com.ryancraft.entity.GreatMoonRabbit;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.rabbit.RabbitModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.rabbit.Rabbit;

/** Renderers for the Great Moon Rabbit (its own model, from the approved concept) and its minions. */
public final class MoonBossRenderers {
    static final RenderStateDataKey<Float> SWING = RenderStateDataKey.create(() -> "ryancraft:boss_swing");
    static final RenderStateDataKey<Boolean> BRUTE = RenderStateDataKey.create(() -> "ryancraft:brute");

    public static class BossState extends net.minecraft.client.renderer.entity.state.LivingEntityRenderState { public float swing; }

    /** The Great Moon Rabbit, drawn with its own model at full size (the approved concept: tools/moon_boss_model.py). */
    public static class Boss extends net.minecraft.client.renderer.entity.MobRenderer<GreatMoonRabbit, BossState, BossModel> {
        private static final Identifier TEX = RyanCraft.id("textures/entity/great_moon_rabbit.png");
        public Boss(EntityRendererProvider.Context ctx) { super(ctx, new BossModel(ctx.bakeLayer(BossModel.LAYER)), 1.6f); }
        @Override public BossState createRenderState() { return new BossState(); }
        @Override public void extractRenderState(GreatMoonRabbit r, BossState s, float p) {
            super.extractRenderState(r, s, p);
            s.swing = Math.max(0, r.swing() - p) / 10f;
        }
        @Override public Identifier getTextureLocation(BossState s) { return TEX; }
    }

    /**
     * The boss's model, from critter_models/great_moon_rabbit.json: body, head (with the ears hanging from it), two arms and
     * the Moon Mortar staff. It turns its head to look at you, its ears sway, it breathes, hops as it moves, and brings
     * the staff down overhead when it strikes.
     */
    public static class BossModel extends EntityModel<BossState> {
        public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("great_moon_rabbit"), "main");
        private final ModelPart body, head, leftEar, rightEar, rightArm, leftArm, staff;
        public BossModel(ModelPart root) {
            super(root);
            body = root.getChild("body"); head = root.getChild("head"); leftEar = head.getChild("left_ear"); rightEar = head.getChild("right_ear");
            rightArm = root.getChild("right_arm"); leftArm = root.getChild("left_arm"); staff = root.getChild("staff");
        }

        public static LayerDefinition create() {
            com.google.gson.JsonObject spec = com.ryancraft.client.model.CritterModel.spec("great_moon_rabbit");
            MeshDefinition mesh = new MeshDefinition();
            java.util.Map<String, PartDefinition> defs = new java.util.HashMap<>();
            for (com.google.gson.JsonElement el : spec.getAsJsonArray("parts")) {
                com.google.gson.JsonObject p = el.getAsJsonObject();
                CubeListBuilder cubes = CubeListBuilder.create();
                int i = 0;
                for (com.google.gson.JsonElement be : p.getAsJsonArray("boxes")) {
                    com.google.gson.JsonObject b = be.getAsJsonObject();
                    com.google.gson.JsonArray uv = b.getAsJsonArray("uv"), f = b.getAsJsonArray("from"), sz = b.getAsJsonArray("size");
                    cubes.texOffs(uv.get(0).getAsInt(), uv.get(1).getAsInt()).addBox(f.get(0).getAsFloat(), f.get(1).getAsFloat(), f.get(2).getAsFloat(),
                            sz.get(0).getAsFloat(), sz.get(1).getAsFloat(), sz.get(2).getAsFloat(),
                            new net.minecraft.client.model.geom.builders.CubeDeformation(0.004f * i++));      // no two faces exactly coplanar
                }
                com.google.gson.JsonArray pv = p.getAsJsonArray("pivot"), rot = p.getAsJsonArray("rot");
                String parent = p.has("parent") && !p.get("parent").isJsonNull() ? p.get("parent").getAsString() : null;
                PartDefinition holder = parent == null ? mesh.getRoot() : defs.get(parent);
                defs.put(p.get("name").getAsString(), holder.addOrReplaceChild(p.get("name").getAsString(), cubes, PartPose.offsetAndRotation(
                        pv.get(0).getAsFloat(), pv.get(1).getAsFloat(), pv.get(2).getAsFloat(), rot.get(0).getAsFloat(), rot.get(1).getAsFloat(), rot.get(2).getAsFloat())));
            }
            com.google.gson.JsonArray tex = spec.getAsJsonArray("texture");
            return LayerDefinition.create(mesh, tex.get(0).getAsInt(), tex.get(1).getAsInt());
        }

        @Override
        public void setupAnim(BossState s) {
            super.setupAnim(s);
            float age = s.ageInTicks;
            head.yRot = Mth.clamp(s.yRot, -50f, 50f) * Mth.DEG_TO_RAD;
            head.xRot = Mth.clamp(s.xRot, -30f, 30f) * Mth.DEG_TO_RAD;
            leftEar.zRot += Mth.sin(age * 0.07f) * 0.06f; rightEar.zRot -= Mth.sin(age * 0.07f + 1f) * 0.06f;
            leftEar.xRot += Mth.sin(age * 0.05f) * 0.04f;
            // a hop while it moves, a slow breath while it stands
            float hop = Math.abs(Mth.sin(s.walkAnimationPos * 0.45f)) * 4f * Math.min(1f, s.walkAnimationSpeed * 2f);
            float breath = Mth.sin(age * 0.08f) * 0.35f;
            for (ModelPart p : new ModelPart[]{body, head, rightArm, leftArm, staff}) p.y -= hop;
            head.y += breath; rightArm.y += breath * 0.6f; leftArm.y += breath * 0.6f; staff.y += breath * 0.6f;
            // the strike: staff and arms raised overhead and brought down
            float k = Mth.sin(s.swing * Mth.PI);
            staff.xRot -= k * 1.3f; rightArm.xRot -= k * 0.9f; leftArm.xRot -= k * 0.9f;
        }
    }

    public static class Minion extends RabbitRenderer {
        private static final Identifier TEX = RyanCraft.id("textures/entity/angry_moon_rabbit.png"), BRUTE_TEX = RyanCraft.id("textures/entity/angry_moon_rabbit_brute.png");
        public Minion(EntityRendererProvider.Context ctx) { super(ctx); }
        @Override public void extractRenderState(Rabbit r, RabbitRenderState s, float p) {
            super.extractRenderState(r, s, p);
            ((FabricRenderState) s).setData(BRUTE, r instanceof AngryMoonRabbit a && a.isBrute());
        }
        @Override public Identifier getTextureLocation(RabbitRenderState s) { return Boolean.TRUE.equals(((FabricRenderState) s).getData(BRUTE)) ? BRUTE_TEX : TEX; }
    }

    private MoonBossRenderers() {}
}
