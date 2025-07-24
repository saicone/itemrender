package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.render.registry.ItemRegistry;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_13_R1.FurnaceRecipe;
import net.minecraft.server.v1_13_R1.IRecipe;
import net.minecraft.server.v1_13_R1.ItemStack;
import net.minecraft.server.v1_13_R1.NonNullList;
import net.minecraft.server.v1_13_R1.PacketPlayOutRecipeUpdate;
import net.minecraft.server.v1_13_R1.RecipeItemStack;
import net.minecraft.server.v1_13_R1.ShapedRecipes;
import net.minecraft.server.v1_13_R1.ShapelessRecipes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

public class UpdateRecipesRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutRecipeUpdate> {

    private static final MethodHandle RECIPES = Lookup.getter(PacketPlayOutRecipeUpdate.class, List.class, "a");

    private static final MethodHandle FURNACE_GROUP = Lookup.getter(FurnaceRecipe.class, String.class, "group");
    private static final MethodHandle FURNACE_INGREDIENT = Lookup.getter(FurnaceRecipe.class, RecipeItemStack.class, "ingredient");

    private static final MethodHandle SHAPED_GROUP = Lookup.getter(ShapedRecipes.class, String.class, "group", "f");

    private static final MethodHandle SHAPELESS_GROUP = Lookup.getter(ShapelessRecipes.class, String.class, "group", "b");


    public UpdateRecipesRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.RECIPE;
    }

    @Override
    public @Nullable PacketPlayOutRecipeUpdate rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayOutRecipeUpdate packet) {
        final List<IRecipe> recipes = Lookup.invoke(RECIPES, packet);
        if (recipes.isEmpty()) {
            return packet;
        }
        for (int i = 0; i < recipes.size(); i++) {
            final IRecipe recipe = apply(player, view, recipes.get(i));
            if (recipe != null) {
                recipes.set(i, recipe);
            }
        }
        return packet;
    }

    @Nullable
    protected IRecipe apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull IRecipe recipe) {
        if (recipe instanceof FurnaceRecipe) {
            final FurnaceRecipe furnace = (FurnaceRecipe) recipe;
            final var result = this.mapper.apply(player, recipe.d(), view, ItemSlot.Recipe.COOKING_RESULT);

            final RecipeItemStack ingredient = Lookup.invoke(FURNACE_INGREDIENT, furnace);

            RecipeItemStack applied = apply(player, view, ingredient, ItemSlot.Recipe.COOKING_INGREDIENT);
            if (applied == null) {
                if (!result.edited()) {
                    return null;
                }
                applied = ingredient;
            }

            return new FurnaceRecipe(furnace.getKey(), Lookup.invoke(FURNACE_GROUP, furnace), applied, result.itemOrDefault(ItemRegistry.empty()), furnace.g(), furnace.h());
        } else if (recipe instanceof ShapedRecipes) {
            final ShapedRecipes shaped = (ShapedRecipes) recipe;
            final var result = this.mapper.apply(player, recipe.d(), view, ItemSlot.Recipe.SHAPED_RESULT);

            NonNullList<RecipeItemStack> ingredients = apply(player, view, shaped.e(), "shaped:ingredient", ItemSlot.Recipe.SHAPED_INGREDIENT);
            if (ingredients == null) {
                if (!result.edited()) {
                    return null;
                }
                ingredients = shaped.e();
            }

            return new ShapedRecipes(shaped.getKey(), Lookup.invoke(SHAPED_GROUP, shaped), shaped.g(), shaped.h(), ingredients, result.itemOrDefault(ItemRegistry.empty()));
        } else if (recipe instanceof ShapelessRecipes) {
            final ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
            final var result = this.mapper.apply(player, recipe.d(), view, ItemSlot.Recipe.SHAPELESS_RESULT);

            NonNullList<RecipeItemStack> ingredients = apply(player, view, shapeless.e(), "shapeless:ingredient", ItemSlot.Recipe.SHAPELESS_INGREDIENT);
            if (ingredients == null) {
                if (!result.edited()) {
                    return null;
                }
                ingredients = shapeless.e();
            }

            return new ShapelessRecipes(shapeless.getKey(), Lookup.invoke(SHAPELESS_GROUP, shapeless), result.itemOrDefault(ItemRegistry.empty()), ingredients);
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
