package com.ryancraft.entity;

import com.ryancraft.bag.Baggable;
import com.ryancraft.moon.MoonBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Moon rabbit: long-eared, silvery and shy, hopping very high in the low gravity. Feed one moon herbs and it may take to
 * you (one in three; a moon cake always wins it over): it then follows you about, even home through the portal. Scoop it up in a bundle (four to a
 * bundle) and keep it in a cage like the chipmunks. Moon herbs breed them.
 */
public class MoonRabbit extends Rabbit implements Baggable {
    private @Nullable UUID friend;

    public MoonRabbit(EntityType<? extends MoonRabbit> type, Level level) { super(type, level); }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new FollowFriendGoal());
    }

    @Override public boolean isFood(ItemStack stack) { return stack.is(MoonBlocks.MOON_HERB.asItem()) || stack.is(MoonBlocks.MOON_CAKE); }
    @Override public int bagCapacity() { return 4; }
    @Override public CageKind cageKind() { return CageKind.CRITTER; }
    @Override public boolean requiresCustomPersistence() { return friend != null || super.requiresCustomPersistence(); }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (friend == null && isFood(stack) && !isBaby()) {
            boolean cake = stack.is(MoonBlocks.MOON_CAKE);
            usePlayerItem(player, hand, stack);
            if (!level().isClientSide()) {
                if (cake || random.nextInt(3) == 0) { friend = player.getUUID(); level().broadcastEntityEvent(this, (byte) 7); }
                else level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public @Nullable Rabbit getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return RyanEntities.MOON_RABBIT.create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean canMate(Animal other) { return other instanceof MoonRabbit && super.canMate(other); }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) { return true; }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        if (friend != null) out.putString("Friend", friend.toString());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        friend = in.getString("Friend").map(UUID::fromString).orElse(null);
    }

    /** Hop after the friend who fed it, when they wander more than a few blocks off. */
    class FollowFriendGoal extends Goal {
        private @Nullable Player target;
        FollowFriendGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            if (friend == null) return false;
            target = level().getPlayerByUUID(friend);
            return target != null && !target.isSpectator() && distanceToSqr(target) > 36 && distanceToSqr(target) < 32 * 32;
        }
        @Override public boolean canContinueToUse() { return target != null && target.isAlive() && distanceToSqr(target) > 9; }
        @Override public void tick() { if (target != null) getNavigation().moveTo(target, 1.4); }
        @Override public void stop() { target = null; getNavigation().stop(); }
    }
}
