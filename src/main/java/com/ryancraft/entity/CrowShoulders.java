package com.ryancraft.entity;

/** Which of a player's shoulders has a crow on it (bit 0 left, bit 1 right), synced so clients can draw it. */
public interface CrowShoulders {
    byte ryancraft$crowShoulders();
    void ryancraft$setCrowShoulders(byte bits);
}
