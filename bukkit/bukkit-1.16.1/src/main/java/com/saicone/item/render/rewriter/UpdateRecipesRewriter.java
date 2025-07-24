package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.render.registry.ItemRegistry;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_16_R1.FurnaceRecipe;
import net.minecraft.server.v1_16_R1.IRecipe;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.NonNullList;
import net.minecraft.server.v1_16_R1.PacketPlayOutRecipeUpdate;
import net.minecraft.server.v1_16_R1.RecipeBlasting;
import net.minecraft.server.v1_16_R1.RecipeCampfire;
import net.minecraft.server.v1_16_R1.RecipeCooking;
import net.minecraft.server.v1_16_R1.RecipeItemStack;
import net.minecraft.server.v1_16_R1.RecipeSingleItem;
import net.minecraft.server.v1_16_R1.RecipeSmithing;
import net.minecraft.server.v1_16_R1.RecipeSmoking;
import net.minecraft.server.v1_16_R1.RecipeStonecutting;
import net.minecraft.server.v1_16_R1.ShapedRecipes;
import net.minecraft.server.v1_16_R1.ShapelessRecipes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

public class UpdateRecipesRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutRecipeUpdate> {

    private static final MethodHandle RECIPES = Lookup.getter(PacketPlayOutRecipeUpdate.class, List.class, "a");

    private static final MethodHandle COOKING_GROUP = Lookup.getter(RecipeCooking.class, String.class, "group");
    private static final MethodHandle COOKING_INGREDIENT = Lookup.getter(RecipeCooking.class, RecipeItemStack.class, "ingredient");

    private static final MethodHandle SHAPED_GROUP = Lookup.getter(ShapedRecipes.class, String.class, "group");

    private static final MethodHandle SHAPELESS_GROUP = Lookup.getter(ShapelessRecipes.class, String.class, "group");

    private static final MethodHandle SINGLE_GROUP = Lookup.getter(RecipeSingleItem.class, String.class, "group");
    private static final MethodHandle SINGLE_INGREDIENT = Lookup.getter(RecipeSingleItem.class, RecipeItemStack.class, "ingredient");

    private static final MethodHandle TRANSFORM_BASE = Lookup.getter(RecipeSmithing.class, RecipeItemStack.class, "a");
    private static final MethodHandle TRANSFORM_ADDITION = Lookup.getter(RecipeSmithing.class, RecipeItemStack.class, "b");


