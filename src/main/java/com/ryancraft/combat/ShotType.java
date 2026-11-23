package com.ryancraft.combat;

/**
 * What a cannon can fire. speed in blocks per tick; damage to a mob hit directly; blast is the explosion on impact (0 for
 * none); pellets &gt; 0 bursts into that many lead balls spread over spread degrees.
 */
public enum ShotType {
    ROUND("cannonball", 3.0f, 20f, 2.2f, false, 0, 0),
    HEATED("heated_shot", 3.0f, 20f, 1.4f, true, 0, 0),
    CHAIN("chain_shot", 2.4f, 16f, 0f, false, 0, 0),
    GRAPE("grapeshot", 2.3f, 0f, 0f, false, 9, 7f),
    CANISTER("canister_shot", 2.1f, 0f, 0f, false, 16, 13f),
    SHELL("explosive_shell", 2.6f, 12f, 3.2f, false, 0, 0),
    PELLET("musket_ball", 2.3f, 6f, 0f, false, 0, 0);

    public final String item; public final float speed, damage, blast, spread; public final boolean fire; public final int pellets;
    ShotType(String item, float speed, float damage, float blast, boolean fire, int pellets, float spread) {
        this.item = item; this.speed = speed; this.damage = damage; this.blast = blast; this.fire = fire; this.pellets = pellets; this.spread = spread;
    }
}
