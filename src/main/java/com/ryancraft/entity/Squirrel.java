package com.ryancraft.entity;

import com.ryancraft.RyanSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;

/**
 * Eastern gray squirrel (deep woods) and fox squirrel (open woods and edges).
 * Quick, nervous, and straight up the nearest trunk when you come near.
 */
public class Squirrel extends CritterAnimal {
    public static final int GRAY = 0, FOX = 1;

    public Squirrel(EntityType<? extends Squirrel> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.32);
    }

    @Override protected int variantCount() { return 2; }

    /** Fox squirrels are the bigger of the two; eastern grays are noticeably smaller. */
    @Override
    protected void onVariantSet(int variant) {
        var scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) scale.setBaseValue(variant == FOX ? 1.15 : 0.85);
    }
    @Override protected boolean climbs() { return true; }

    @Override
    protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) {
        var biome = level.getBiome(pos);
        boolean openWoods = biome.is(Biomes.PLAINS) || biome.is(Biomes.MEADOW) || biome.is(BiomeTags.IS_SAVANNA)
                || biome.unwrapKey().map(k -> k.identifier().getPath().equals("shrubland")).orElse(false);
        return this.random.nextFloat() < (openWoods ? 0.75f : 0.25f) ? FOX : GRAY;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ClimbTreeGoal());
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.8));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.5, 1.9,
                e -> !e.isCrouching() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, this::isFood, true));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.2));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack s) { return s.is(Items.SWEET_BERRIES) || s.is(Items.PUMPKIN_SEEDS) || s.is(Items.MELON_SEEDS) || s.is(Items.COCOA_BEANS); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return offspring(RyanEntities.SQUIRREL, l, p); }
    @Override protected SoundEvent getAmbientSound() { return RyanSounds.SQUIRREL_CHATTER; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return RyanSounds.SQUIRREL_CHATTER; }
    @Override protected SoundEvent getDeathSound() { return RyanSounds.SQUIRREL_CHATTER; }
    @Override public int getAmbientSoundInterval() { return 240; }
    @Override protected String variantName() { return getVariant() == FOX ? "fox" : null; }

    /** Run to a nearby trunk and scurry up it into the canopy, now and then and always when spooked; sit up there a while. */
    class ClimbTreeGoal extends Goal {
        private net.minecraft.core.BlockPos trunk;
        private int ticks, perch;

        ClimbTreeGoal() { this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP)); }

        private boolean spooked() { return Squirrel.this.getLastHurtByMob() != null || Squirrel.this.level().getNearestPlayer(Squirrel.this, 6) instanceof Player p && !p.isCrouching() && !p.isCreative() && !p.isSpectator(); }

        @Override public boolean canUse() {
            if (Squirrel.this.isBaby() || !Squirrel.this.onGround()) return false;
            if (!spooked() && Squirrel.this.getRandom().nextInt(300) != 0) return false;
            trunk = findTrunk();
            return trunk != null;
        }

        private net.minecraft.core.BlockPos findTrunk() {
            net.minecraft.core.BlockPos me = Squirrel.this.blockPosition(), best = null;
            double bestD = Double.MAX_VALUE;
            for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(me.offset(-10, -2, -10), me.offset(10, 2, 10))) {
                var st = Squirrel.this.level().getBlockState(p);
                if (!st.is(net.minecraft.tags.BlockTags.LOGS) || !Squirrel.this.level().getBlockState(p.above()).is(net.minecraft.tags.BlockTags.LOGS)) continue;
                if (Squirrel.this.level().getBlockState(p.below()).is(net.minecraft.tags.BlockTags.LOGS)) continue;   // the trunk's foot
                double d = p.distSqr(me);
                if (d < bestD) { bestD = d; best = p.immutable(); }
            }
            return best;
        }

        @Override public void start() { ticks = 0; perch = 0; }
        @Override public boolean canContinueToUse() { return trunk != null && ticks < 600 && (perch == 0 || perch < 200); }
        @Override public void stop() { trunk = null; Squirrel.this.getNavigation().stop(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override public void tick() {
            ticks++;
            Squirrel self = Squirrel.this;
            double cx = trunk.getX() + 0.5, cz = trunk.getZ() + 0.5;
            double flat = Math.hypot(self.getX() - cx, self.getZ() - cz);
            if (perch > 0) { perch++; self.getNavigation().stop(); return; }
            if (flat > 1.4 && self.getY() < trunk.getY() + 1) {            // run to the foot of the tree
                if (ticks % 10 == 1) self.getNavigation().moveTo(cx, trunk.getY(), cz, spooked() ? 1.8 : 1.2);
                return;
            }
            // press into the trunk: the climbing check lifts it while it's pushing against the log
            net.minecraft.core.BlockPos level = net.minecraft.core.BlockPos.containing(cx, self.getY() + 0.5, cz);
            if (self.level().getBlockState(level).is(net.minecraft.tags.BlockTags.LOGS)) {
                self.getMoveControl().setWantedPosition(cx, self.getY() + 1.5, cz, 1.4);
                self.setDeltaMovement(self.getDeltaMovement().x, Math.max(self.getDeltaMovement().y, 0.2), self.getDeltaMovement().z);
            } else {
                perch = 1;                                                  // out of trunk: up in the crown, sit tight
            }
        }
    }
}
