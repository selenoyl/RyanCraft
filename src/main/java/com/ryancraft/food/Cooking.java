package com.ryancraft.food;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/** Vintage Story style cooking: the cast-iron cooking pot over a campfire, and the free-form meals it makes. Assets: tools/cooking.py. */
public final class Cooking {
    public static final DataComponentType<Meal> MEAL_DATA = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, RyanCraft.id("meal"),
            DataComponentType.<Meal>builder().persistent(Meal.CODEC).networkSynchronized(Meal.STREAM_CODEC).build());
    public static final Block COOKING_POT;
    public static final Item COOKING_POT_ITEM, MEAL;
    public static final BlockEntityType<CookingPotBlockEntity> POT_ENTITY;
    public static final Block MEAL_BOWLS;
    public static final BlockEntityType<MealBowlsBlockEntity> MEAL_BOWLS_ENTITY;

    static {
        ResourceKey<Block> bk = ResourceKey.create(Registries.BLOCK, RyanCraft.id("cooking_pot"));
        COOKING_POT = Registry.register(BuiltInRegistries.BLOCK, bk, new CookingPotBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops().strength(2.0f).sound(SoundType.LANTERN)
                .lightLevel(st -> st.getValue(CookingPotBlock.SUPPORT) == CookingPotBlock.Support.FIRE && st.getValue(CookingPotBlock.LIT) ? 15 : 0).noOcclusion().pushReaction(PushReaction.DESTROY).setId(bk)));
        COOKING_POT_ITEM = item("cooking_pot", p -> new BlockItem(COOKING_POT, p), new Item.Properties().stacksTo(16).useBlockDescriptionPrefix());
        MEAL = item("meal", MealItem::new, new Item.Properties().stacksTo(1).food(new FoodProperties(6, 7.2f, false)).usingConvertsTo(Items.BOWL));
        ResourceKey<Block> mk = ResourceKey.create(Registries.BLOCK, RyanCraft.id("meal_bowls"));
        MEAL_BOWLS = Registry.register(BuiltInRegistries.BLOCK, mk, new MealBowlsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                .instabreak().sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.DESTROY).setId(mk)));
        MEAL_BOWLS_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("meal_bowls"),
                FabricBlockEntityTypeBuilder.create(MealBowlsBlockEntity::new, MEAL_BOWLS).build());
        POT_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, RyanCraft.id("cooking_pot"),
                FabricBlockEntityTypeBuilder.create(CookingPotBlockEntity::new, COOKING_POT).build());
    }

    private static Item item(String name, java.util.function.Function<Item.Properties, Item> f, Item.Properties p) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, f.apply(p.setId(key)));
    }

    public static void init() {
        // sneak-use a meal on the top of a block to set the bowl down (up to four to a block)
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            var stack = player.getItemInHand(hand);
            if (!stack.is(MEAL) || !player.isSecondaryUseActive() || player.isSpectator()) return net.minecraft.world.InteractionResult.PASS;
            var target = level.getBlockState(hit.getBlockPos()).is(MEAL_BOWLS) ? hit.getBlockPos()
                    : hit.getDirection() == net.minecraft.core.Direction.UP ? hit.getBlockPos().above() : null;
            if (target == null) return net.minecraft.world.InteractionResult.PASS;
            if (!level.isClientSide()) {
                if (!MealBowlsBlock.place(level, target, stack)) return net.minecraft.world.InteractionResult.FAIL;
                stack.consume(1, player);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        });
        // use the pot on a campfire and it goes right into the fire: the two become one block
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            var stack = player.getItemInHand(hand);
            var state = level.getBlockState(hit.getBlockPos());
            if (!stack.is(COOKING_POT_ITEM) || !state.is(net.minecraft.world.level.block.Blocks.CAMPFIRE) || player.isSpectator())
                return net.minecraft.world.InteractionResult.PASS;
            if (!level.isClientSide()) {
                FuelHolder camp = level.getBlockEntity(hit.getBlockPos()) instanceof FuelHolder f ? f : null;
                var fire = new java.util.ArrayList<ItemStack>(); int burn = -1, total = 0;
                if (camp != null) { fire.add(camp.ryancraft$fuel().get(0).copy()); burn = camp.ryancraft$burn(); total = camp.ryancraft$total(); camp.ryancraft$fuel().set(0, ItemStack.EMPTY); }
                level.setBlock(hit.getBlockPos(), COOKING_POT.defaultBlockState().setValue(CookingPotBlock.SUPPORT, CookingPotBlock.Support.FIRE)
                        .setValue(CookingPotBlock.LIT, state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)), 3);
                if (level.getBlockEntity(hit.getBlockPos()) instanceof CookingPotBlockEntity pot) {          // the fire carries on under the pot
                    if (!fire.isEmpty()) pot.ryancraft$fuel().set(0, fire.get(0));
                    pot.ryancraft$setBurn(burn); pot.ryancraft$setTotal(total); pot.setChanged();
                }
                level.playSound(null, hit.getBlockPos(), net.minecraft.sounds.SoundEvents.LANTERN_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1f, 0.8f);
                stack.consume(1, player);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(e -> e.accept(COOKING_POT_ITEM));
    }

    private Cooking() {}
}
