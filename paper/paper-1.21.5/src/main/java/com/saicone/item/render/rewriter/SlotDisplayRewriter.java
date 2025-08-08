package com.saicone.item.render.rewriter;

import com.saicone.item.ItemContext;
import com.saicone.item.ItemSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface SlotDisplayRewriter<PlayerT> {

    @Nullable
    default SlotDisplay rewrite(@NotNull SlotDisplay display, @NotNull Function<ItemStack, ItemContext<PlayerT, ItemStack>> context) {
        if (display instanceof SlotDisplay.ItemSlotDisplay item) {
            final var result = context.apply(new ItemStack(item.item()))
                    .apply();
            if (result.empty()) {
                return SlotDisplay.Empty.INSTANCE;
            } else if (result.edited()) {
                return new SlotDisplay.ItemSlotDisplay(result.item().getItemHolder());
            }
        } else if (display instanceof SlotDisplay.ItemStackSlotDisplay itemStack) {
            final var result = context.apply(itemStack.stack())
                    .apply();
            if (result.empty()) {
                return SlotDisplay.Empty.INSTANCE;
            } else if (result.edited()) {
                return new SlotDisplay.ItemStackSlotDisplay(result.item());
            }
        } else if (display instanceof SlotDisplay.TagSlotDisplay tag) {
            // TODO: Check this implementation later to convert TagKey into ItemStack and viceversa
        } else if (display instanceof SlotDisplay.SmithingTrimDemoSlotDisplay smithing) {
            final SlotDisplay base = rewrite(smithing.base(), context.andThen(c -> c.withSlot(ItemSlot.Recipe.TRIM_BASE)));
            final SlotDisplay material = rewrite(smithing.material(), context.andThen(c -> c.withSlot(ItemSlot.Recipe.TRIM_ADDITION)));

            if (base == null && material == null) {
                return null;
            }

            return new SlotDisplay.SmithingTrimDemoSlotDisplay(
                    base == null ? smithing.base() : base,
                    material == null ? smithing.material() : material,
                    smithing.pattern()
            );
        } else if (display instanceof SlotDisplay.WithRemainder withRemainder) {
            final SlotDisplay input = rewrite(withRemainder.input(), context);
            final SlotDisplay remainder = rewrite(withRemainder.remainder(), context);

            if (input == null && remainder == null) {
                return null;
            }

            return new SlotDisplay.WithRemainder(
                    input == null ? withRemainder.input() : input,
                    remainder == null ? withRemainder.remainder() : remainder
            );
        } else if (display instanceof SlotDisplay.Composite composite) {
            boolean edited = false;
            final List<SlotDisplay> contents = new ArrayList<>();
            for (SlotDisplay content : composite.contents()) {
                final SlotDisplay result = rewrite(content, context);
                if (result == null) {
                    contents.add(content);
                } else {
                    contents.add(result);
                    edited = true;
                }
            }

            if (edited) {
                return new SlotDisplay.Composite(contents);
            }
            contents.clear();
        }
        return null;
    }

    @Nullable
    default RecipeDisplay rewrite(@NotNull RecipeDisplay recipe, @NotNull BiFunction<ItemStack, ItemSlot, ItemContext<PlayerT, ItemStack>> context) {
        switch (recipe) {
            case FurnaceRecipeDisplay furnace -> {
                final SlotDisplay ingredient = rewrite(furnace.ingredient(), item -> context.apply(item, ItemSlot.Recipe.COOKING_INGREDIENT));
                final SlotDisplay fuel = rewrite(furnace.fuel(), item -> context.apply(item, ItemSlot.Recipe.COOKING_FUEL));
                final SlotDisplay result = rewrite(furnace.result(), item -> context.apply(item, ItemSlot.Recipe.COOKING_RESULT));
                final SlotDisplay craftingStation = rewrite(furnace.craftingStation(), item -> context.apply(item, ItemSlot.Recipe.CRAFTING_STATION));

                if (ingredient == null && fuel == null && result == null && craftingStation == null) {
                    return null;
                }

                return new FurnaceRecipeDisplay(
                        ingredient == null ? furnace.ingredient() : ingredient,
                        fuel == null ? furnace.fuel() : fuel,
                        result == null ? furnace.result() : result,
                        craftingStation == null ? furnace.craftingStation() : craftingStation,
                        furnace.duration(),
                        furnace.experience()
                );
            }
            case ShapedCraftingRecipeDisplay shaped -> {
                final List<SlotDisplay> ingredients = rewrite(shaped.ingredients(), ItemSlot.Recipe.SHAPED_INGREDIENT, context);
                final SlotDisplay result = rewrite(shaped.result(), item -> context.apply(item, ItemSlot.Recipe.SHAPED_RESULT));
                final SlotDisplay craftingStation = rewrite(shaped.craftingStation(), item -> context.apply(item, ItemSlot.Recipe.CRAFTING_STATION));

                if (ingredients == null && result == null && craftingStation == null) {
                    return null;
                }

                return new ShapedCraftingRecipeDisplay(
                        shaped.width(),
                        shaped.height(),
                        ingredients == null ? shaped.ingredients() : ingredients,
                        result == null ? shaped.result() : result,
                        craftingStation == null ? shaped.craftingStation() : craftingStation
                );
            }
            case ShapelessCraftingRecipeDisplay shapeless -> {
                final List<SlotDisplay> ingredients = rewrite(shapeless.ingredients(), ItemSlot.Recipe.SHAPELESS_INGREDIENT, context);
                final SlotDisplay result = rewrite(shapeless.result(), item -> context.apply(item, ItemSlot.Recipe.SHAPELESS_RESULT));
                final SlotDisplay craftingStation = rewrite(shapeless.craftingStation(), item -> context.apply(item, ItemSlot.Recipe.CRAFTING_STATION));

                if (ingredients == null && result == null && craftingStation == null) {
                    return null;
                }

                return new ShapelessCraftingRecipeDisplay(
                        ingredients == null ? shapeless.ingredients() : ingredients,
                        result == null ? shapeless.result() : result,
                        craftingStation == null ? shapeless.craftingStation() : craftingStation
                );
            }
            case SmithingRecipeDisplay smithing -> {
                final SlotDisplay template = rewrite(smithing.template(), item -> context.apply(item, ItemSlot.Recipe.TRANSFORM_TEMPLATE));
                final SlotDisplay base = rewrite(smithing.base(), item -> context.apply(item, ItemSlot.Recipe.TRANSFORM_BASE));
                final SlotDisplay addition = rewrite(smithing.addition(), item -> context.apply(item, ItemSlot.Recipe.TRANSFORM_ADDITION));
                final SlotDisplay result = rewrite(smithing.result(), item -> context.apply(item, ItemSlot.Recipe.TRANSFORM_RESULT));
                final SlotDisplay craftingStation = rewrite(smithing.craftingStation(), item -> context.apply(item, ItemSlot.Recipe.CRAFTING_STATION));

                if (template == null && base == null && addition == null && result == null && craftingStation == null) {
                    return recipe;
                }

                return new SmithingRecipeDisplay(
                        template == null ? smithing.template() : template,
                        base == null ? smithing.base() : base,
                        addition == null ? smithing.addition() : addition,
                        result == null ? smithing.result() : result,
                        craftingStation == null ? smithing.craftingStation() : craftingStation
                );
            }
            case StonecutterRecipeDisplay stonecutter -> {
                final SlotDisplay input = rewrite(stonecutter.input(), item -> context.apply(item, ItemSlot.Recipe.STONECUTTER_INGREDIENT));
                final SlotDisplay result = rewrite(stonecutter.result(), item -> context.apply(item, ItemSlot.Recipe.STONECUTTER_RESULT));
                final SlotDisplay craftingStation = rewrite(stonecutter.craftingStation(), item -> context.apply(item, ItemSlot.Recipe.CRAFTING_STATION));

                if (input == null && result == null && craftingStation == null) {
                    return recipe;
                }

                return new StonecutterRecipeDisplay(
                        input == null ? stonecutter.input() : input,
                        result == null ? stonecutter.result() : result,
                        craftingStation == null ? stonecutter.craftingStation() : craftingStation
                );
            }
            default -> {
                return null;
            }
        }
    }

    @Nullable
    private List<SlotDisplay> rewrite(@NotNull List<SlotDisplay> ingredients, @NotNull ItemSlot[] slots, @NotNull BiFunction<ItemStack, ItemSlot, ItemContext<PlayerT, ItemStack>> context) {
        final List<SlotDisplay> list = new ArrayList<>(ingredients.size());
        boolean edited = false;
        int index = 0;
        for (SlotDisplay ingredient : ingredients) {
            final ItemSlot slot = slots[index];
            final SlotDisplay result = rewrite(ingredient, item -> context.apply(item, slot));
            if (result == null) {
                list.add(ingredient);
            } else {
                list.add(result);
                edited = true;
            }
            index++;
        }
        return edited ? list : null;
    }
}
