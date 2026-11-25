package com.ryancraft.sky;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The Skylands: floating islands above the clouds (our own take on the old Aether, named for Beta's sky dimension).
 * Reached through the classic portal: a frame of glowstone, lit by pouring water into it.
 */
public final class Skylands {
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, RyanCraft.id("skylands"));
    public static final List<Item> ITEMS = new ArrayList<>();

    public static final Block SKYSTONE, SKY_PORTAL, ZEPHYRITE_ORE, ZEPHYRITE_BLOCK;
    public static final Item ZEPHYRITE;
    /** Skylands flowers and their potted forms (cut-out rendered). */
    public static final List<Block> FLOWERS = new ArrayList<>();
    public static final Block CLOUDBLOOM, SKY_BELL, WINDFLOWER, SUNPETAL;
    /** Updrafts: vents of rising air on the islands. */
    public static final Block ZEPHYR_VENT;
    /** Roc eggs, found in nests on the high islands. */
    public static final Block ROC_EGG;

    static {
        SKYSTONE = block("skystone", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.ICE).requiresCorrectToolForDrops().strength(1.5f, 6.0f), true);
        // zephyrite: a light, wind-touched crystal in the skystone, for gear suited to falling and gliding
        ZEPHYRITE_ORE = block("zephyrite_ore", p -> new net.minecraft.world.level.block.DropExperienceBlock(net.minecraft.util.valueproviders.UniformInt.of(2, 5), p),
                BlockBehaviour.Properties.of().mapColor(MapColor.ICE).requiresCorrectToolForDrops().strength(3.0f, 3.0f).lightLevel(s -> 3), true);
        ZEPHYRITE_BLOCK = block("zephyrite_block", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).requiresCorrectToolForDrops()
                .strength(3.0f, 4.0f).sound(SoundType.AMETHYST), true);
        ZEPHYRITE = item("zephyrite", new Item.Properties());
        ZEPHYR_VENT = block("zephyr_vent", ZephyrVentBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.ICE).requiresCorrectToolForDrops()
                .strength(1.5f, 6.0f).lightLevel(s -> 4), true);
        ROC_EGG = block("roc_egg", RocEggBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.5f)
                .sound(SoundType.METAL).randomTicks().noOcclusion(), true);
        CLOUDBLOOM = flower("cloudbloom", net.minecraft.world.effect.MobEffects.SLOW_FALLING, 7.0f);
        SKY_BELL = flower("sky_bell", net.minecraft.world.effect.MobEffects.JUMP_BOOST, 6.0f);
        WINDFLOWER = flower("windflower", net.minecraft.world.effect.MobEffects.SPEED, 6.0f);
        SUNPETAL = flower("sunpetal", net.minecraft.world.effect.MobEffects.SATURATION, 0.35f);
        SKY_PORTAL = block("sky_portal", SkyPortalBlock::new, BlockBehaviour.Properties.of().noCollision().randomTicks().strength(-1.0f)
                .sound(SoundType.GLASS).lightLevel(s -> 11).pushReaction(PushReaction.BLOCK), false);
    }

    private static Block flower(String name, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, float seconds) {
        Block f = block(name, p -> new net.minecraft.world.level.block.FlowerBlock(effect, seconds, p),
                BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.POPPY), true);
        Block pot = block("potted_" + name, p -> new net.minecraft.world.level.block.FlowerPotBlock(f, p),
                net.minecraft.world.level.block.Blocks.flowerPotProperties(), false);
        FLOWERS.add(f); FLOWERS.add(pot);
        return f;
    }

    private static Item item(String name, Item.Properties props) {
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Item i = Registry.register(BuiltInRegistries.ITEM, ik, new Item(props.setId(ik)));
        ITEMS.add(i); return i;
    }


    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props, boolean item) {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, bk, f.apply(props.setId(bk)));
        if (item) {
            ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
            ITEMS.add(Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix())));
        }
        return b;
    }

    public static final net.minecraft.world.level.block.entity.BlockEntityType<ZephyrVentBlock.Entity_> ZEPHYR_VENT_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("zephyr_vent"),
            net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(ZephyrVentBlock.Entity_::new, ZEPHYR_VENT).build());

    public static final com.ryancraft.worldgen.RocNestFeature ROC_NEST = Registry.register(BuiltInRegistries.FEATURE, RyanCraft.id("roc_nest"),
            new com.ryancraft.worldgen.RocNestFeature());

    public static boolean fullZephyrite(net.minecraft.world.entity.LivingEntity e) {
        return e.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(ZEPHYRITE_GEAR.helmet)
                && e.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(ZEPHYRITE_GEAR.chestplate)
                && e.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).is(ZEPHYRITE_GEAR.leggings)
                && e.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).is(ZEPHYRITE_GEAR.boots);
    }

    /** Zephyrite gear: iron's tier, a little easier to enchant; the boots stop fall damage, the full set glides (init). */
    public static final com.ryancraft.metal.GearSet ZEPHYRITE_GEAR = com.ryancraft.metal.GearSet.ironTier("zephyrite", 18);

    /** Fall out of the bottom of the Skylands and you drop into the overworld's sky at the same spot (as in the Aether), and fall
     *  the whole way: only in peaceful do you drift down gently. */
    public static final int FALL_OUT_Y = -24, ARRIVE_Y = 300;

    private static void fallOut(net.minecraft.server.level.ServerLevel sky) {
        var overworld = sky.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return;
        var falling = new java.util.ArrayList<net.minecraft.world.entity.Entity>();
        for (var e : sky.getAllEntities())
            if (e.getY() < FALL_OUT_Y && !e.isPassenger() && (e instanceof net.minecraft.world.entity.LivingEntity || e instanceof net.minecraft.world.entity.item.ItemEntity)) falling.add(e);
        for (var e : falling) {
            var v = e.getDeltaMovement();
            e.teleport(new net.minecraft.world.level.portal.TeleportTransition(overworld, new net.minecraft.world.phys.Vec3(e.getX(), ARRIVE_Y, e.getZ()),
                    new net.minecraft.world.phys.Vec3(v.x, Math.min(v.y, 0), v.z), e.getYRot(), e.getXRot(), net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
            if (e instanceof net.minecraft.world.entity.LivingEntity le                       // a long way down: only peaceful drifts you the last of it
                    && sky.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL)
                le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 20 * 40, 0));   // about 25 s from y 300 to the ground
        }
    }

    public static void init() {
        ZEPHYRITE_GEAR.addToTabs();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(level -> {
            if (level.dimension().equals(LEVEL) && level.getGameTime() % 5 == 0) fallOut(level);
        });
        // zephyrite boots: you land like a feather; a full set lets you drift down slowly while you hold sneak
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
                !(source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)
                        && entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).is(ZEPHYRITE_GEAR.boots)));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (net.minecraft.server.level.ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (!p.isShiftKeyDown() || p.onGround() || p.getDeltaMovement().y > -0.1 || !fullZephyrite(p)) continue;
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 10, 0, true, false, false));
            }
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(e -> ITEMS.forEach(e::accept));
        // Pour a water bucket into a glowstone frame to light the portal (instead of placing the water)
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.WATER_BUCKET) || player.isSpectator()) return InteractionResult.PASS;
            BlockPos at = hit.getBlockPos().relative(hit.getDirection());
            if (!level.getBlockState(hit.getBlockPos()).is(net.minecraft.world.level.block.Blocks.GLOWSTONE) && !SkyPortalShape.nearGlowstone(level, at))
                return InteractionResult.PASS;
            for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
                SkyPortalShape shape = SkyPortalShape.find(level, at, axis);
                if (shape == null) continue;
                if (!level.isClientSide()) {
                    shape.fill(level);
                    level.playSound(null, at, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.6f, 1.6f);
                    level.playSound(null, at, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0f, 1.0f);
                    if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }

    private Skylands() {}
}
