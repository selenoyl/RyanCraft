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
    static final RenderStateDataKey<Boolean> BRUTE = RenderStateDataKey.create(() -> "ryancraft:brute");

    public static class BossState extends net.minecraft.client.renderer.entity.state.LivingEntityRenderState {
        /** The move it's making (GreatMoonRabbit.NONE..SUMMON) and how far through it, 0..1. */
        public int anim; public float progress; public boolean frenzy;
    }

    /** The Great Moon Rabbit, drawn with its own model at full size (the approved concept: tools/moon_boss_model.py). */
    public static class Boss extends net.minecraft.client.renderer.entity.MobRenderer<GreatMoonRabbit, BossState, BossModel> {
        private static final Identifier TEX = RyanCraft.id("textures/entity/great_moon_rabbit.png");
        private static final Identifier EYES = RyanCraft.id("textures/entity/great_moon_rabbit_eyes.png");
        private static final Identifier JADE = RyanCraft.id("textures/entity/great_moon_rabbit_jade.png");
        public Boss(EntityRendererProvider.Context ctx) {
            super(ctx, new BossModel(ctx.bakeLayer(BossModel.LAYER)), 1.6f);
            addLayer(new Glow(this, EYES, s -> s.frenzy && s.deathTime <= 0));      // the eclipse: blazing eyes, the halo of rays
            addLayer(new Glow(this, JADE, s -> s.anim == GreatMoonRabbit.SUMMON));  // the staff's jade top, calling the pack
        }
        @Override public BossState createRenderState() { return new BossState(); }
        @Override public void extractRenderState(GreatMoonRabbit r, BossState s, float p) {
            super.extractRenderState(r, s, p);
            s.anim = r.anim(); s.frenzy = r.frenzied();
            s.progress = s.anim == GreatMoonRabbit.NONE ? 0f
                    : Mth.clamp(1f - (r.animTicks() - p) / GreatMoonRabbit.DURATION[s.anim], 0f, 1f);
        }
        @Override public Identifier getTextureLocation(BossState s) { return TEX; }
        /** In the frenzy its moon-dust fur goes dark, like the moon in an eclipse. */
        @Override protected int getModelTint(BossState s) { return s.frenzy ? 0xFF8C78B4 : super.getModelTint(s); }

        /** Not vanilla's tip onto its side: it staggers (the model then topples it over backwards, see BossModel.die). */
        @Override protected void setupRotations(BossState s, PoseStack pose, float bodyRot, float scale) {
            float dt = s.deathTime;
            s.deathTime = 0; super.setupRotations(s, pose, bodyRot, scale); s.deathTime = dt;
            if (dt > 0) pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(Mth.sin(dt * 0.9f) * 7f * Math.max(0f, 1f - dt / 16f)));
        }
    }

    /** Draws the model again, full-bright, with a glow sheet (transparent but for what glows), while the test holds. */
    static class Glow extends RenderLayer<BossState, BossModel> {
        private final net.minecraft.client.renderer.rendertype.RenderType type;
        private final java.util.function.Predicate<BossState> when;
        Glow(RenderLayerParent<BossState, BossModel> parent, Identifier tex, java.util.function.Predicate<BossState> when) {
            super(parent); type = net.minecraft.client.renderer.rendertype.RenderTypes.eyes(tex); this.when = when;
        }
        @Override public void submit(PoseStack pose, SubmitNodeCollector out, int light, BossState s, float yRot, float xRot) {
            if (s.isInvisible || !when.test(s)) return;
            out.submitModel(getParentModel(), s, pose, type, 0xF000F0, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, -1, null, s.outlineColor, null);
        }
    }

    /**
     * The boss's model, from critter_models/great_moon_rabbit.json: body, head (with the crown, the ears and the frenzy's
     * halo on it), two arms and the Moon Mortar staff. It turns its head to look at you, its ears sway, it breathes, hops
     * as it moves, and each move has its own keyframed pose: the overhead strike, the slam (wound up high, crouched, then
     * crashed down), the meteor bat (a sideways swing), the summon (staff held up, ears straight). Dying, the staff
     * falls from its paws and the crown tumbles off.
     */
    public static class BossModel extends EntityModel<BossState> {
        public static final ModelLayerLocation LAYER = new ModelLayerLocation(RyanCraft.id("great_moon_rabbit"), "main");
        private final ModelPart body, head, crown, halo, leftEar, rightEar, rightArm, leftArm, staff;
        public BossModel(ModelPart root) {
            super(root);
            body = root.getChild("body"); head = root.getChild("head"); crown = head.getChild("crown"); halo = head.getChild("halo");
            leftEar = head.getChild("left_ear"); rightEar = head.getChild("right_ear");
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

        // A move's pose, as offsets from the resting pose, keyed through the move:
        //   {t, armX, armY, staffX, staffY, staffZ, crouch, headX, earsUp, staff sideways (px, + is its left)}
        // The wind-ups raise the paws beside its right cheek with the staff's top up, back and out over the right shoulder,
        // so the shaft clears the big head.
        private static final float[][] STRIKE = {
                {0f, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0.35f, -1.1f, 0, -0.76f, 0, 0.04f, 0, -0.1f, 0, -9f},
                {0.6f, 0.3f, 0, 1.2f, 0, 0, 1, 0.1f, 0, 0}, {1f, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
        private static final float[][] SLAM = {                // impact at 14/24 = 0.583, when the server lands it
                {0f, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0.42f, -1.25f, 0, -0.8f, 0, 0.04f, 2.5f, -0.35f, 0.5f, -9f},
                {0.5f, -1.3f, 0, -0.85f, 0, 0.04f, 3f, -0.4f, 0.5f, -9.5f}, {0.585f, 0.35f, 0, 1.8f, 0, 0.55f, 4f, 0.25f, 0, 0},
                {0.75f, 0.35f, 0, 1.8f, 0, 0.55f, 3.5f, 0.2f, 0, 0}, {1f, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
        private static final float[][] BAT = {                 // contact at 6/12 = 0.5, when the meteor flies
                {0f, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0.32f, -0.2f, 0.7f, 1.1f, 1.5f, 0.55f, 1, 0, 0, 0},
                {0.5f, -0.3f, 0f, 1.1f, 0f, 0.55f, 1, 0, 0, 0}, {0.68f, -0.4f, -0.8f, 1.0f, -1.7f, 0.55f, 0.5f, 0, 0, 0},
                {1f, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
        private static final float[][] SUMMON = {
                {0f, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0.28f, -1.45f, 0, -0.55f, 0, 0.55f, 0, -0.4f, 1, 0},
                {0.8f, -1.5f, 0, -0.6f, 0, 0.55f, 0, -0.45f, 1, 0}, {1f, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

        private static float[] key(float[][] keys, float t) {
            for (int i = 1; i < keys.length; i++) {
                if (t <= keys[i][0]) {
                    float[] a = keys[i - 1], b = keys[i];
                    float u = (t - a[0]) / (b[0] - a[0]); u = u * u * (3 - 2 * u);
                    float[] out = new float[a.length];
                    for (int j = 1; j < a.length; j++) out[j] = Mth.lerp(u, a[j], b[j]);
                    return out;
                }
            }
            return new float[keys[0].length];
        }

        /** Where the paws hold the staff, for an arm swung to xRot: the staff goes where the paws go. */
        private static float handY(float armX) { return 12f * Mth.cos(armX); }
        private static float handZ(float armX) { return 12f * Mth.sin(armX); }

        @Override
        public void setupAnim(BossState s) {
            super.setupAnim(s);
            float age = s.ageInTicks;
            head.yRot = Mth.clamp(s.yRot, -50f, 50f) * Mth.DEG_TO_RAD;
            head.xRot = Mth.clamp(s.xRot, -30f, 30f) * Mth.DEG_TO_RAD;
            leftEar.zRot += Mth.sin(age * 0.07f) * 0.06f; rightEar.zRot -= Mth.sin(age * 0.07f + 1f) * 0.06f;
            leftEar.xRot += Mth.sin(age * 0.05f) * 0.04f;
            // the move's pose
            float[] k = switch (s.anim) {
                case GreatMoonRabbit.SWING -> key(STRIKE, s.progress);
                case GreatMoonRabbit.SLAM -> key(SLAM, s.progress);
                case GreatMoonRabbit.BAT -> key(BAT, s.progress);
                case GreatMoonRabbit.SUMMON -> key(SUMMON, s.progress);
                default -> null;
            };
            if (s.deathTime > 0) k = null;
            if (k != null) {
                float armRest = rightArm.xRot;
                rightArm.xRot += k[1]; leftArm.xRot += k[1]; rightArm.yRot += k[2]; leftArm.yRot += k[2];
                staff.xRot += k[3]; staff.yRot += k[4]; staff.zRot += k[5];
                staff.y += handY(rightArm.xRot) - handY(armRest); staff.z += handZ(rightArm.xRot) - handZ(armRest);
                staff.x += k[9] - k[2] * 6f;
                rightArm.zRot += k[9] * 0.03f; leftArm.zRot += k[9] * 0.03f;            // both paws over to that side
                for (ModelPart p : new ModelPart[]{body, head, rightArm, leftArm, staff}) p.y += k[6];
                head.xRot += k[7];
                leftEar.xRot -= leftEar.xRot * k[8]; leftEar.zRot -= leftEar.zRot * k[8];        // ears pricked straight up
                rightEar.xRot -= rightEar.xRot * k[8]; rightEar.zRot -= rightEar.zRot * k[8];
                if (s.anim == GreatMoonRabbit.SUMMON) staff.zRot += Mth.sin(age * 1.3f) * 0.05f * k[8];  // the staff trembles with power
            }
            // a hop while it moves, a slow breath while it stands
            float hop = s.deathTime > 0 ? 0 : Math.abs(Mth.sin(s.walkAnimationPos * 0.45f)) * 4f * Math.min(1f, s.walkAnimationSpeed * 2f);
            float breath = s.deathTime > 0 ? 0 : Mth.sin(age * 0.08f) * 0.35f;
            for (ModelPart p : new ModelPart[]{body, head, rightArm, leftArm, staff}) p.y -= hop;
            head.y += breath; rightArm.y += breath * 0.6f; leftArm.y += breath * 0.6f; staff.y += breath * 0.6f;
            // the eclipse halo, turning slowly behind its head
            halo.visible = s.frenzy && s.deathTime <= 0;
            halo.zRot = age * 0.03f;
            float scale = 1f + Mth.sin(age * 0.15f) * 0.06f; halo.xScale = scale; halo.yScale = scale;
            if (s.deathTime > 0) die(s.deathTime);
        }

        /**
         * Dying: the paws let go and the staff drops and lies flat; then it topples over backwards, about its feet, and the
         * crown tumbles off behind its head to land upright on the floor. (The fall is done here, not by turning the whole
         * render, so the dropped staff stays on the floor.)
         */
        private void die(float dt) {
            float let = Math.min(dt / 6f, 1f);
            rightArm.xRot = Mth.lerp(let, rightArm.xRot, -0.15f); leftArm.xRot = Mth.lerp(let, leftArm.xRot, -0.15f);
            rightArm.zRot += 0.35f * let; leftArm.zRot -= 0.35f * let;
            head.yRot *= 1f - let;
            float drop = Mth.clamp(dt / 12f, 0f, 1f), g = drop * drop;              // falling under the moon's gravity
            staff.x += 10f * drop; staff.y += 22f * g; staff.z -= 4f * drop;
            staff.zRot = Mth.lerp(g, staff.zRot, Mth.HALF_PI); staff.xRot = Mth.lerp(g, staff.xRot, 0f); staff.yRot = Mth.lerp(g, staff.yRot, 0.3f);
            float fall = Mth.clamp((dt - 10f) / 14f, 0f, 1f); fall *= fall;          // gathering speed as it goes over
            head.xRot = Mth.lerp(fall, head.xRot, -0.45f);                          // chin up, the head lolling back
            float tilt = -1.54f * fall;
            for (ModelPart p : new ModelPart[]{body, head, rightArm, leftArm}) fallBack(p, tilt, 9f * fall);
            float c = Mth.clamp((dt - 12f) / 20f, 0f, 1f);
            crown.y -= 9f * c + 7f * Mth.sin(c * Mth.PI);                           // up off its head (head-up is now "behind")...
            crown.z += 7f * c;                                                      // ...and down onto the floor
            crown.xRot += c * (Mth.TWO_PI + 2f);                                    // a full tumble, landing upright
        }

        /** Turns a top-level part about the feet (model y 24), raised by lift so its back rests on the floor, not in it. */
        private static void fallBack(ModelPart p, float angle, float lift) {
            float y = p.y - 24f, z = p.z, c = Mth.cos(angle), s = Mth.sin(angle);
            p.y = 24f + y * c - z * s - lift; p.z = y * s + z * c;
            org.joml.Vector3f e = new org.joml.Quaternionf().rotationX(angle).mul(new org.joml.Quaternionf().rotationZYX(p.zRot, p.yRot, p.xRot))
                    .getEulerAnglesZYX(new org.joml.Vector3f());
            p.xRot = e.x; p.yRot = e.y; p.zRot = e.z;
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
