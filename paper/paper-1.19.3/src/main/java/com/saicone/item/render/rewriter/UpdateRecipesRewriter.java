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
import java.util.function.Supplier;

public class UpdateRecipesRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundUpdateRecipesPacket> {

    private final NonNullList<?> EMPTY_LIST = NonNullList.create();

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
        for (int i = 0; i < recipes.size(); i++) {
            final Recipe<?> recipe = apply(player, view, recipes.get(i));
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
    protected Recipe<?> apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull Recipe<?> recipe) {
        if (recipe instanceof AbstractCookingRecipe cooking) {
            final var result = this.mapper.apply(player, recipe.getResultItem(), view, ItemSlot.Recipe.COOKING_RESULT);
            if (result.item() == null) {
                return null;
            }

            final Ingredient ingredient = Lookup.invoke(COOKING_INGREDIENT, cooking);

            Ingredient applied = apply(player, view, ingredient, ItemSlot.Recipe.COOKING_INGREDIENT);
            if (applied == null) {
                return null;
            } else if (applied == Ingredient.EMPTY) {
                if (!result.edited()) {
                    return recipe;
                }
                applied = ingredient;
            }

            if (recipe instanceof BlastingRecipe) {
                return new BlastingRecipe(cooking.getId(), cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof CampfireCookingRecipe) {
                return new CampfireCookingRecipe(cooking.getId(), cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof SmeltingRecipe) {
                return new SmeltingRecipe(cooking.getId(), cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof SmokingRecipe) {
                return new SmokingRecipe(cooking.getId(), cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            }
        } else if (recipe instanceof ShapedRecipe shaped) {
            final var result = this.mapper.apply(player, recipe.getResultItem(), view, ItemSlot.Recipe.SHAPED_RESULT);
            if (result.item() == null) {
                return null;
            }

            NonNullList<Ingredient> ingredients = apply(player, view, shaped.getIngredients(), "shaped:ingredient", ItemSlot.Recipe.SHAPED_INGREDIENT);
            if (ingredients == null) {
                return null;
            } else if (ingredients == EMPTY_LIST) {
                if (!result.edited()) {
                    return recipe;
                }
                ingredients = shaped.getIngredients();
            }

            return new ShapedRecipe(shaped.getId(), shaped.getGroup(), shaped.category(), shaped.getWidth(), shaped.getHeight(), ingredients, result.item());
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            final var result = this.mapper.apply(player, recipe.getResultItem(), view, ItemSlot.Recipe.SHAPELESS_RESULT);
            if (result.item() == null) {
                return null;
            }

            NonNullList<Ingredient> ingredients = apply(player, view, shapeless.getIngredients(), "shapeless:ingredient", ItemSlot.Recipe.SHAPELESS_INGREDIENT);
            if (ingredients == null) {
                return null;
            } else if (ingredients == EMPTY_LIST) {
                if (!result.edited()) {
                    return recipe;
                }
                ingredients = shapeless.getIngredients();
            }

            return new ShapelessRecipe(shapeless.getId(), shapeless.getGroup(), shapeless.category(), result.item(), ingredients);
        } else if (recipe instanceof StonecutterRecipe stonecutter) {
            final var result = this.mapper.apply(player, recipe.getResultItem(), view, ItemSlot.Recipe.STONECUTTER_RESULT);
            if (result.item() == null) {
                return null;
            }

            final Ingredient ingredient = Lookup.invoke(SINGLE_INGREDIENT, stonecutter);

            Ingredient applied = apply(player, view, ingredient, ItemSlot.Recipe.STONECUTTER_INGREDIENT);
            if (applied == null) {
                return null;
            } else if (applied == Ingredient.EMPTY) {
                if (!result.edited()) {
                    return recipe;
                }
                applied = ingredient;
            }

            return new StonecutterRecipe(stonecutter.getId(), stonecutter.getGroup(), applied, result.item());
        } else if (recipe instanceof UpgradeRecipe upgrade) {
            final var result = this.mapper.apply(player, recipe.getResultItem(), view, ItemSlot.Recipe.TRANSFORM_RESULT);
            if (result.item() == null) {
                return null;
            }

            final Ingredient[] ingredients = apply(
                    () -> apply(player, view, Lookup.invoke(TRANSFORM_BASE, upgrade), ItemSlot.Recipe.TRANSFORM_BASE),
                    () -> apply(player, view, Lookup.invoke(TRANSFORM_ADDITION, upgrade), ItemSlot.Recipe.TRANSFORM_ADDITION)
            );
            if (ingredients == null) {
                return null;
            } else if (ingredients.length == 0) {
                return recipe;
            }

            return new UpgradeRecipe(upgrade.getId(), ingredients[0], ingredients[1], result.item());
        }
        return recipe;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected NonNullList<Ingredient> apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull NonNullList<Ingredient> ingredients, @NotNull String slotType, @NotNull ItemSlot[] slots) {
        final NonNullList<Ingredient> list = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
        int empty = 0;
        boolean edited = false;
        for (int i = 0; i < ingredients.size(); i++) {
            final Ingredient ingredient = ingredients.get(i);
            final Ingredient result = apply(player, view, ingredient, i < 9 ? slots[i] : ItemSlot.pair(slotType, i));
            if (result == Ingredient.EMPTY) {
                list.add(ingredient);
                continue;
            }
            if (result == null) {
                list.add(Ingredient.EMPTY);
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
        return edited ? list : (NonNullList<Ingredient>) EMPTY_LIST;
    }

    @Nullable
    protected Ingredient[] apply(@NotNull Supplier<Ingredient>... ingredients) {
        final Ingredient[] array = new Ingredient[ingredients.length];
        boolean edited = false;
        for (int i = 0; i < ingredients.length; i++) {
            final Ingredient ingredient = ingredients[i].get();
            if (ingredient == null) {
                return null;
            } else {
                array[i] = ingredient;
                if (ingredient != Ingredient.EMPTY) {
                    edited = true;
                }
            }
        }
        return edited ? array : new Ingredient[0];
    }

    @Nullable
    protected Ingredient apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull Ingredient ingredient, @NotNull Object slot) {
        if (ingredient == Ingredient.EMPTY) {
            return Ingredient.EMPTY;
        }
        final List<Ingredient.Value> items = new ArrayList<>(ingredient.getItems().length);
        boolean edited = false;
        for (ItemStack item : ingredient.getItems()) {
            if (item == null) {
                return null;
            }
            final var result = this.mapper.apply(player, item, view, slot);
            items.add(new Ingredient.ItemValue(result.item()));
            if (result.edited()) {
                edited = true;
            }
        }
        if (edited) {
            return new Ingredient(items.stream());
        }
        items.clear();
        return Ingredient.EMPTY;
    }
}