    public UpdateRecipesRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.RECIPE;
    }

    @Override
    public @Nullable PacketPlayOutRecipeUpdate rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayOutRecipeUpdate packet) {
        final List<IRecipe<?>> recipes = Lookup.invoke(RECIPES, packet);
        if (recipes.isEmpty()) {
            return packet;
        }
        for (int i = 0; i < recipes.size(); i++) {
            final IRecipe<?> recipe = apply(player, view, recipes.get(i));
            if (recipe != null) {
                recipes.set(i, recipe);
            }
        }
        return packet;
    }

    @Nullable
    protected IRecipe<?> apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull IRecipe<?> recipe) {
        if (recipe instanceof RecipeCooking) {
            final RecipeCooking cooking = (RecipeCooking) recipe;
            final var result = this.mapper.apply(player, recipe.getResult(), view, ItemSlot.Recipe.COOKING_RESULT);

            final RecipeItemStack ingredient = Lookup.invoke(COOKING_INGREDIENT, cooking);

            RecipeItemStack applied = apply(player, view, ingredient, ItemSlot.Recipe.COOKING_INGREDIENT);
            if (applied == null) {
                if (!result.edited()) {
                    return null;
                }
                applied = ingredient;
            }

            final String group = Lookup.invoke(COOKING_GROUP, cooking);
            if (recipe instanceof RecipeBlasting) {
                return new RecipeBlasting(cooking.getKey(), group, applied, result.itemOrDefault(ItemRegistry.empty()), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof RecipeCampfire) {
                return new RecipeCampfire(cooking.getKey(), group, applied, result.itemOrDefault(ItemRegistry.empty()), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof FurnaceRecipe) {
                return new FurnaceRecipe(cooking.getKey(), group, applied, result.itemOrDefault(ItemRegistry.empty()), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof RecipeSmoking) {
                return new RecipeSmoking(cooking.getKey(), group, applied, result.itemOrDefault(ItemRegistry.empty()), cooking.getExperience(), cooking.getCookingTime());
            }
        } else if (recipe instanceof ShapedRecipes) {
            final ShapedRecipes shaped = (ShapedRecipes) recipe;
            final var result = this.mapper.apply(player, recipe.getResult(), view, ItemSlot.Recipe.SHAPED_RESULT);

            NonNullList<RecipeItemStack> ingredients = apply(player, view, shaped.a(), "shaped:ingredient", ItemSlot.Recipe.SHAPED_INGREDIENT);
            if (ingredients == null) {
                if (!result.edited()) {
                    return null;
                }
                ingredients = shaped.a();
            }

            return new ShapedRecipes(shaped.getKey(), Lookup.invoke(SHAPED_GROUP, shaped), shaped.i(), shaped.j(), ingredients, result.itemOrDefault(ItemRegistry.empty()));
        } else if (recipe instanceof ShapelessRecipes) {
            final ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
            final var result = this.mapper.apply(player, recipe.getResult(), view, ItemSlot.Recipe.SHAPELESS_RESULT);

            NonNullList<RecipeItemStack> ingredients = apply(player, view, shapeless.a(), "shapeless:ingredient", ItemSlot.Recipe.SHAPELESS_INGREDIENT);
            if (ingredients == null) {
                if (!result.edited()) {
                    return null;
                }
                ingredients = shapeless.a();
            }

            return new ShapelessRecipes(shapeless.getKey(), Lookup.invoke(SHAPELESS_GROUP, shapeless), result.itemOrDefault(ItemRegistry.empty()), ingredients);
        } else if (recipe instanceof RecipeStonecutting) {
            final RecipeStonecutting stonecutter = (RecipeStonecutting) recipe;
            final var result = this.mapper.apply(player, recipe.getResult(), view, ItemSlot.Recipe.STONECUTTER_RESULT);

            final RecipeItemStack ingredient = Lookup.invoke(SINGLE_INGREDIENT, stonecutter);

            RecipeItemStack applied = apply(player, view, ingredient, ItemSlot.Recipe.STONECUTTER_INGREDIENT);
            if (applied == null) {
                if (!result.edited()) {
                    return null;
                }
                applied = ingredient;
            }

            return new RecipeStonecutting(stonecutter.getKey(), Lookup.invoke(SINGLE_GROUP, stonecutter), applied, result.itemOrDefault(ItemRegistry.empty()));
        } else if (recipe instanceof RecipeSmithing) {
            final RecipeSmithing upgrade = (RecipeSmithing) recipe;
            final var result = this.mapper.apply(player, recipe.getResult(), view, ItemSlot.Recipe.TRANSFORM_RESULT);

            RecipeItemStack base = apply(player, view, Lookup.invoke(TRANSFORM_BASE, upgrade), ItemSlot.Recipe.TRANSFORM_BASE);
            RecipeItemStack addition = apply(player, view, Lookup.invoke(TRANSFORM_ADDITION, upgrade), ItemSlot.Recipe.TRANSFORM_ADDITION);

            if (!result.edited() && base == null && addition == null) {
                return null;
            }

            if (base == null) {
                base = Lookup.invoke(TRANSFORM_BASE, upgrade);
            }

            if (addition == null) {
                addition = Lookup.invoke(TRANSFORM_ADDITION, upgrade);
            }

            return new RecipeSmithing(upgrade.getKey(), base, addition, result.itemOrDefault(ItemRegistry.empty()));
        }
        return null;
    }

    @Nullable
    protected NonNullList<RecipeItemStack> apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull NonNullList<RecipeItemStack> ingredients, @NotNull String slotType, @NotNull ItemSlot[] slots) {
        if (ingredients.isEmpty()) {
            return null;
        }
        final NonNullList<RecipeItemStack> list = NonNullList.a(ingredients.size(), RecipeItemStack.a);
        boolean edited = false;
        for (int i = 0; i < ingredients.size(); i++) {
            final RecipeItemStack ingredient = ingredients.get(i);
            final RecipeItemStack result = apply(player, view, ingredient, i < 9 ? slots[i] : ItemSlot.pair(slotType, i));
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
    protected RecipeItemStack apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull RecipeItemStack ingredient, @NotNull ItemSlot slot) {
        if (ingredient == RecipeItemStack.a) { // empty item
            return null;
        }
        ingredient.buildChoices(); // Ensure choices are available to use
        final List<RecipeItemStack.Provider> items = new ArrayList<>(ingredient.choices.length);
        boolean edited = false;
        for (ItemStack item : ingredient.choices) {
            if (item == null) {
                continue;
            }
            final var result = this.mapper.apply(player, item, view, slot);
            if (result.edited()) {
                items.add(new RecipeItemStack.StackProvider(result.itemOrDefault(ItemRegistry.empty())));
                edited = true;
            } else {
                items.add(new RecipeItemStack.StackProvider(item));
            }
        }
        if (edited) {
            return new RecipeItemStack(items.stream());
        }
        items.clear();
        return null;
    }
}
