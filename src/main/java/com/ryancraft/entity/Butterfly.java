package com.ryancraft.entity;

import com.ryancraft.bag.Baggable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Butterflies, twenty eastern species (like the tropical fish: a few wing patterns in many colours). They flutter
 * over meadows and flowers by day. Catch them in a bundle and keep them in a bird cage.
 */
public class Butterfly extends SmallFlyer implements Baggable {
    public static final String[] SPECIES = {"monarch", "viceroy", "eastern_tiger_swallowtail", "black_swallowtail", "pipevine_swallowtail",
            "spicebush_swallowtail", "zebra_swallowtail", "red_admiral", "painted_lady", "great_spangled_fritillary", "gulf_fritillary",
            "mourning_cloak", "question_mark", "red_spotted_purple", "cabbage_white", "clouded_sulphur", "spring_azure", "common_buckeye",
            "luna_moth", "zebra_longwing"};

    public Butterfly(EntityType<? extends Butterfly> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 2.0).add(Attributes.FLYING_SPEED, 0.35).add(Attributes.MOVEMENT_SPEED, 0.15);
    }

    @Override protected int variantCount() { return SPECIES.length; }
    @Override protected String variantName() { return SPECIES[getVariant()]; }
    @Override protected int chooseVariant(ServerLevelAccessor level, BlockPos pos) {
        // Monarchs and cabbage whites are the commonest; luna moths the rarest
        int r = this.random.nextInt(100);
        if (r < 14) return 0;
        if (r < 26) return 14;
        if (r < 28) return 18;
        return this.random.nextInt(SPECIES.length);
    }

    @Override public int bagCapacity() { return 4; }
    @Override public CageKind cageKind() { return CageKind.BIRD; }
    @Override public boolean isFood(ItemStack s) { return s.is(net.minecraft.tags.ItemTags.FLOWERS); }
    @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return null; }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return null; }
    @Override protected SoundEvent getDeathSound() { return null; }
}
