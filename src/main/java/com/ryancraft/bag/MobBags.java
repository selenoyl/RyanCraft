package com.ryancraft.bag;

import com.ryancraft.RyanCraft;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

/**
 * Scooping small mobs into bundles. Right-click a {@link Baggable} mob with an empty bundle (or one already holding
 * that kind of mob) to catch it; right-click a block to let one go there. Cages take them with a sneak-right-click.
 */
public final class MobBags {
    public static final DataComponentType<MobBag> MOB_BAG = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            RyanCraft.id("mob_bag"), DataComponentType.<MobBag>builder().persistent(MobBag.CODEC).networkSynchronized(MobBag.STREAM_CODEC).build());

    public static boolean has(ItemStack stack) {
        MobBag bag = stack.get(MOB_BAG);
        return bag != null && bag.count() > 0;
    }

    public static MobBag get(ItemStack stack) { return stack.get(MOB_BAG); }

    private static boolean holdsItems(ItemStack bundle) {
        var contents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        return contents != null && !contents.isEmpty();
    }

    // ---- saving and restoring mobs

    public static CompoundTag save(Entity e) {
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, e.registryAccess());
        e.saveWithoutId(out);
        out.putString("id", EntityType.getKey(e.getType()).toString());
        return out.buildResult();
    }

    /** A fresh copy of the mob (new UUID, so a duplicated bag can never clash with a living mob). */
    public static Optional<Entity> load(ServerLevel level, CompoundTag tag) {
        Optional<Entity> e = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag), level, EntitySpawnReason.LOAD);
        e.ifPresent(x -> x.setUUID(UUID.randomUUID()));
        return e;
    }

    // ---- catching

    /** Tries to scoop {@code target} into the bundle. Returns a message for the player if it can't. */
    public static Component tryCatch(Player player, ItemStack bundle, Entity target) {
        if (!(target instanceof Baggable b) || !target.isAlive()) return null;
        if (holdsItems(bundle)) return Component.translatable("message.ryancraft.bag.has_items");
        MobBag bag = bundle.get(MOB_BAG);
        String type = EntityType.getKey(target.getType()).toString();
        if (bag != null && bag.count() > 0) {
            if (!bag.typeId().equals(type)) return Component.translatable("message.ryancraft.bag.other_kind", bag.names().get(0));
            if (bag.isFull()) return Component.translatable("message.ryancraft.bag.full");
        }
        if (target instanceof Mob m) Cages.freeFromCage(m);
        Component name = target.getName().copy();
        CompoundTag tag = save(target);
        bundle.set(MOB_BAG, (bag == null ? new MobBag(java.util.List.of(), java.util.List.of(), b.bagCapacity()) : bag).with(tag, name, b.bagCapacity()));
        target.level().playSound(null, target.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0f, 1.2f);
        target.discard();
        return null;
    }

    /** Takes the last mob out of the bag (the caller places it). */
    public static Optional<Entity> takeOne(ServerLevel level, ItemStack bundle) {
        MobBag bag = bundle.get(MOB_BAG);
        if (bag == null || bag.count() == 0) return Optional.empty();
        Optional<Entity> e = load(level, bag.mobs().get(bag.count() - 1));
        if (e.isPresent()) {
            MobBag rest = bag.withoutLast();
            if (rest.count() == 0) bundle.remove(MOB_BAG); else bundle.set(MOB_BAG, rest);
        }
        return e;
    }

    public static Baggable.CageKind kindOf(ItemStack bundle) {
        MobBag bag = bundle.get(MOB_BAG);
        if (bag == null || bag.count() == 0) return Baggable.CageKind.NONE;
        Optional<EntityType<?>> type = EntityType.byString(bag.typeId());
        return type.map(Cages::kindOf).orElse(Baggable.CageKind.NONE);
    }

    private static void tell(Player player, Component msg) {
        if (msg != null && player instanceof ServerPlayer sp) sp.displayClientMessage(msg, true);
    }

    // ---- events

    public static void init() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof BundleItem) || !(entity instanceof Baggable) || player.isSpectator()) return InteractionResult.PASS;
            if (!level.isClientSide()) tell(player, tryCatch(player, stack, entity));
            return InteractionResult.SUCCESS;
        });
        UseBlockCallback.EVENT.register(MobBags::useOnBlock);
    }

    private static InteractionResult useOnBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator()) return InteractionResult.PASS;
        BlockPos pos = hit.getBlockPos();
        ItemStack stack = player.getItemInHand(hand);
        if (level.getBlockState(pos).getBlock() instanceof CageBlock) return Cages.use(player, level, hand, stack, pos);
        // Let a mob go where you click
        if (stack.getItem() instanceof BundleItem && has(stack)) {
            if (level instanceof ServerLevel sl) {
                BlockPos at = pos.relative(hit.getDirection());
                takeOne(sl, stack).ifPresent(e -> {
                    Vec3 c = Vec3.atBottomCenterOf(at);
                    e.snapTo(c.x, c.y, c.z, player.getYRot() + 180f, 0f);
                    sl.addFreshEntity(e);
                    sl.playSound(null, at, SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, 1.0f, 1.0f);
                });
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private MobBags() {}
}
