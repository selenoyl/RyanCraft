package com.ryancraft.pipes;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Each stroke draws a quarter bucket of water; it pours out of the spout, or builds up toward a bucketful. */
public class HandPumpBlockEntity extends BlockEntity {
    private static final long STROKE = FluidConstants.BUCKET / 4;
    /** Water drawn up but not yet poured (for filling a bucket by hand). */
    private long held;
    /** Client: when the handle last went down (for the animation). */
    public long lastStroke = -1000;

    public HandPumpBlockEntity(BlockPos pos, BlockState state) { super(RyanPipes.HAND_PUMP_ENTITY, pos, state); }

    public Direction spout() { return getBlockState().getValue(HandPumpBlock.FACING); }

    /** One pull of the handle (by a player, or an engine when player is null). */
    public void stroke(Level level, Player player, InteractionHand hand) {
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
        level.playSound(null, worldPosition, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.5f, 0.7f + level.getRandom().nextFloat() * 0.1f);
        if (held < FluidConstants.BUCKET) {
            // a quarter of a bucket per stroke; a whole bucket is drawn from the pool every fourth stroke
            if (held % FluidConstants.BUCKET == 0 && FluidWell.draw(level, worldPosition, 16, true) == null) return;
            held += STROKE;
        }
        FluidVariant water = FluidVariant.of(Fluids.WATER);
        long poured = FluidWell.push(level, worldPosition, spout(), water, held);
        if (poured > 0) {
            held -= poured;
            level.playSound(null, worldPosition.relative(spout()), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.3f, 1.4f);
        } else if (player != null && held >= FluidConstants.BUCKET) {
            ItemStack inHand = player.getItemInHand(hand);
            if (inHand.is(Items.BUCKET)) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(inHand, player, new ItemStack(Items.WATER_BUCKET)));
                held = 0;
                level.playSound(null, worldPosition, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.6f, 1.0f);
            }
        }
        setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putLong("Held", held); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); held = in.getLongOr("Held", 0L); }
}
