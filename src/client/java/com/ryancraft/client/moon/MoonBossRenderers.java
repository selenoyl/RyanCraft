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

/** Renderers for the Great Moon Rabbit (a giant vanilla rabbit with its mortar staff and jade crown) and its minions. */
public final class MoonBossRenderers {
    static final RenderStateDataKey<Float> SWING = RenderStateDataKey.create(() -> "ryancraft:boss_swing");
    static final RenderStateDataKey<Boolean> BRUTE = RenderStateDataKey.create(() -> "ryancraft:brute");

    public static class Boss extends RabbitRenderer {
        private static final Identifier TEX = RyanCraft.id("textures/entity/great_moon_rabbit.png");
        public Boss(EntityRendererProvider.Context ctx) {
            super(ctx);
            ((com.ryancraft.client.mixin.LivingEntityRendererInvoker) (Object) this).ryancraft$addLayer(new GearLayer(this, ctx));
            this.shadowRadius = 0.5f;
        }
        @Override public void extractRenderState(Rabbit r, RabbitRenderState s, float p) {
            super.extractRenderState(r, s, p);
            ((FabricRenderState) s).setData(SWING, r instanceof GreatMoonRabbit g ? Math.max(0, g.swing() - p) / 10f : 0f);
        }
        @Override public Identifier getTextureLocation(RabbitRenderState s) { return TEX; }
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

    /** The mortar staff (a long haft with a stone mortar on top) held at its side, and the jade crown on its head. */
    public static class GearModel extends EntityModel<RabbitRenderState> {
        public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("great_moon_rabbit_gear"), "main");
        private final ModelPart staff;
        public GearModel(ModelPart root) { super(root); staff = root.getChild("staff"); }

        public static LayerDefinition create() {
            MeshDefinition mesh = new MeshDefinition(); PartDefinition root = mesh.getRoot();
            PartDefinition staff = root.addOrReplaceChild("staff", CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-0.5f, -9f, -0.5f, 1, 11, 1)                        // the haft
                    .texOffs(4, 0).addBox(-1.5f, -11.5f, -1.5f, 3, 3, 3)                     // the mortar bowl
                    .texOffs(4, 7).addBox(-1f, -12f, -1f, 2, 1, 2), PartPose.offset(-3.5f, 22f, -3f));
            root.addOrReplaceChild("crown", CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-2.5f, -1f, -2.5f, 5, 1, 5)                      // the band
                    .texOffs(0, 22).addBox(-2.5f, -2f, -2.5f, 1, 1, 1).addBox(1.5f, -2f, -2.5f, 1, 1, 1)
                    .addBox(-2.5f, -2f, 1.5f, 1, 1, 1).addBox(1.5f, -2f, 1.5f, 1, 1, 1), PartPose.offset(0f, 17f, -4f));
            return LayerDefinition.create(mesh, 32, 32);
        }

        @Override
        public void setupAnim(RabbitRenderState s) {
            super.setupAnim(s);
            Float sw = ((FabricRenderState) s).getData(SWING);
            float k = sw == null ? 0 : sw;
            staff.xRot = -Mth.sin(k * Mth.PI) * 1.4f;                                         // a big overhead swing
            staff.zRot = 0.15f;
        }
    }

    static class GearLayer extends RenderLayer<RabbitRenderState, RabbitModel> {
        private static final Identifier TEX = RyanCraft.id("textures/entity/great_moon_rabbit_gear.png");
        private final GearModel model;
        GearLayer(RenderLayerParent<RabbitRenderState, RabbitModel> parent, EntityRendererProvider.Context ctx) { super(parent); model = new GearModel(ctx.bakeLayer(GearModel.LAYER)); }
        @Override public void submit(PoseStack pose, SubmitNodeCollector out, int light, RabbitRenderState s, float y, float x) {
            out.submitModel(model, s, pose, model.renderType(TEX), light, LivingEntityRenderer.getOverlayCoords(s, 0f), -1, null, s.outlineColor, null);
        }
    }

    private MoonBossRenderers() {}
}
