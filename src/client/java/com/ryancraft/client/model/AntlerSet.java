package com.ryancraft.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deer antler racks from critter_models/deer_antlers.json (tools/critters.py), shared by the live deer
 * and the skull mount so a mounted rack always matches the animal it came from.
 */
public final class AntlerSet {
    public static final String[] RARE = {null, "cactus", "palmated", "lopsided"};
    private final Map<String, List<ModelPart>> groups = new HashMap<>();

    private static JsonObject groupsJson() {
        return CritterModel.spec("deer_antlers").getAsJsonObject("groups");
    }

    /** Adds every rack as hidden-by-default children of the head (or skull). */
    public static void addTo(PartDefinition head) {
        JsonObject groups = groupsJson();
        for (String key : groups.keySet()) {
            for (JsonElement el : groups.getAsJsonArray(key)) {
                JsonObject p = el.getAsJsonObject();
                CubeListBuilder cubes = CubeListBuilder.create();
                for (JsonElement be : p.getAsJsonArray("boxes")) {
                    JsonObject b = be.getAsJsonObject();
                    var uv = b.getAsJsonArray("uv"); var f = b.getAsJsonArray("from"); var sz = b.getAsJsonArray("size");
                    cubes.texOffs(uv.get(0).getAsInt(), uv.get(1).getAsInt()).mirror(b.get("mirror").getAsBoolean())
                            .addBox(f.get(0).getAsFloat(), f.get(1).getAsFloat(), f.get(2).getAsFloat(),
                                    sz.get(0).getAsFloat(), sz.get(1).getAsFloat(), sz.get(2).getAsFloat());
                }
                var pv = p.getAsJsonArray("pivot"); var r = p.getAsJsonArray("rot");
                head.addOrReplaceChild(p.get("name").getAsString(), cubes, PartPose.offsetAndRotation(
                        pv.get(0).getAsFloat(), pv.get(1).getAsFloat(), pv.get(2).getAsFloat(),
                        r.get(0).getAsFloat(), r.get(1).getAsFloat(), r.get(2).getAsFloat()));
            }
        }
    }

    public AntlerSet(ModelPart head) {
        JsonObject json = groupsJson();
        for (String key : json.keySet()) {
            List<ModelPart> parts = new ArrayList<>();
            for (JsonElement el : json.getAsJsonArray(key)) parts.add(head.getChild(el.getAsJsonObject().get("name").getAsString()));
            groups.put(key, parts);
        }
    }

    /** stage -1 shows nothing (fawns); a rare rack replaces a stage-4 rack. */
    public void show(int stage, boolean dropTine, int rack) {
        String main = stage < 0 ? null : (stage >= 4 && rack > 0 && rack < RARE.length) ? RARE[rack] : String.valueOf(stage);
        String drop = (main != null && dropTine && stage >= 3 && (rack == 0)) ? "drop" + stage : null;
        for (var e : groups.entrySet()) {
            boolean visible = e.getKey().equals(main) || e.getKey().equals(drop);
            for (ModelPart part : e.getValue()) part.visible = visible;
        }
    }
}
