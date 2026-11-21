package com.ryancraft.client.model;

import com.ryancraft.client.render.DeerRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Skull-on-a-plank (critter_models/deer_mount.json) wearing the same antler racks as the live deer. */
public class DeerMountModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER = CritterModel.layer("deer_skull_mount");
    private final AntlerSet antlers;

    public DeerMountModel(ModelPart root) {
        super(root);
        this.antlers = new AntlerSet(root.getChild("skull"));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = CritterModel.createMesh("deer_mount");
        AntlerSet.addTo(mesh.getRoot().getChild("skull"));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        if (state instanceof DeerRenderState d) antlers.show(d.antlers, d.dropTine, d.rack);
    }
}
