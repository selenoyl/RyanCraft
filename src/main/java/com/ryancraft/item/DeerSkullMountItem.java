package com.ryancraft.item;

import com.ryancraft.entity.DeerMount;
import com.ryancraft.entity.RyanEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.UseOnContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A primitive deer skull mount: bleached skull and rack nailed to a plank. Carries the exact rack of the
 * buck it came from (stage, drop tines, rare rack, antler colour). Use it on a wall to hang it.
 */
public class DeerSkullMountItem extends Item {
    public static final int[] ANTLER_RGB = {0xD6C49C, 0x957652, 0x5E4430};
    private static final String[] RARE = {null, "cactus", "palmated", "lopsided"};

    public DeerSkullMountItem(Properties props) { super(props); }

    public static ItemStack create(int stage, boolean dropTine, int rack, int color) {
        ItemStack stack = new ItemStack(com.ryancraft.RyanItems.DEER_SKULL_MOUNT);
        apply(stack, stage, dropTine, rack, color);
        return stack;
    }

    public static void apply(ItemStack stack, int stage, boolean dropTine, int rack, int color) {
        stage = Math.max(1, Math.min(4, stage));
        boolean rare = stage == 4 && rack > 0 && rack < RARE.length;
        boolean drop = !rare && dropTine && stage >= 3;
        String key = rare ? RARE[rack] : drop ? "drop" + stage : "s" + stage;
        CompoundTag tag = new CompoundTag();
        tag.putInt("Antlers", stage);
        tag.putBoolean("DropTine", drop);
        tag.putInt("Rack", rare ? rack : 0);
        tag.putInt("AntlerColor", color);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.ryancraft.deer_skull_mount." + (drop ? "drop" : key)));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(key),
                List.of(ANTLER_RGB[Math.max(0, Math.min(2, color))])));
        List<Component> lore = new ArrayList<>();
        if (color == 1) lore.add(Component.translatable("item.ryancraft.deer_skull_mount.dark").withStyle(ChatFormatting.GRAY));
        if (color == 2) lore.add(Component.translatable("item.ryancraft.deer_skull_mount.chocolate").withStyle(ChatFormatting.GOLD));
        if (rare) lore.add(Component.translatable("item.ryancraft.deer_skull_mount.rare").withStyle(ChatFormatting.LIGHT_PURPLE));
        if (!lore.isEmpty()) stack.set(DataComponents.LORE, new ItemLore(lore));
    }

    public static CompoundTag data(ItemStack stack) {
        CustomData d = stack.get(DataComponents.CUSTOM_DATA);
        if (d == null) { CompoundTag t = new CompoundTag(); t.putInt("Antlers", 4); return t; }
        return d.copyTag();
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Direction face = ctx.getClickedFace();
        if (face.getAxis().isVertical() || ctx.getPlayer() == null) return InteractionResult.PASS;
        if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        BlockPos wall = ctx.getClickedPos();
        DeerMount mount = RyanEntities.DEER_MOUNT.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (mount == null) return InteractionResult.FAIL;
        CompoundTag tag = data(ctx.getItemInHand());
        mount.setRackData(tag.getIntOr("Antlers", 4), tag.getBooleanOr("DropTine", false), tag.getIntOr("Rack", 0), tag.getIntOr("AntlerColor", 0));
        mount.hangOn(wall, face);
        // the mount's box overlaps its own wall, so only other mounts or creatures in the way block it
        if (!level.getEntities(mount, mount.getBoundingBox().deflate(0.05), e -> e instanceof DeerMount || e instanceof net.minecraft.world.entity.LivingEntity).isEmpty()) {
            mount.discard();
            return InteractionResult.FAIL;
        }
        level.addFreshEntity(mount);
        level.playSound(null, mount.blockPosition(), SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 1.0f, 0.9f);
        ctx.getItemInHand().consume(1, ctx.getPlayer());
        return InteractionResult.SUCCESS;
    }
}
