package com.ryancraft.entity;

import com.ryancraft.item.DeerSkullMountItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A hung deer skull mount. Like an armor stand it's an entity rather than a block, so it can carry the
 * buck's exact rack. Punch it to take it down; it falls if its wall goes away.
 */
public class DeerMount extends Mob {
    private static final EntityDataAccessor<Integer> ANTLERS = SynchedEntityData.defineId(DeerMount.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DROP_TINE = SynchedEntityData.defineId(DeerMount.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RACK = SynchedEntityData.defineId(DeerMount.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(DeerMount.class, EntityDataSerializers.INT);
    /** Synced so the client faces the mount out of the wall too. */
    private static final EntityDataAccessor<Integer> FACING = SynchedEntityData.defineId(DeerMount.class, EntityDataSerializers.INT);

    public DeerMount(EntityType<? extends DeerMount> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setNoGravity(true);
        this.noPhysics = true;          // it sits partly inside its wall
        this.setPersistenceRequired();
        this.setSilent(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANTLERS, 4);
        builder.define(DROP_TINE, false);
        builder.define(RACK, 0);
        builder.define(COLOR, 0);
        builder.define(FACING, Direction.SOUTH.get2DDataValue());
    }

    public int getAntlers() { return entityData.get(ANTLERS); }
    public boolean hasDropTine() { return entityData.get(DROP_TINE); }
    public int getRack() { return entityData.get(RACK); }
    public int getAntlerColor() { return entityData.get(COLOR); }
    public Direction getFacing() { return Direction.from2DDataValue(entityData.get(FACING)); }
    private void setFacing(Direction d) { entityData.set(FACING, d.get2DDataValue()); }

    public void setRackData(int stage, boolean drop, int rack, int color) {
        entityData.set(ANTLERS, stage); entityData.set(DROP_TINE, drop); entityData.set(RACK, rack); entityData.set(COLOR, color);
    }

    /** Places the mount flat against the clicked wall face, facing out of it. */
    public void hangOn(BlockPos wall, Direction face) {
        setFacing(face);
        double wallX = wall.getX() + 0.5 + face.getStepX() * 0.5, wallZ = wall.getZ() + 0.5 + face.getStepZ() * 0.5;
        this.snapTo(wallX + face.getStepX() * 0.0625, wall.getY(), wallZ + face.getStepZ() * 0.0625, face.toYRot(), 0);
        faceOut();
    }

    private void faceOut() {
        float yaw = getFacing().toYRot();
        this.setYRot(yaw); this.yRotO = yaw; this.yBodyRot = yaw; this.yBodyRotO = yaw; this.yHeadRot = yaw; this.yHeadRotO = yaw;
    }

    public ItemStack asItem() {
        return DeerSkullMountItem.create(getAntlers(), hasDropTine(), getRack(), getAntlerColor());
    }

    @Override
    public void tick() {
        super.tick();
        faceOut();
        this.setDeltaMovement(0, 0, 0);
        if (this.level() instanceof ServerLevel level && this.tickCount % 20 == 0) {
            Direction facing = getFacing();
            BlockPos wall = BlockPos.containing(this.getX() - facing.getStepX() * 0.3, this.getY() + 0.3, this.getZ() - facing.getStepZ() * 0.3);
            if (!level.getBlockState(wall).isFaceSturdy(level, wall, facing)) popOff(level, true);
        }
    }

    private void popOff(ServerLevel level, boolean drop) {
        if (drop) this.spawnAtLocation(level, asItem());
        this.playSound(SoundEvents.ITEM_FRAME_BREAK, 1.0f, 0.9f);
        this.discard();
    }

    /** Only a player's hit does anything: it takes the mount down. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) {
            popOff(level, !player.isCreative());
            return true;
        }
        return false;
    }

    @Override public boolean isPushable() { return false; }
    @Override public boolean isPickable() { return true; }
    @Override public ItemStack getPickResult() { return asItem(); }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity e) {}
    @Override public boolean isAffectedByPotions() { return false; }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("Antlers", getAntlers()); out.putBoolean("DropTine", hasDropTine());
        out.putInt("Rack", getRack()); out.putInt("AntlerColor", getAntlerColor());
        out.putInt("Facing", getFacing().get2DDataValue());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        setRackData(in.getIntOr("Antlers", 4), in.getBooleanOr("DropTine", false), in.getIntOr("Rack", 0), in.getIntOr("AntlerColor", 0));
        setFacing(Direction.from2DDataValue(in.getIntOr("Facing", 0)));
        faceOut();
    }
}
