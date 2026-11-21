package com.ryancraft.client.ambience;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** A looping ambience layer that fades smoothly toward a target volume, optionally anchored in the world. */
class AmbientLoop extends AbstractTickableSoundInstance {
    private static final float FADE_PER_TICK = 0.012f;   // ~3-4 s full fade, MAtmos-style crossfades
    private float target;
    private Vec3 targetPos;

    AmbientLoop(SoundEvent event, Vec3 anchor) {
        super(event, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.01f;
        if (anchor == null) {
            this.relative = true;
            this.attenuation = Attenuation.NONE;
        } else {
            this.attenuation = Attenuation.LINEAR;
            this.x = anchor.x; this.y = anchor.y; this.z = anchor.z;
            this.targetPos = anchor;
        }
    }

    void setTarget(float target, Vec3 pos) {
        this.target = target;
        if (pos != null) this.targetPos = pos;
    }

    float target() { return target; }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        float d = target - volume;
        volume += Math.max(-FADE_PER_TICK, Math.min(FADE_PER_TICK, d));
        if (!relative && targetPos != null) {    // glide toward where the water etc. is
            x += (targetPos.x - x) * 0.05; y += (targetPos.y - y) * 0.05; z += (targetPos.z - z) * 0.05;
        }
        if (target <= 0.0f && volume <= 0.005f) {
            stop();
        }
    }
}
