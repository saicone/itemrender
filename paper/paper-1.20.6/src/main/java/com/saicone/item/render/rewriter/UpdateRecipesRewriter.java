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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UpdateRecipesRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundUpdateRecipesPacket> {

    private static final MethodHandle COOKING_INGREDIENT = Lookup.getter(AbstractCookingRecipe.class, Ingredient.class, "ingredient", "d");

    private static final MethodHandle SINGLE_INGREDIENT = Lookup.getter(SingleItemRecipe.class, Ingredient.class, "ingredient", "a");

    private static final MethodHandle TRANSFORM_TEMPLATE = Lookup.getter(SmithingTransformRecipe.class, Ingredient.class, "template", "a");
    private static final MethodHandle TRANSFORM_BASE = Lookup.getter(SmithingTransformRecipe.class, Ingredient.class, "base", "b");
    private static final MethodHandle TRANSFORM_ADDITION = Lookup.getter(SmithingTransformRecipe.class, Ingredient.class, "addition", "c");

    private static final MethodHandle TRIM_TEMPLATE = Lookup.getter(SmithingTrimRecipe.class, Ingredient.class, "ingredient", "a");
    private static final MethodHandle TRIM_BASE = Lookup.getter(SmithingTrimRecipe.class, Ingredient.class, "ingredient", "b");
    private static final MethodHandle TRIM_ADDITION = Lookup.getter(SmithingTrimRecipe.class, Ingredient.class, "ingredient", "c");

    public UpdateRecipesRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.RECIPE;
    }

    @Override
    public @Nullable ClientboundUpdateRecipesPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundUpdateRecipesPacket packet) {
        final List<RecipeHolder<?>> recipes = packet.getRecipes();
        if (recipes.isEmpty()) {
            return packet;
        }
        for (int i = 0; i < recipes.size(); i++) {
            final RecipeHolder<?> holder = recipes.get(i);
            final Recipe<?> recipe = apply(player, view, holder.value());
            if (recipe != null) {
                recipes.set(i, new RecipeHolder<>(holder.id(), (Recipe<?>) recipe));
            }
        }
        return packet;
    }


    @Nullable
    protected Recipe<?> apply(@NotNull PlayerT player, @NotNull ItemView view, @NotNull Recipe<?> recipe) {
        switch (recipe) {
            case AbstractCookingRecipe cooking -> {
                final var result = this.mapper.apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), view, ItemSlot.Recipe.COOKING_RESULT);

                final Ingredient ingredient = Lookup.invoke(COOKING_INGREDIENT, cooking);

                Ingredient applied = apply(player, view, ingredient, ItemSlot.Recipe.COOKING_INGREDIENT);
                if (applied == null) {
                    if (!result.edited()) {
                        return null;
                    }
                    applied = ingredient;
                }

                switch (recipe) {
                    case BlastingRecipe ignored -> {
                        return new BlastingRecipe(cooking.getGroup(), cooking.category(), applied, result.itemOrDefault(ItemStack.EMPTY), cooking.getExperience(), cooking.getCookingTime());
                    }
                    case CampfireCookingRecipe ignored -> {
                        return new CampfireCookingRecipe(cooking.getGroup(), cooking.category(), applied, result.itemOrDefault(ItemStack.EMPTY), cooking.getExperience(), cooking.getCookingTime());
                    }
                    case SmeltingRecipe ignored -> {
                        return new SmeltingRecipe(cooking.getGroup(), cooking.category(), applied, result.itemOrDefault(ItemStack.EMPTY), cooking.getExperience(), cooking.getCookingTime());
                    }
                    case SmokingRecipe ignored -> {
                        return new SmokingRecipe(cooking.getGroup(), cooking.category(), applied, result.itemOrDefault(ItemStack.EMPTY), cooking.getExperience(), cooking.getCookingTime());
                    }
                    default -> {
                    }
                }
            }
            case ShapedRecipe shaped -> {
                final var result = this.mapper.apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), view, ItemSlot.Recipe.SHAPED_RESULT);

                NonNullList<Ingredient> ingredients = apply(player, view, shaped.getIngredients(), "shaped:ingredient", ItemSlot.Recipe.SHAPED_INGREDIENT);
                if (ingredients == null) {
                    if (!result.edited()) {
                        return null;
                    }
                    ingredients = shaped.getIngredients();
                }

                return new ShapedRecipe(shaped.getGroup(), shaped.category(), new ShapedRecipePattern(shaped.getWidth(), shaped.getHeight(), ingredients, Optional.empty()), result.itemOrDefault(ItemStack.EMPTY), shaped.showNotification());
            }
            case ShapelessRecipe shapeless -> {
                final var result = this.mapper.apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), view, ItemSlot.Recipe.SHAPELESS_RESULT);

                NonNullList<Ingredient> ingredients = apply(player, view, shapeless.getIngredients(), "shapeless:ingredient", ItemSlot.Recipe.SHAPELESS_INGREDIENT);
                if (ingredients == null) {
                    if (!result.edited()) {
                        return null;
                    }
                    ingredients = shapeless.getIngredients();
                }

                return new ShapelessRecipe(shapeless.getGroup(), shapeless.category(), result.itemOrDefault(ItemStack.EMPTY), ingredients);
            }
            case SmithingTransformRecipe transform -> {
                final var result = this.mapper.apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), view, ItemSlot.Recipe.TRANSFORM_RESULT);

                Ingredient template = apply(player, view, Lookup.invoke(TRANSFORM_TEMPLATE, transform), ItemSlot.Recipe.TRANSFORM_TEMPLATE);
                Ingredient base = apply(player, view, Lookup.invoke(TRANSFORM_BASE, transform), ItemSlot.Recipe.TRANSFORM_BASE);
                Ingredient addition = apply(player, view, Lookup.invoke(TRANSFORM_ADDITION, transform), ItemSlot.Recipe.TRANSFORM_ADDITION);

                if (!result.edited() && template == null && base == null && addition == null) {
                    return null;
                }

                if (template == null) {
                    template = Lookup.invoke(TRANSFORM_TEMPLATE, transform);
                }
                if (base == null) {
                    base = Lookup.invoke(TRANSFORM_BASE, transform);
                }
                if (addition == null) {
                    addition = Lookup.invoke(TRANSFORM_ADDITION, transform);
                }

                return new SmithingTransformRecipe(template, base, addition, result.itemOrDefault(ItemStack.EMPTY));
            }
            case SmithingTrimRecipe trim -> {
                Ingredient template = apply(player, view, Lookup.invoke(TRIM_TEMPLATE, trim), ItemSlot.Recipe.TRIM_TEMPLATE);
                Ingredient base = apply(player, view, Lookup.invoke(TRIM_BASE, trim), ItemSlot.Recipe.TRIM_BASE);
                Ingredient addition = apply(player, view, Lookup.invoke(TRIM_ADDITION, trim), ItemSlot.Recipe.TRIM_ADDITION);

                if (template == null && base == null && addition == null) {
                    return null;
                }

                if (template == null) {
                    template = Lookup.invoke(TRIM_TEMPLATE, trim);
                }
                if (base == null) {
                    base = Lookup.invoke(TRIM_BASE, trim);
                }
                if (addition == null) {
                    addition = Lookup.invoke(TRIM_ADDITION, trim);
                }

                return new SmithingTrimRecipe(template, base, addition);
            }
            case StonecutterRecipe stonecutter -> {
                final var result = this.mapper.apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), view, ItemSlot.Recipe.STONECUTTER_RESULT);

                final Ingredient ingredient = Lookup.invoke(SINGLE_INGREDIENT, stonecutter);

                Ingredient applied = apply(player, view, ingredient, ItemSlot.Recipe.STONECUTTER_INGREDIENT);
                if (applied == null) {
                    if (!result.edited()) {
                        return null;
                    }
                    applied = ingredient;
                }

                return new StonecutterRecipe(stonecutter.getGroup(), applied, result.itemOrDefault(ItemStack.EMPTY));
            }
            default -> {
            }
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
