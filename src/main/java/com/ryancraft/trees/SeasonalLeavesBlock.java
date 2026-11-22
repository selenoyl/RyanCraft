package com.ryancraft.trees;

import com.ryancraft.season.Seasons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/** Our trees' leaves: they turn to their autumn colour through the autumn, and back to green (or blossom) in spring. */
public class SeasonalLeavesBlock extends UntintedParticleLeavesBlock {
    private final boolean autumn;
    private Supplier<Block> other = () -> null;

    public SeasonalLeavesBlock(boolean autumn, float particleChance, ParticleOptions particle, Properties p) {
        super(particleChance, particle, p);
        this.autumn = autumn;
    }

    public void pair(Supplier<Block> other) { this.other = other; }

    @Override protected boolean isRandomlyTicking(BlockState state) { return true; }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (super.isRandomlyTicking(state)) super.randomTick(state, level, pos, random);   // decay as usual
        if (level.getBlockState(pos) != state) return;
        Block to = other.get();
        int sub = Seasons.sub(level);
        if (to == null || Seasons.autumnLeaves(sub) == autumn || !Seasons.temperate(level.getBiome(pos).value())) return;
        if (random.nextFloat() >= Seasons.turnChance(sub)) return;
        level.setBlock(pos, to.withPropertiesOf(state), Block.UPDATE_CLIENTS);
    }
}
