package com.ryancraft.food;

import com.mojang.serialization.MapCodec;
import com.ryancraft.season.Seasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A four-stage crop that only grows in its own seasons in temperate biomes (unless it's under a roof, in a
 * greenhouse); elsewhere, and in season, it grows like wheat. Winter stalls it like every crop.
 */
public class SeasonalCropBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private final Supplier<ItemLike> seed;
    private final Set<Seasons.Season> seasons;

    public SeasonalCropBlock(Properties p, Supplier<ItemLike> seed, Set<Seasons.Season> seasons) {
        super(p); this.seed = seed; this.seasons = EnumSet.copyOf(seasons);
    }

    public Set<Seasons.Season> seasons() { return seasons; }

    @Override public MapCodec<? extends CropBlock> codec() { return simpleCodec(p -> new SeasonalCropBlock(p, seed, seasons)); }
    @Override protected IntegerProperty getAgeProperty() { return AGE; }
    @Override public int getMaxAge() { return 3; }
    @Override protected ItemLike getBaseSeedId() { return seed.get(); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(AGE); }

    public boolean inSeason(ServerLevel level, BlockPos pos) {
        if (!Seasons.temperate(level.getBiome(pos).value()) || !level.canSeeSky(pos.above())) return true;
        return seasons.contains(Seasons.season(Seasons.sub(level)));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource r) {
        if (!inSeason(level, pos)) return;
        if (r.nextInt(3) != 0) super.randomTick(state, level, pos, r);      // four stages, so a little slower per stage than wheat
    }
}
