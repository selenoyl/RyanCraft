package com.ryancraft.bag;

import com.ryancraft.RyanCraft;
import com.ryancraft.entity.RyanEntities;
import com.ryancraft.mixin.ServerPlayerShoulderAccess;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.parrot.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.function.Function;

/**
 * Bird cage and chipmunk cage.
 * <ul>
 *   <li>Sneak-right-click with a mob bag: puts one in (if it's the right kind of cage and it's empty).</li>
 *   <li>Right-click with a bundle: scoops the resident back out.</li>
 *   <li>Bird cage, sneak-right-click with an empty hand: your shoulder parrot hops in. Right-click with an empty
 *       hand: your parrot hops back onto your shoulder.</li>
 *   <li>Break the cage and whoever's inside goes free.</li>
 * </ul>
 */
public final class Cages {
    public static final String CAGED_TAG = "ryancraft.caged";

    public static final Block BIRD_CAGE = block("bird_cage", p -> new CageBlock(Baggable.CageKind.BIRD, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).sound(SoundType.LANTERN).noOcclusion());
    public static final Block CHIPMUNK_CAGE = block("chipmunk_cage", p -> new CageBlock(Baggable.CageKind.CRITTER, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).sound(SoundType.CHAIN).noOcclusion());
    public static final BlockEntityType<CageBlockEntity> CAGE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            RyanCraft.id("cage"), FabricBlockEntityTypeBuilder.create(CageBlockEntity::new, BIRD_CAGE, CHIPMUNK_CAGE).build());

    /** Which cage each baggable (or shoulder-riding) mob lives in. */
    private static final Map<EntityType<?>, Baggable.CageKind> KINDS = Map.of(
            RyanEntities.CHIPMUNK, Baggable.CageKind.CRITTER,
            RyanEntities.SONGBIRD, Baggable.CageKind.BIRD,
            EntityType.PARROT, Baggable.CageKind.BIRD,
            RyanEntities.CROW, Baggable.CageKind.BIRD,
            RyanEntities.BUTTERFLY, Baggable.CageKind.BIRD,
            RyanEntities.HUMMINGBIRD, Baggable.CageKind.BIRD,
            RyanEntities.BOBWHITE, Baggable.CageKind.BIRD,
            RyanEntities.BOX_TURTLE, Baggable.CageKind.CRITTER);

    public static Baggable.CageKind kindOf(EntityType<?> type) { return KINDS.getOrDefault(type, Baggable.CageKind.NONE); }

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> f, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, RyanCraft.id(name));
        Block b = Registry.register(BuiltInRegistries.BLOCK, key, f.apply(props.setId(key)));
        ResourceKey<Item> ik = ResourceKey.create(Registries.ITEM, RyanCraft.id(name));
        Registry.register(BuiltInRegistries.ITEM, ik, new BlockItem(b, new Item.Properties().setId(ik).useBlockDescriptionPrefix()));
        return b;
    }

    public static void init() {
        MobBags.init();
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(e -> { e.accept(BIRD_CAGE); e.accept(CHIPMUNK_CAGE); });
    }

    // ---- moving mobs in and out

    public static void putInCage(ServerLevel level, BlockPos pos, CageBlockEntity be, Mob m) {
        CageBlock cage = (CageBlock) level.getBlockState(pos).getBlock();
        Vec3 c = Vec3.atBottomCenterOf(pos);
        m.snapTo(c.x, pos.getY() + cage.floor / 16, c.z, level.getRandom().nextFloat() * 360f, 0f);
        m.setDeltaMovement(Vec3.ZERO);
        m.setNoAi(true);
        m.setInvulnerable(true);
        m.setPersistenceRequired();
        m.addTag(CAGED_TAG);
        if (level.getEntity(m.getUUID()) != m) level.addFreshEntity(m);
        be.setOccupant(m.getUUID());
        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.6f, 1.4f);
    }

    public static void freeFromCage(Mob m) {
        if (!m.getTags().contains(CAGED_TAG)) return;
        m.removeTag(CAGED_TAG);
        m.setNoAi(false);
        m.setInvulnerable(false);
    }

    public static boolean isCaged(Entity e) { return e.getTags().contains(CAGED_TAG); }

    /** Right-click on a cage (called from {@link MobBags}' block-use event, before sneaking skips block interaction). */
    static InteractionResult use(Player player, Level level, InteractionHand hand, ItemStack stack, BlockPos pos) {
        // Sneak-use a spawn egg on an empty cage of the right kind: the animal hatches inside
        if (stack.getItem() instanceof net.minecraft.world.item.SpawnEggItem egg && player.isSecondaryUseActive()) {
            EntityType<?> type = egg.getType(stack);
            CageBlock eggCage = (CageBlock) level.getBlockState(pos).getBlock();
            if (kindOf(type) != eggCage.kind) return InteractionResult.PASS;
            if (!(level instanceof ServerLevel sl) || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
            if (!(level.getBlockEntity(pos) instanceof CageBlockEntity be)) return InteractionResult.PASS;
            if (be.resident() != null) return tell(sp, "message.ryancraft.cage.occupied");
            if (type.create(sl, net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE) instanceof Mob m) {
                m.snapTo(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, sl.random.nextFloat() * 360f, 0);
                m.finalizeSpawn(sl, sl.getCurrentDifficultyAt(pos), net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE, null);
                sl.addFreshEntity(m);
                putInCage(sl, pos, be, m);
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        boolean bundle = stack.getItem() instanceof BundleItem;
        boolean emptyHand = hand == InteractionHand.MAIN_HAND && stack.isEmpty();
        if (!bundle && !emptyHand) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel sl) || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof CageBlockEntity be)) return InteractionResult.PASS;
        CageBlock cage = (CageBlock) level.getBlockState(pos).getBlock();
        Mob resident = be.resident();

        if (bundle && player.isSecondaryUseActive() && MobBags.has(stack)) {
            if (resident != null) return tell(sp, "message.ryancraft.cage.occupied");
            if (MobBags.kindOf(stack) != cage.kind) return tell(sp, "message.ryancraft.cage.wrong_kind");
            MobBags.takeOne(sl, stack).ifPresent(e -> { if (e instanceof Mob m) putInCage(sl, pos, be, m); });
            return InteractionResult.SUCCESS;
        }
        if (bundle) {
            if (resident instanceof Baggable) {
                Component msg = MobBags.tryCatch(player, stack, resident);
                if (msg != null) sp.displayClientMessage(msg, true); else be.clear();
            } else if (resident != null) {
                return tell(sp, "message.ryancraft.cage.shoulder_bird");
            } else if (MobBags.has(stack)) {
                return tell(sp, "message.ryancraft.cage.sneak_hint");
            }
            return InteractionResult.SUCCESS;
        }
        // Empty hand at the bird cage: shoulder parrots
        if (cage.kind != Baggable.CageKind.BIRD) return InteractionResult.PASS;
        ServerPlayerShoulderAccess access = (ServerPlayerShoulderAccess) sp;
        if (player.isSecondaryUseActive() && resident == null) {
            boolean left = !sp.getShoulderEntityLeft().isEmpty();
            CompoundTag tag = left ? sp.getShoulderEntityLeft() : sp.getShoulderEntityRight();
            if (tag.isEmpty()) return InteractionResult.PASS;
            MobBags.load(sl, tag).ifPresent(e -> {
                if (!(e instanceof Mob m)) return;
                if (left) access.ryancraft$setShoulderEntityLeft(new CompoundTag()); else access.ryancraft$setShoulderEntityRight(new CompoundTag());
                putInCage(sl, pos, be, m);
            });
            return InteractionResult.SUCCESS;
        }
        if (!player.isSecondaryUseActive() && resident instanceof ShoulderRidingEntity bird && bird.isOwnedBy(player)) {
            freeFromCage(bird);
            bird.snapTo(player.getX(), player.getY() + 1.4, player.getZ());
            if (bird.setEntityOnShoulder(sp)) be.clear();
            else { putInCage(sl, pos, be, bird); return tell(sp, "message.ryancraft.cage.shoulders_full"); }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult tell(ServerPlayer sp, String key) {
        sp.displayClientMessage(Component.translatable(key), true);
        return InteractionResult.SUCCESS;
    }

    private Cages() {}
}
