package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_16_R3.FurnaceRecipe;
import net.minecraft.server.v1_16_R3.IRecipe;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.NonNullList;
import net.minecraft.server.v1_16_R3.PacketPlayOutRecipeUpdate;
import net.minecraft.server.v1_16_R3.RecipeBlasting;
import net.minecraft.server.v1_16_R3.RecipeCampfire;
import net.minecraft.server.v1_16_R3.RecipeCooking;
import net.minecraft.server.v1_16_R3.RecipeItemStack;
import net.minecraft.server.v1_16_R3.RecipeSingleItem;
import net.minecraft.server.v1_16_R3.RecipeSmithing;
import net.minecraft.server.v1_16_R3.RecipeSmoking;
import net.minecraft.server.v1_16_R3.RecipeStonecutting;
import net.minecraft.server.v1_16_R3.ShapedRecipes;
import net.minecraft.server.v1_16_R3.ShapelessRecipes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UpdateRecipesRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutRecipeUpdate> {

    private final NonNullList<?> EMPTY_LIST = NonNullList.a();

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
        for (int i = 0; i < recipes.size(); i++) {
            final IRecipe<?> recipe = apply(player, view, recipes.get(i));
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
    protected IRecipe<?> apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull IRecipe<?> recipe) {
        if (recipe instanceof RecipeCooking) {
            final RecipeCooking cooking = (RecipeCooking) recipe;
            final var result = this.mapper.apply(player, recipe.getResult(), view, ItemSlot.Recipe.COOKING_RESULT);
            if (result.item() == null) {
                return null;
            }

            final RecipeItemStack ingredient = Lookup.invoke(COOKING_INGREDIENT, cooking);

            RecipeItemStack applied = apply(player, view, ingredient, ItemSlot.Recipe.COOKING_INGREDIENT);
            if (applied == null) {
                return null;
            } else if (applied == RecipeItemStack.a) {
                if (!result.edited()) {
                    return recipe;
                }
                applied = ingredient;
            }

            final String group = Lookup.invoke(COOKING_GROUP, cooking);
            if (recipe instanceof RecipeBlasting) {
                return new RecipeBlasting(cooking.getKey(), group, applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof RecipeCampfire) {
                return new RecipeCampfire(cooking.getKey(), group, applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof FurnaceRecipe) {
                return new FurnaceRecipe(cooking.getKey(), group, applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof RecipeSmoking) {
                return new RecipeSmoking(cooking.getKey(), group, applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            }
        } else if (recipe instanceof ShapedRecipes) {
            final ShapedRecipes shaped = (ShapedRecipes) recipe;
            final var result = this.mapper.apply(player, recipe.getResult(), view, ItemSlot.Recipe.SHAPED_RESULT);
            if (result.item() == null) {
                return null;
            }

            NonNullList<RecipeItemStack> ingredients = apply(player, view, shaped.a(), "shaped:ingredient", ItemSlot.Recipe.SHAPED_INGREDIENT);
            if (ingredients == null) {
                return null;
            } else if (ingredients == this.EMPTY_LIST) {
                if (!result.edited()) {
                    return recipe;
                }
                ingredients = shaped.a();
            }

            return new ShapedRecipes(shaped.getKey(), Lookup.invoke(SHAPED_GROUP, shaped), shaped.i(), shaped.j(), ingredients, result.item());
        } else if (recipe instanceof ShapelessRecipes) {
            final ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
            final var result = this.mapper.apply(player, recipe.getResult(), view, ItemSlot.Recipe.SHAPELESS_RESULT);
            if (result.item() == null) {
                return null;
            }

            NonNullList<RecipeItemStack> ingredients = apply(player, view, shapeless.a(), "shapeless:ingredient", ItemSlot.Recipe.SHAPELESS_INGREDIENT);
            if (ingredients == null) {
                return null;
            } else if (ingredients == this.EMPTY_LIST) {
                if (!result.edited()) {
                    return recipe;
                }
                ingredients = shapeless.a();
            }

            return new ShapelessRecipes(shapeless.getKey(), Lookup.invoke(SHAPELESS_GROUP, shapeless), result.item(), ingredients);
        } else if (recipe instanceof RecipeStonecutting) {
            final RecipeStonecutting stonecutter = (RecipeStonecutting) recipe;
            final var result = this.mapper.apply(player, recipe.getResult(), view, ItemSlot.Recipe.STONECUTTER_RESULT);
            if (result.item() == null) {
                return null;
            }

            final RecipeItemStack ingredient = Lookup.invoke(SINGLE_INGREDIENT, stonecutter);

            RecipeItemStack applied = apply(player, view, ingredient, ItemSlot.Recipe.STONECUTTER_INGREDIENT);
            if (applied == null) {
                return null;
            } else if (applied == RecipeItemStack.a) {
                if (!result.edited()) {
                    return recipe;
                }
                applied = ingredient;
            }

            return new RecipeStonecutting(stonecutter.getKey(), Lookup.invoke(SINGLE_GROUP, stonecutter), applied, result.item());
        } else if (recipe instanceof RecipeSmithing) {
            final RecipeSmithing upgrade = (RecipeSmithing) recipe;
            final var result = this.mapper.apply(player, recipe.getResult(), view, ItemSlot.Recipe.TRANSFORM_RESULT);
            if (result.item() == null) {
                return null;
            }

            final RecipeItemStack[] ingredients = apply(
                    () -> apply(player, view, Lookup.invoke(TRANSFORM_BASE, upgrade), ItemSlot.Recipe.TRANSFORM_BASE),
                    () -> apply(player, view, Lookup.invoke(TRANSFORM_ADDITION, upgrade), ItemSlot.Recipe.TRANSFORM_ADDITION)
            );
            if (ingredients == null) {
                return null;
            } else if (ingredients.length == 0) {
                return recipe;
            }

            return new RecipeSmithing(upgrade.getKey(), ingredients[0], ingredients[1], result.item());
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
    protected RecipeItemStack[] apply(@NotNull Supplier<RecipeItemStack>... ingredients) {
        final RecipeItemStack[] array = new RecipeItemStack[ingredients.length];
        boolean edited = false;
        for (int i = 0; i < ingredients.length; i++) {
            final RecipeItemStack ingredient = ingredients[i].get();
            if (ingredient == null) {
                return null;
            } else {
                array[i] = ingredient;
                if (ingredient != RecipeItemStack.a) {
                    edited = true;
                }
            }
        }
        return edited ? array : new RecipeItemStack[0];
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
