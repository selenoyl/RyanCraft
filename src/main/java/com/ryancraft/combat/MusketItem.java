package com.ryancraft.combat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A muzzle-loading musket. Hold use to load it: powder, then the ball, then the ramrod, about three seconds in all,
 * using one gunpowder and one musket ball. Then use to fire: a loud crack, a cloud of smoke and a hard-hitting lead
 * ball that wanders off line past close range. Loaded, it shows the ball in its tooltip.
 */
public class MusketItem extends Item {
    public static final int LOAD_TICKS = 60;
    private static final float INACCURACY = 3.5f;

    public MusketItem(Properties p) { super(p); }

    public static boolean loaded(ItemStack s) { ChargedProjectiles c = s.get(DataComponents.CHARGED_PROJECTILES); return c != null && !c.isEmpty(); }

    private static boolean hasAmmo(Player p) {
        if (p.getAbilities().instabuild) return true;
        return p.getInventory().contains(new ItemStack(Items.GUNPOWDER)) && p.getInventory().contains(new ItemStack(Cannons.item(ShotType.PELLET)));
    }

    private static void consume(Player p, Item item) {
        if (p.getAbilities().instabuild) return;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) if (inv.getItem(i).is(item)) { inv.getItem(i).shrink(1); return; }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (loaded(stack)) {
            fire(level, player, stack, hand);
            return InteractionResult.SUCCESS;
        }
        if (!hasAmmo(player)) return InteractionResult.FAIL;
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    private void fire(Level level, Player player, ItemStack stack, InteractionHand hand) {
        stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.6f, 1.7f + level.random.nextFloat() * 0.2f);
        if (!(level instanceof ServerLevel sl)) return;
        Vec3 look = player.getLookAngle(), muzzle = player.getEyePosition().add(look.scale(1.0)).add(0, -0.1, 0);
        CannonShot ball = new CannonShot(sl, player, muzzle.x, muzzle.y, muzzle.z, ShotType.MUSKET);
        ball.shoot(look.x, look.y, look.z, ShotType.MUSKET.speed, INACCURACY);
        sl.addFreshEntity(ball);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z, 14, 0.15, 0.15, 0.15, 0.03);
        sl.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 3, 0.05, 0.05, 0.05, 0.02);
        player.setXRot(player.getXRot() - 4f);                                         // the kick
        stack.hurtAndBreak(1, player, hand);
    }

    @Override public int getUseDuration(ItemStack s, LivingEntity e) { return 72000; }
    @Override public ItemUseAnimation getUseAnimation(ItemStack s) { return ItemUseAnimation.CROSSBOW; }

    @Override
    public void onUseTick(Level level, LivingEntity e, ItemStack stack, int remaining) {
        int used = getUseDuration(stack, e) - remaining;
        if (level.isClientSide()) return;
        if (used == 5) level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.SAND_PLACE, SoundSource.PLAYERS, 0.6f, 1.4f);            // powder
        if (used == 25) level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.6f, 1.6f);           // ball
        if (used == 40 || used == 50) level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.4f, 1.8f);  // ramrod
        if (used == LOAD_TICKS && e instanceof Player p && hasAmmo(p)) {
            consume(p, Items.GUNPOWDER); consume(p, Cannons.item(ShotType.PELLET));
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStack(Cannons.item(ShotType.PELLET))));
            level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 0.8f, 0.8f);
            p.stopUsingItem();
        }
    }
}
