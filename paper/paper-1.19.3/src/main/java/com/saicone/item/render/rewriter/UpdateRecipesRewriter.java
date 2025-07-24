package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

public class UpdateRecipesRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundUpdateRecipesPacket> {

    private static final MethodHandle COOKING_INGREDIENT = Lookup.getter(AbstractCookingRecipe.class, Ingredient.class, "ingredient", "d");

    private static final MethodHandle SINGLE_INGREDIENT = Lookup.getter(SingleItemRecipe.class, Ingredient.class, "ingredient", "a");

    private static final MethodHandle TRANSFORM_BASE = Lookup.getter(UpgradeRecipe.class, Ingredient.class, "base", "a");
    private static final MethodHandle TRANSFORM_ADDITION = Lookup.getter(UpgradeRecipe.class, Ingredient.class, "addition", "b");

    public UpdateRecipesRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.RECIPE;
    }

    @Override
    public @Nullable ClientboundUpdateRecipesPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundUpdateRecipesPacket packet) {
        final List<Recipe<?>> recipes = packet.getRecipes();
        if (recipes.isEmpty()) {
            return packet;
        }
        for (int i = 0; i < recipes.size(); i++) {
            final Recipe<?> recipe = apply(player, view, recipes.get(i));
            if (recipe != null) {
                recipes.set(i, recipe);
            }
        }
        return packet;
    }


    @Nullable
    protected Recipe<?> apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull Recipe<?> recipe) {
        if (recipe instanceof AbstractCookingRecipe cooking) {
            final var result = this.mapper.apply(player, recipe.getResultItem(), view, ItemSlot.Recipe.COOKING_RESULT);

            final Ingredient ingredient = Lookup.invoke(COOKING_INGREDIENT, cooking);

            Ingredient applied = apply(player, view, ingredient, ItemSlot.Recipe.COOKING_INGREDIENT);
            if (applied == null) {
                if (!result.edited()) {
                    return null;
                }
                applied = ingredient;
            }

            if (recipe instanceof BlastingRecipe) {
                return new BlastingRecipe(cooking.getId(), cooking.getGroup(), cooking.category(), applied, result.itemOrDefault(ItemStack.EMPTY), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof CampfireCookingRecipe) {
                return new CampfireCookingRecipe(cooking.getId(), cooking.getGroup(), cooking.category(), applied, result.itemOrDefault(ItemStack.EMPTY), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof SmeltingRecipe) {
                return new SmeltingRecipe(cooking.getId(), cooking.getGroup(), cooking.category(), applied, result.itemOrDefault(ItemStack.EMPTY), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof SmokingRecipe) {
                return new SmokingRecipe(cooking.getId(), cooking.getGroup(), cooking.category(), applied, result.itemOrDefault(ItemStack.EMPTY), cooking.getExperience(), cooking.getCookingTime());
            }
        } else if (recipe instanceof ShapedRecipe shaped) {
            final var result = this.mapper.apply(player, recipe.getResultItem(), view, ItemSlot.Recipe.SHAPED_RESULT);

            NonNullList<Ingredient> ingredients = apply(player, view, shaped.getIngredients(), "shaped:ingredient", ItemSlot.Recipe.SHAPED_INGREDIENT);
            if (ingredients == null) {
                if (!result.edited()) {
                    return null;
                }
                ingredients = shaped.getIngredients();
            }

            return new ShapedRecipe(shaped.getId(), shaped.getGroup(), shaped.category(), shaped.getWidth(), shaped.getHeight(), ingredients, result.itemOrDefault(ItemStack.EMPTY));
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            final var result = this.mapper.apply(player, recipe.getResultItem(), view, ItemSlot.Recipe.SHAPELESS_RESULT);

            NonNullList<Ingredient> ingredients = apply(player, view, shapeless.getIngredients(), "shapeless:ingredient", ItemSlot.Recipe.SHAPELESS_INGREDIENT);
            if (ingredients == null) {
                if (!result.edited()) {
                    return null;
                }
                ingredients = shapeless.getIngredients();
            }

            return new ShapelessRecipe(shapeless.getId(), shapeless.getGroup(), shapeless.category(), result.itemOrDefault(ItemStack.EMPTY), ingredients);
        } else if (recipe instanceof StonecutterRecipe stonecutter) {
            final var result = this.mapper.apply(player, recipe.getResultItem(), view, ItemSlot.Recipe.STONECUTTER_RESULT);

            final Ingredient ingredient = Lookup.invoke(SINGLE_INGREDIENT, stonecutter);

            Ingredient applied = apply(player, view, ingredient, ItemSlot.Recipe.STONECUTTER_INGREDIENT);
            if (applied == null) {
                if (!result.edited()) {
                    return null;
                }
                applied = ingredient;
            }

            return new StonecutterRecipe(stonecutter.getId(), stonecutter.getGroup(), applied, result.itemOrDefault(ItemStack.EMPTY));
        } else if (recipe instanceof UpgradeRecipe upgrade) {
            final var result = this.mapper.apply(player, recipe.getResultItem(), view, ItemSlot.Recipe.TRANSFORM_RESULT);

            Ingredient base = apply(player, view, Lookup.invoke(TRANSFORM_BASE, upgrade), ItemSlot.Recipe.TRANSFORM_BASE);
            Ingredient addition = apply(player, view, Lookup.invoke(TRANSFORM_ADDITION, upgrade), ItemSlot.Recipe.TRANSFORM_ADDITION);

            if (!result.edited() && base == null && addition == null) {
                return null;
            }

            if (base == null) {
                base = Lookup.invoke(TRANSFORM_BASE, upgrade);
            }

            if (addition == null) {
                addition = Lookup.invoke(TRANSFORM_ADDITION, upgrade);
            }

            return new UpgradeRecipe(upgrade.getId(), base, addition, result.itemOrDefault(ItemStack.EMPTY));
        }
        return null;
    }

    @Nullable
    protected NonNullList<Ingredient> apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull NonNullList<Ingredient> ingredients, @NotNull String slotType, @NotNull ItemSlot[] slots) {
        if (ingredients.isEmpty()) {
            return null;
        }
        final NonNullList<Ingredient> list = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
        boolean edited = false;
        for (int i = 0; i < ingredients.size(); i++) {
            final Ingredient ingredient = ingredients.get(i);
            final Ingredient result = apply(player, view, ingredient, i < 9 ? slots[i] : ItemSlot.pair(slotType, i));
            if (result == null) {
                list.add(ingredient);
            } else {
                list.add(result);
                edited = true;
            }
        }
        if (edited) {
            return list;
        }
        list.clear();
        return null;
    }

    @Nullable
    protected Ingredient apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull Ingredient ingredient, @NotNull ItemSlot slot) {
        if (ingredient == Ingredient.EMPTY || ingredient.getItems().length == 0) {
            return null;
        }
        final List<Ingredient.Value> items = new ArrayList<>(ingredient.getItems().length);
        boolean edited = false;
        for (ItemStack item : ingredient.getItems()) {
            if (item == null) {
                continue;
            }
            final var result = this.mapper.apply(player, item, view, slot);
            if (result.edited()) {
                items.add(new Ingredient.ItemValue(result.itemOrDefault(ItemStack.EMPTY)));
                edited = true;
            } else {
                items.add(new Ingredient.ItemValue(item));
            }
        }
        if (edited) {
            return new Ingredient(items.stream());
        }
        items.clear();
        return null;
    }
}
