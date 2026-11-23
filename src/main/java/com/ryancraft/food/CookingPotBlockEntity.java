package com.ryancraft.food;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/** What's in a cooking pot: its base, the raw ingredients, how far along they are, and once done the meal and portions left. */
public class CookingPotBlockEntity extends BlockEntity implements FuelHolder {
    public static final int COOK_TICKS = 600;
    private final List<ItemStack> ingredients = new ArrayList<>();
    private int progress, portions, heat;
    /** Ticks on the fire before the pot comes to the boil. */
    public static final int BOIL_TICKS = 600;
    private Meal meal;
    /** A pot sitting right in a campfire keeps that fire going (food/CampfireFuel). */
    private final net.minecraft.core.NonNullList<ItemStack> fuel = net.minecraft.core.NonNullList.withSize(1, ItemStack.EMPTY);
    private int burn = -1, burnTotal;

    @Override public net.minecraft.core.NonNullList<ItemStack> ryancraft$fuel() { return fuel; }
    @Override public int ryancraft$burn() { return burn; }
    @Override public void ryancraft$setBurn(int b) { burn = b; }
    @Override public int ryancraft$total() { return burnTotal; }
    @Override public void ryancraft$setTotal(int t) { burnTotal = t; }
    @Override public void ryancraft$changed() { setChanged(); }
    boolean inFire() { return getBlockState().getValue(CookingPotBlock.SUPPORT) == CookingPotBlock.Support.FIRE; }

    public CookingPotBlockEntity(BlockPos pos, BlockState state) { super(Cooking.POT_ENTITY, pos, state); }

    private CookingPotBlock.Fill fill() { return getBlockState().getValue(CookingPotBlock.FILL); }
    private void setFill(CookingPotBlock.Fill f) { if (level != null) level.setBlock(worldPosition, getBlockState().setValue(CookingPotBlock.FILL, f), 3); }
    int ingredientCount() { return ingredients.size(); }
    int portions() { return portions; }

    void setBase(CookingPotBlock.Fill base) { setFill(base); progress = 0; recolour(); setChanged(); }
    void add(ItemStack s) { ingredients.add(s); progress = 0; recolour(); setChanged(); }
    ItemStack takeLast() { ItemStack s = ingredients.remove(ingredients.size() - 1); progress = 0; recolour(); setChanged(); return s; }

    /** Tints the liquid for its base and what's in it (or the finished meal). */
    private void recolour() {
        if (level == null) return;
        boolean milk = fill() == CookingPotBlock.Fill.MILK || fill() == CookingPotBlock.Fill.CHOWDER || fill() == CookingPotBlock.Fill.PORRIDGE;
        List<Item> items = meal != null ? meal.ingredients() : ingredients.stream().map(ItemStack::getItem).toList();
        int t = PotColour.pot(milk, items);
        if (getBlockState().getValue(CookingPotBlock.TINT) != t) level.setBlock(worldPosition, getBlockState().setValue(CookingPotBlock.TINT, t), 3);
    }

    void tick() {
        if (inFire() && getBlockState().getValue(CookingPotBlock.LIT) && level instanceof net.minecraft.server.level.ServerLevel sl && !CampfireFuel.burn(sl, this)) {
            level.setBlock(worldPosition, getBlockState().setValue(CookingPotBlock.LIT, false), 3);
            CampfireFuel.goOut(level, worldPosition);
        }
        boolean milk = fill() == CookingPotBlock.Fill.MILK;
        boolean hot = CookingPotBlock.heated(level, worldPosition);
        heat = hot ? Math.min(BOIL_TICKS, heat + 1) : Math.max(0, heat - 3);           // comes to the boil slowly, cools faster
        boolean boiling = fill() != CookingPotBlock.Fill.EMPTY && heat >= BOIL_TICKS;
        if (boiling != getBlockState().getValue(CookingPotBlock.COOKING)) level.setBlock(worldPosition, getBlockState().setValue(CookingPotBlock.COOKING, boiling), 3);
        boolean can = boiling && (milk || fill() == CookingPotBlock.Fill.WATER) && !ingredients.isEmpty();
        if (!can) return;
        if (++progress < COOK_TICKS) return;
        List<Item> items = ingredients.stream().map(ItemStack::getItem).toList();
        meal = new Meal(Meal.kindFor(milk, items), new ArrayList<>(items));
        portions = items.size();
        ingredients.clear(); progress = 0;
        setFill(CookingPotBlock.Fill.valueOf(meal.kind().toUpperCase()));
        recolour();
        setChanged();
    }

    ItemStack serve() {
        ItemStack out = new ItemStack(Cooking.MEAL);
        out.set(Cooking.MEAL_DATA, meal);
        out.set(DataComponents.FOOD, meal.portion(meal.ingredients().size()));
        out.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(meal.kind()), List.of()));
        if (--portions <= 0) { portions = 0; meal = null; setFill(CookingPotBlock.Fill.EMPTY); recolour(); }
        setChanged();
        return out;
    }

    Component describe() {
        Component pot = potLine();
        return inFire() ? pot.copy().append(Component.literal("  |  ")).append(CampfireFuel.describe(this, getBlockState().getValue(CookingPotBlock.LIT))) : pot;
    }

    private Component potLine() {
        if (meal != null) return Component.translatable("block.ryancraft.cooking_pot.meal", meal.name(), portions);
        CookingPotBlock.Fill f = fill();
        if (f == CookingPotBlock.Fill.EMPTY) return Component.translatable("block.ryancraft.cooking_pot.empty");
        String base = f == CookingPotBlock.Fill.MILK ? "milk" : "water";
        if (ingredients.isEmpty()) return Component.translatable("block.ryancraft.cooking_pot.base_" + base);
        Component list = Component.empty();
        for (int i = 0; i < ingredients.size(); i++) { if (i > 0) list = list.copy().append(", "); list = list.copy().append(ingredients.get(i).getHoverName()); }
        boolean hot = CookingPotBlock.heated(level, worldPosition);
        if (hot && heat < BOIL_TICKS) return Component.translatable("block.ryancraft.cooking_pot.heating_" + base, list, heat * 100 / BOIL_TICKS);
        return Component.translatable("block.ryancraft.cooking_pot." + (hot ? "simmering_" : "cold_") + base, list, progress * 100 / COOK_TICKS);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) for (ItemStack s : ingredients) Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, s);
        ingredients.clear();
        if (level != null && !fuel.get(0).isEmpty()) Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, fuel.get(0));
        fuel.set(0, ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store("Ingredients", ItemStack.CODEC.listOf(), ingredients);
        out.putInt("Progress", progress); out.putInt("Portions", portions); out.putInt("Heat", heat);
        if (meal != null) out.store("Meal", Meal.CODEC, meal);
        net.minecraft.world.ContainerHelper.saveAllItems(out.child("Fuel"), fuel);
        out.putInt("Burn", burn); out.putInt("BurnTotal", burnTotal);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        ingredients.clear(); in.read("Ingredients", ItemStack.CODEC.listOf()).ifPresent(ingredients::addAll);
        progress = in.getIntOr("Progress", 0); portions = in.getIntOr("Portions", 0); heat = in.getIntOr("Heat", 0);
        meal = in.read("Meal", Meal.CODEC).orElse(null);
        fuel.set(0, ItemStack.EMPTY); in.child("Fuel").ifPresent(c -> net.minecraft.world.ContainerHelper.loadAllItems(c, fuel));
        burn = in.getIntOr("Burn", -1); burnTotal = in.getIntOr("BurnTotal", 0);
    }
}
