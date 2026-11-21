package com.ryancraft.bag;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Remembers who lives in the cage and walks them around inside it: short trots to a random spot, pauses to look
 * around, and for birds, hops up onto the perch and back down. The resident's own AI is switched off while caged.
 */
public class CageBlockEntity extends BlockEntity {
    private UUID occupant;
    private int missing, timer, hop;
    private Vec3 target;
    private double restY, fromY;

    public CageBlockEntity(BlockPos pos, BlockState state) { super(Cages.CAGE_ENTITY, pos, state); }

    public boolean occupied() { return occupant != null; }
    public UUID occupant() { return occupant; }
    public void setOccupant(UUID id) { occupant = id; missing = 0; target = null; hop = 0; restY = -1; setChanged(); }
    public void clear() { occupant = null; setChanged(); }

    public Mob resident() {
        if (occupant == null || !(level instanceof ServerLevel sl)) return null;
        return sl.getEntity(occupant) instanceof Mob m && m.isAlive() ? m : null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CageBlockEntity be) {
        if (be.occupant == null || !(state.getBlock() instanceof CageBlock cage)) return;
        Entity e = ((ServerLevel) level).getEntity(be.occupant);
        if (!(e instanceof Mob m) || !m.isAlive() || !m.getTags().contains(Cages.CAGED_TAG)) {
            // Gone (caught, or the cage was emptied); allow a moment after loading for the mob to load in too
            if (++be.missing > 40) be.clear();
            return;
        }
        be.missing = 0;
        var r = level.getRandom();
        double hw = m.getBbWidth() / 2 + 0.02;
        double minX = pos.getX() + cage.inset / 16 + hw, maxX = pos.getX() + 1 - cage.inset / 16 - hw;
        double minZ = pos.getZ() + cage.inset / 16 + hw, maxZ = pos.getZ() + 1 - cage.inset / 16 - hw;
        double floorY = pos.getY() + cage.floor / 16;
        double topY = pos.getY() + cage.ceiling / 16 - m.getBbHeight() - 0.01;
        if (be.restY < 0) be.restY = floorY;

        double x = m.getX(), y = m.getY(), z = m.getZ();
        boolean bird = cage.kind == Baggable.CageKind.BIRD;

        // Birds hop between the floor and the perch
        if (bird && be.hop == 0 && be.target == null && r.nextInt(120) == 0) {
            double perchY = pos.getY() + cage.perch / 16;
            be.fromY = be.restY;
            be.restY = (be.restY == floorY && perchY <= topY) ? perchY : floorY;
            be.hop = 10;
            if (be.restY != floorY) be.target = new Vec3(Mth.clamp(x, minX, maxX), 0, pos.getZ() + 0.5);
        }
        if (be.hop > 0) {
            be.hop--;
            double t = 1 - be.hop / 10.0;
            y = Mth.lerp(t, be.fromY, be.restY) + Math.sin(t * Math.PI) * 0.18;
            m.setOnGround(be.hop == 0);
        } else {
            y = be.restY;
        }

        // Pick somewhere to go, or pause
        if (--be.timer <= 0) {
            if (r.nextFloat() < 0.35f) { be.target = null; be.timer = 20 + r.nextInt(60); }
            else {
                double tz = bird && be.restY != floorY ? pos.getZ() + 0.5 : Mth.lerp(r.nextDouble(), minZ, maxZ);
                be.target = new Vec3(Mth.lerp(r.nextDouble(), minX, maxX), 0, tz);
                be.timer = 40 + r.nextInt(60);
            }
        }
        if (be.target != null) {
            double dx = be.target.x - x, dz = be.target.z - z, dist = Math.sqrt(dx * dx + dz * dz);
            double speed = bird ? 0.03 : 0.05;
            if (dist < speed) { x = be.target.x; z = be.target.z; be.target = null; }
            else {
                x += dx / dist * speed; z += dz / dist * speed;
                float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
                m.setYRot(yaw); m.setYBodyRot(yaw); m.setYHeadRot(yaw);
            }
        } else if (r.nextInt(40) == 0) {
            m.setYHeadRot(m.getYRot() + (r.nextFloat() - 0.5f) * 90f);
        }
        m.setPos(Mth.clamp(x, minX, maxX), Mth.clamp(y, floorY, Math.max(floorY, topY)), Mth.clamp(z, minZ, maxZ));
        m.setDeltaMovement(Vec3.ZERO);
    }

    /** Breaking the cage lets the resident out. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        Mob m = resident();
        if (m != null) Cages.freeFromCage(m);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (occupant != null) out.store("Occupant", UUIDUtil.CODEC, occupant);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        occupant = in.read("Occupant", UUIDUtil.CODEC).orElse(null);
    }
}
