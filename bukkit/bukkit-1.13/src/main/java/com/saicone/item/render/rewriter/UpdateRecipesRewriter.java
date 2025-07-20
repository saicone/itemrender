package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
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

    private final NonNullList<?> EMPTY_LIST = NonNullList.a();

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
        for (int i = 0; i < recipes.size(); i++) {
            final IRecipe recipe = apply(player, view, recipes.get(i));
            if (recipe == null) {
                recipes.remove(i);
                i--;
            } else {
                recipes.set(i, recipe);
            }
        }
        if (recipes.isEmpty()) {
            return null;
        }
        return packet;
    }

    @Nullable
    protected IRecipe apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull IRecipe recipe) {
        if (recipe instanceof FurnaceRecipe) {
            final FurnaceRecipe furnace = (FurnaceRecipe) recipe;
            final var result = this.mapper.apply(player, recipe.d(), view, ItemSlot.Recipe.COOKING_RESULT);
            if (result.item() == null) {
                return null;
            }

            final RecipeItemStack ingredient = Lookup.invoke(FURNACE_INGREDIENT, furnace);

            RecipeItemStack applied = apply(player, view, ingredient, ItemSlot.Recipe.COOKING_INGREDIENT);
            if (applied == null) {
                return null;
            } else if (applied == RecipeItemStack.a) {
                if (!result.edited()) {
                    return recipe;
                }
                applied = ingredient;
            }

            return new FurnaceRecipe(furnace.getKey(), Lookup.invoke(FURNACE_GROUP, furnace), applied, result.item(), furnace.g(), furnace.h());
        } else if (recipe instanceof ShapedRecipes) {
            final ShapedRecipes shaped = (ShapedRecipes) recipe;
            final var result = this.mapper.apply(player, recipe.d(), view, ItemSlot.Recipe.SHAPED_RESULT);
            if (result.item() == null) {
                return null;
            }

            NonNullList<RecipeItemStack> ingredients = apply(player, view, shaped.e(), "shaped:ingredient", ItemSlot.Recipe.SHAPED_INGREDIENT);
            if (ingredients == null) {
                return null;
            } else if (ingredients == this.EMPTY_LIST) {
                if (!result.edited()) {
                    return recipe;
                }
                ingredients = shaped.e();
            }

            return new ShapedRecipes(shaped.getKey(), Lookup.invoke(SHAPED_GROUP, shaped), shaped.g(), shaped.h(), ingredients, result.item());
        } else if (recipe instanceof ShapelessRecipes) {
            final ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
            final var result = this.mapper.apply(player, recipe.d(), view, ItemSlot.Recipe.SHAPELESS_RESULT);
            if (result.item() == null) {
                return null;
            }

            NonNullList<RecipeItemStack> ingredients = apply(player, view, shapeless.e(), "shapeless:ingredient", ItemSlot.Recipe.SHAPELESS_INGREDIENT);
            if (ingredients == null) {
                return null;
            } else if (ingredients == this.EMPTY_LIST) {
                if (!result.edited()) {
                    return recipe;
                }
                ingredients = shapeless.e();
            }

            return new ShapelessRecipes(shapeless.getKey(), Lookup.invoke(SHAPELESS_GROUP, shapeless), result.item(), ingredients);
        }
        return recipe;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected NonNullList<RecipeItemStack> apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull NonNullList<RecipeItemStack> ingredients, @NotNull String slotType, @NotNull ItemSlot[] slots) {
        NonNullList<RecipeItemStack> list = NonNullList.a(ingredients.size(), RecipeItemStack.a);
        int empty = 0;
        boolean edited = false;
        for (int i = 0; i < ingredients.size(); i++) {
            final RecipeItemStack ingredient = ingredients.get(i);
            final RecipeItemStack result = apply(player, view, ingredient, i < 9 ? slots[i] : ItemSlot.pair(slotType, i));
            if (result == RecipeItemStack.a) {
                list.add(ingredient);
                continue;
            }
            if (result == null) {
                list.add(RecipeItemStack.a);
                empty++;
            } else {
                list.add(result);
            }
            edited = true;
        }
        if (empty == list.size()) {
            return null;
        }
        list.clear();
        return edited ? list : (NonNullList<RecipeItemStack>) this.EMPTY_LIST;
    }

    @Nullable
    protected RecipeItemStack apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull RecipeItemStack ingredient, @NotNull ItemSlot slot) {
        if (ingredient == RecipeItemStack.a) {
            return RecipeItemStack.a;
        }
        final List<RecipeItemStack.Provider> items = new ArrayList<>(ingredient.choices.length);
        boolean edited = false;
        ingredient.buildChoices();
        for (ItemStack item : ingredient.choices) {
            if (item == null) {
                return null;
            }
            final var result = this.mapper.apply(player, item, view, slot);
            items.add(new RecipeItemStack.StackProvider(result.item()));
            if (result.edited()) {
                edited = true;
            }
        }
        if (edited) {
            return new RecipeItemStack(items.stream());
        }
        items.clear();
        return RecipeItemStack.a;
    }
}
