package com.ryancraft.metal;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.NonNullList;

/**
 * Steel, for cannons and muskets. A blast furnace alone can't get hot enough: point a running engine (redstone or
 * Stirling) at it and every pulse works the bellows, stoking it. While it's stoked and burning fuel, iron ingots in it
 * slowly become steel ingots. More or faster engines keep it hotter.
 */
public final class Steel {
    public static final int STEEL_TIME = 400;                 // ticks per ingot while stoked
    public static final int HEAT_PER_PULSE = 70, MAX_HEAT = 240;

    public static final Item STEEL_INGOT, STEEL_BLOCK_ITEM;
    public static final Block STEEL_BLOCK;
    // decoration: cut steel (stairs, slab), plating, chiseled, a see-through grate, bars, and a redstone door and trapdoor
    public static final Block CUT_STEEL, CUT_STEEL_STAIRS, CUT_STEEL_SLAB, STEEL_PLATING, CHISELED_STEEL, STEEL_GRATE, STEEL_BARS, STEEL_DOOR, STEEL_TRAPDOOR;
    public static final java.util.List<Item> DECOR = new java.util.ArrayList<>();

    /** Added to every furnace block entity: how hot the bellows have it (ticks left). */
    public interface Stoked { int ryancraft$heat(); void ryancraft$setHeat(int heat); }
    /** Accessor for the furnace's private timers. */
    public interface FurnaceTimers {
        int ryancraft$lit(); void ryancraft$setLit(int v);
        void ryancraft$setLitTotal(int v);
        int ryancraft$cook(); void ryancraft$setCook(int v);
        void ryancraft$setCookTotal(int v);
        NonNullList<ItemStack> ryancraft$items();
    }

    static {
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id("steel_ingot"));
        STEEL_INGOT = Registry.register(BuiltInRegistries.ITEM, ik, new Item(new Item.Properties().setId(ik)));
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id("steel_block"));
        STEEL_BLOCK = Registry.register(BuiltInRegistries.BLOCK, bk, new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                .requiresCorrectToolForDrops().strength(6.0f, 8.0f).sound(SoundType.METAL).setId(bk)));
        ResourceKey<Item> bik = ResourceKey.create(Registries.ITEM, RyanCraft.id("steel_block"));
        STEEL_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, bik, new BlockItem(STEEL_BLOCK, new Item.Properties().setId(bik).useBlockDescriptionPrefix()));
        CUT_STEEL = decor("cut_steel", Block::new, metal());
        CUT_STEEL_STAIRS = decor("cut_steel_stairs", p -> new net.minecraft.world.level.block.StairBlock(CUT_STEEL.defaultBlockState(), p), metal());
        CUT_STEEL_SLAB = decor("cut_steel_slab", net.minecraft.world.level.block.SlabBlock::new, metal());
        STEEL_PLATING = decor("steel_plating", Block::new, metal());
        CHISELED_STEEL = decor("chiseled_steel", Block::new, metal());
        STEEL_GRATE = decor("steel_grate", net.minecraft.world.level.block.WaterloggedTransparentBlock::new, metal().noOcclusion()
                .isValidSpawn((st, l, ps, t) -> false).isRedstoneConductor((st, l, ps) -> false).isSuffocating((st, l, ps) -> false).isViewBlocking((st, l, ps) -> false));
        STEEL_BARS = decor("steel_bars", net.minecraft.world.level.block.IronBarsBlock::new, metal().strength(5.0f, 6.0f).noOcclusion());
        STEEL_DOOR = decor("steel_door", p -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, p),
                metal().strength(5.0f).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
        STEEL_TRAPDOOR = decor("steel_trapdoor", p -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, p),
                metal().strength(5.0f).noOcclusion().isValidSpawn((st, l, ps, t) -> false));
    }

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(5.0f, 7.0f).sound(SoundType.METAL);
    }

    private static Block decor(String name, java.util.function.Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item.Properties ip = new Item.Properties().setId(ik).useBlockDescriptionPrefix();
        DECOR.add(Registry.register(BuiltInRegistries.ITEM, ik, name.endsWith("_door") ? new net.minecraft.world.item.DoubleHighBlockItem(b, ip) : new BlockItem(b, ip)));
        return b;
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(e -> e.accept(STEEL_INGOT));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(e -> { e.accept(STEEL_BLOCK_ITEM); DECOR.forEach(e::accept); });
    }

    /** An engine pulse reached a blast furnace: work the bellows. */
    public static void stoke(ServerLevel level, BlockPos pos, AbstractFurnaceBlockEntity furnace) {
        Stoked s = (Stoked) furnace;
        s.ryancraft$setHeat(Math.min(MAX_HEAT, s.ryancraft$heat() + HEAT_PER_PULSE));
        if (((FurnaceTimers) furnace).ryancraft$lit() > 0) {
            Direction front = furnace.getBlockState().getValue(AbstractFurnaceBlock.FACING);
            double x = pos.getX() + 0.5 + front.getStepX() * 0.52, z = pos.getZ() + 0.5 + front.getStepZ() * 0.52;
            level.sendParticles(ParticleTypes.FLAME, x, pos.getY() + 0.4, z, 4, 0.15, 0.1, 0.15, 0.02);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 2, 0.1, 0.05, 0.1, 0.02);
            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.25f, 0.6f + level.getRandom().nextFloat() * 0.2f);
        }
    }

    /**
     * A blast furnace tick with iron ingots in it (instead of vanilla's, which has no recipe for them). Returns true if
     * it handled the tick.
     */
    public static boolean tick(ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity be) {
        FurnaceTimers t = (FurnaceTimers) be;
        NonNullList<ItemStack> items = t.ryancraft$items();
        ItemStack in = items.get(0), fuel = items.get(1), out = items.get(2);
        if (!in.is(Items.IRON_INGOT)) return false;
        Stoked s = (Stoked) be;
        int heat = Math.max(0, s.ryancraft$heat() - 1);
        s.ryancraft$setHeat(heat);
        boolean roomForSteel = out.isEmpty() || (out.is(STEEL_INGOT) && out.getCount() < out.getMaxStackSize());
        int lit = t.ryancraft$lit();
        if (lit > 0) lit--;
        if (heat > 0 && roomForSteel && lit == 0 && !fuel.isEmpty()) {
            int burn = level.fuelValues().burnDuration(fuel);
            if (burn > 0) {
                lit = burn / 2;                                   // blast furnaces burn fuel twice as fast
                t.ryancraft$setLitTotal(lit);
                if (fuel.is(Items.LAVA_BUCKET)) items.set(1, new ItemStack(Items.BUCKET)); else fuel.shrink(1);
            }
        }
        int cook = t.ryancraft$cook();
        t.ryancraft$setCookTotal(STEEL_TIME);
        if (heat > 0 && lit > 0 && roomForSteel) {
            if (++cook >= STEEL_TIME) {
                cook = 0;
                in.shrink(1);
                if (out.isEmpty()) items.set(2, new ItemStack(STEEL_INGOT)); else out.grow(1);
                level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.3f, 1.6f);
            }
        } else cook = Math.max(0, cook - 2);                       // cools off without the bellows
        t.ryancraft$setLit(lit);
        t.ryancraft$setCook(cook);
        if (state.getValue(AbstractFurnaceBlock.LIT) != (lit > 0)) level.setBlock(pos, state.setValue(AbstractFurnaceBlock.LIT, lit > 0), Block.UPDATE_ALL);
        be.setChanged();
        return true;
    }

    private Steel() {}
}
