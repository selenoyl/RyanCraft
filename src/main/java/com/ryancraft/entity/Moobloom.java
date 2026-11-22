package com.ryancraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * The moobloom: a sunny yellow cow with dandelions growing on its back. It leaves dandelions behind as it
 * grazes; shear it for a handful and it turns back into a plain cow, like a mooshroom.
 */
public class Moobloom extends Cow implements Shearable {
    public Moobloom(EntityType<? extends Moobloom> type, Level level) { super(type, level); }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && !this.isBaby() && this.random.nextInt(2400) == 0) plantDandelion(level);
    }

    private void plantDandelion(ServerLevel level) {
        BlockPos at = this.blockPosition();
        BlockState here = level.getBlockState(at);
        BlockState flower = Blocks.DANDELION.defaultBlockState();
        if ((here.isAir() || here.is(Blocks.SHORT_GRASS)) && flower.canSurvive(level, at)) {
            level.setBlock(at, flower, 3);
            level.gameEvent(this, GameEvent.BLOCK_PLACE, at);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.SHEARS) && readyForShearing()) {
            if (this.level() instanceof ServerLevel level) {
                shear(level, SoundSource.PLAYERS, stack);
                this.gameEvent(GameEvent.SHEAR, player);
                stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void shear(ServerLevel level, SoundSource source, ItemStack shears) {
        level.playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, source, 1.0f, 1.0f);
        this.convertTo(EntityType.COW, ConversionParams.single(this, false, false), cow -> {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 1, 0, 0, 0, 0);
            for (int i = 0; i < 5; i++) this.spawnAtLocation(level, new ItemStack(Items.DANDELION), this.getBbHeight());
        });
    }

    @Override public boolean readyForShearing() { return this.isAlive() && !this.isBaby(); }

    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return RyanEntities.MOOBLOOM.create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
    }
}
