package com.ryancraft.client.sky;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.util.Mth;

/**
 * The pegasus's wings: two feathered plates hinged at the top of the horse's shoulders. On the ground they fold back
 * along the flanks; in the air they spread, beating while it rises and holding a slight lift while it glides.
 */
public class PegasusWingsModel extends EntityModel<HorseRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("pegasus_wings"), "main");
    public static final ModelLayerLocation BABY_LAYER = new ModelLayerLocation(RyanCraft.id("pegasus_wings_baby"), "main");
    public static final RenderStateDataKey<Float> SPREAD = RenderStateDataKey.create(() -> "ryancraft:pegasus_spread");
    public static final RenderStateDataKey<Boolean> RISING = RenderStateDataKey.create(() -> "ryancraft:pegasus_rising");
    private static final float DEG = Mth.DEG_TO_RAD;
    private final ModelPart body, right, left;

    public PegasusWingsModel(ModelPart root) {
        super(root);
        body = root.getChild("body"); right = body.getChild("right_wing"); left = body.getChild("left_wing");
    }

    public static LayerDefinition create() {
        MeshDefinition mesh = new MeshDefinition(); PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0f, 11f, 5f));   // the horse body's pivot
        body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 0).addBox(-18f, -0.5f, 0f, 18f, 1f, 11f), PartPose.offset(-4.5f, -8f, -12f));
        body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 12).addBox(0f, -0.5f, 0f, 18f, 1f, 11f), PartPose.offset(4.5f, -8f, -12f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createBaby() { return create().apply(Baby.transformer()); }

    @Override
    public void setupAnim(HorseRenderState s) {
        super.setupAnim(s);
        Float sp = ((FabricRenderState) s).getData(SPREAD); boolean rising = Boolean.TRUE.equals(((FabricRenderState) s).getData(RISING));
        float spread = sp == null ? 0f : sp, fold = 1f - spread;
        body.xRot = s.standAnimation * -45f * DEG;                     // follow the body when it rears
        float beat = rising ? Mth.sin(s.ageInTicks * 0.9f) * 35f : Mth.sin(s.ageInTicks * 0.15f) * 4f;
        float lift = spread * (12f + beat) + fold * -18f;              // spread: a little above level; folded: drooping
        float sweep = spread * 12f + fold * 82f;                       // folded: swept right back along the flank
        right.yRot = sweep * DEG; right.zRot = lift * DEG;
        left.yRot = -sweep * DEG; left.zRot = -lift * DEG;
    }

    /** Reaches vanilla's foal transformer, so the wings shrink with the foal's body. */
    abstract static class Baby extends AbstractEquineModel<EquineRenderState> {
        private Baby(ModelPart p) { super(p); }
        static MeshTransformer transformer() { return BABY_TRANSFORMER; }
    }
}
