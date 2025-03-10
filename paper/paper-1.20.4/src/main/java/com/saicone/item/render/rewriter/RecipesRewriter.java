package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.mapper.AbstractItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.bukkit.craftbukkit.v1_20_R3.CraftRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class RecipesRewriter<PlayerT> implements PacketRewriter<PlayerT, ClientboundUpdateRecipesPacket> {

    private final NonNullList<?> EMPTY_LIST = NonNullList.create();
    private final Object DUMMY_OBJECT = new Object();

    private static final MethodHandle COOKING_INGREDIENT;

    private static final MethodHandle SINGLE_INGREDIENT;

    private static final MethodHandle TRANSFORM_TEMPLATE;
    private static final MethodHandle TRANSFORM_BASE;
    private static final MethodHandle TRANSFORM_ADDITION;

    private static final MethodHandle TRIM_TEMPLATE;
    private static final MethodHandle TRIM_BASE;
    private static final MethodHandle TRIM_ADDITION;

    static {
        MethodHandle cooking$ingredient = null;

        MethodHandle single$ingredient = null;

        MethodHandle transform$template = null;
        MethodHandle transform$base = null;
        MethodHandle transform$addition = null;

        MethodHandle trim$template = null;
        MethodHandle trim$base = null;
        MethodHandle trim$addition = null;
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            final Field AbstractCookingRecipe$ingredient = AbstractCookingRecipe.class.getDeclaredField("d");
            AbstractCookingRecipe$ingredient.setAccessible(true);
            cooking$ingredient = lookup.unreflectGetter(AbstractCookingRecipe$ingredient);

            final Field SingleItemRecipe$ingredient = SingleItemRecipe.class.getDeclaredField("a");
            SingleItemRecipe$ingredient.setAccessible(true);
            single$ingredient = lookup.unreflectGetter(SingleItemRecipe$ingredient);

            final Field SmithingTransformRecipe$template = SmithingTransformRecipe.class.getDeclaredField("a");
            SmithingTransformRecipe$template.setAccessible(true);
            transform$template = lookup.unreflectGetter(SmithingTransformRecipe$template);
            final Field SmithingTransformRecipe$base = SmithingTransformRecipe.class.getDeclaredField("b");
            SmithingTransformRecipe$base.setAccessible(true);
            transform$base = lookup.unreflectGetter(SmithingTransformRecipe$base);
            final Field SmithingTransformRecipe$addition = SmithingTransformRecipe.class.getDeclaredField("c");
            SmithingTransformRecipe$addition.setAccessible(true);
            transform$addition = lookup.unreflectGetter(SmithingTransformRecipe$addition);

            final Field SmithingTrimRecipe$template = SmithingTrimRecipe.class.getDeclaredField("a");
            SmithingTrimRecipe$template.setAccessible(true);
            trim$template = lookup.unreflectGetter(SmithingTrimRecipe$template);
            final Field SmithingTrimRecipe$base = SmithingTrimRecipe.class.getDeclaredField("b");
            SmithingTrimRecipe$base.setAccessible(true);
            trim$base = lookup.unreflectGetter(SmithingTrimRecipe$base);
            final Field SmithingTrimRecipe$addition = SmithingTrimRecipe.class.getDeclaredField("c");
            SmithingTrimRecipe$addition.setAccessible(true);
            trim$addition = lookup.unreflectGetter(SmithingTrimRecipe$addition);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        COOKING_INGREDIENT = cooking$ingredient;

        SINGLE_INGREDIENT = single$ingredient;

        TRANSFORM_TEMPLATE = transform$template;
        TRANSFORM_BASE = transform$base;
        TRANSFORM_ADDITION = transform$addition;

        TRIM_TEMPLATE = trim$template;
        TRIM_BASE = trim$base;
        TRIM_ADDITION = trim$addition;
    }

    protected final AbstractItemMapper<PlayerT, ItemStack> mapper;

    public RecipesRewriter(@NotNull AbstractItemMapper<PlayerT, ItemStack> mapper) {
        this.mapper = mapper;
    }

    @Override
    public @Nullable ClientboundUpdateRecipesPacket rewrite(@NotNull PlayerT player, @NotNull ClientboundUpdateRecipesPacket packet) {
        final List<RecipeHolder<?>> recipes = packet.getRecipes();
        for (int i = 0; i < recipes.size(); i++) {
            final RecipeHolder<?> holder = recipes.get(i);
            final Object recipe = apply(player, holder.value());
            if (recipe == null) {
                recipes.remove(i);
                i--;
            } else if (recipe != DUMMY_OBJECT) {
                recipes.set(i, new RecipeHolder<>(holder.id(), (Recipe<?>) recipe));
            }
        }
        if (recipes.isEmpty()) {
            return null;
        }
        return packet;
    }


    @Nullable
    protected Object apply(@NotNull PlayerT player, @NotNull Recipe<?> recipe) {
        if (recipe instanceof AbstractCookingRecipe cooking) {
            final var result = this.mapper.apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), ItemView.RECIPE, ItemSlot.Recipe.COOKING_RESULT);
            if (result.item() == null) {
                return null;
            }

            final Ingredient ingredient;
            try {
                ingredient = (Ingredient) COOKING_INGREDIENT.invoke(cooking);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            Ingredient applied = apply(player, ingredient, ItemSlot.Recipe.COOKING_INGREDIENT);
            if (applied == null) {
                return null;
            } else if (applied == Ingredient.EMPTY) {
                if (!result.edited()) {
                    return DUMMY_OBJECT;
                }
                applied = ingredient;
            }

            if (recipe instanceof BlastingRecipe) {
                return new BlastingRecipe(cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof CampfireCookingRecipe) {
                return new CampfireCookingRecipe(cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof SmeltingRecipe) {
                return new SmeltingRecipe(cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof SmokingRecipe) {
                return new SmokingRecipe(cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            }
        } else if (recipe instanceof ShapedRecipe shaped) {
            final var result = this.mapper.apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), ItemView.RECIPE, ItemSlot.Recipe.SHAPED_RESULT);
            if (result.item() == null) {
                return null;
            }

            NonNullList<Ingredient> ingredients = apply(player, shaped.getIngredients(), "shaped:ingredient", ItemSlot.Recipe.SHAPED_INGREDIENT);
            if (ingredients == null) {
                return null;
            } else if (ingredients == EMPTY_LIST) {
                if (!result.edited()) {
                    return DUMMY_OBJECT;
                }
                ingredients = shaped.getIngredients();
            }

            return new ShapedRecipe(shaped.getGroup(), shaped.category(), new ShapedRecipePattern(shaped.getWidth(), shaped.getHeight(), ingredients, Optional.empty()), result.item(), shaped.showNotification());
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            final var result = this.mapper.apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), ItemView.RECIPE, ItemSlot.Recipe.SHAPELESS_RESULT);
            if (result.item() == null) {
                return null;
            }

            NonNullList<Ingredient> ingredients = apply(player, shapeless.getIngredients(), "shapeless:ingredient", ItemSlot.Recipe.SHAPELESS_INGREDIENT);
            if (ingredients == null) {
                return null;
            } else if (ingredients == EMPTY_LIST) {
                if (!result.edited()) {
                    return DUMMY_OBJECT;
                }
                ingredients = shapeless.getIngredients();
            }

            return new ShapelessRecipe(shapeless.getGroup(), shapeless.category(), result.item(), ingredients);
        } else if (recipe instanceof SmithingTransformRecipe transform) {
            final var result = this.mapper.apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), ItemView.RECIPE, ItemSlot.Recipe.TRANSFORM_RESULT);
            if (result.item() == null) {
                return null;
            }

            final Ingredient[] ingredients = apply(
                    () -> apply(player, transform, TRANSFORM_TEMPLATE, ItemSlot.Recipe.TRANSFORM_TEMPLATE),
                    () -> apply(player, transform, TRANSFORM_BASE, ItemSlot.Recipe.TRANSFORM_BASE),
                    () -> apply(player, transform, TRANSFORM_ADDITION, ItemSlot.Recipe.TRANSFORM_ADDITION)
            );
            if (ingredients == null) {
                return null;
            } else if (ingredients.length == 0) {
                return DUMMY_OBJECT;
            }

            return new SmithingTransformRecipe(ingredients[0], ingredients[1], ingredients[2], result.item());
        } else if (recipe instanceof SmithingTrimRecipe trim) {
            final Ingredient[] ingredients = apply(
                    () -> apply(player, trim, TRIM_TEMPLATE, ItemSlot.Recipe.TRIM_TEMPLATE),
                    () -> apply(player, trim, TRIM_BASE, ItemSlot.Recipe.TRIM_BASE),
                    () -> apply(player, trim, TRIM_ADDITION, ItemSlot.Recipe.TRIM_ADDITION)
            );
            if (ingredients == null) {
                return null;
            } else if (ingredients.length == 0) {
                return DUMMY_OBJECT;
            }

            return new SmithingTrimRecipe(ingredients[0], ingredients[1], ingredients[2]);
        } else if (recipe instanceof StonecutterRecipe stonecutter) {
            final var result = this.mapper.apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), ItemView.RECIPE, ItemSlot.Recipe.STONECUTTER_RESULT);
            if (result.item() == null) {
                return null;
            }

            final Ingredient ingredient;
            try {
                ingredient = (Ingredient) SINGLE_INGREDIENT.invoke(stonecutter);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            Ingredient applied = apply(player, ingredient, ItemSlot.Recipe.STONECUTTER_INGREDIENT);
            if (applied == null) {
                return null;
            } else if (applied == Ingredient.EMPTY) {
                if (!result.edited()) {
                    return DUMMY_OBJECT;
                }
                applied = ingredient;
            }

            return new StonecutterRecipe(stonecutter.getGroup(), applied, result.item());
        }
        return DUMMY_OBJECT;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected NonNullList<Ingredient> apply(@NotNull PlayerT player, @NotNull NonNullList<Ingredient> ingredients, @NotNull String slotType, @NotNull ItemSlot[] slots) {
        final NonNullList<Ingredient> list = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
        int empty = 0;
        boolean edited = false;
        for (int i = 0; i < ingredients.size(); i++) {
            final Ingredient ingredient = ingredients.get(i);
            final Ingredient result = apply(player, ingredient, i < 9 ? slots[i] : ItemSlot.pair(slotType, i));
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
    protected Ingredient apply(@NotNull PlayerT player, @NotNull SmithingRecipe recipe, @NotNull MethodHandle field, @NotNull ItemSlot slot) {
        try {
            return apply(player, (Ingredient) field.invoke(recipe), slot);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Nullable
    protected Ingredient apply(@NotNull PlayerT player, @NotNull Ingredient ingredient, @NotNull Object slot) {
        if (ingredient == Ingredient.EMPTY) {
            return Ingredient.EMPTY;
        }
        final List<Ingredient.Value> items = new ArrayList<>(ingredient.getItems().length);
        boolean edited = false;
        for (ItemStack item : ingredient.getItems()) {
            if (item == null) {
                return null;
            }
            final var result = this.mapper.apply(player, item, ItemView.RECIPE, slot);
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
