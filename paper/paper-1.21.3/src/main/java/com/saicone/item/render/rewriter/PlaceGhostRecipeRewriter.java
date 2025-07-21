package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
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

public class PlaceGhostRecipeRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundPlaceGhostRecipePacket> implements SlotDisplayRewriter<PlayerT> {

    private static final List<SlotDisplay> EMPTY_LIST = List.of();

    public PlaceGhostRecipeRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.RECIPE;
    }

    @Override
    public @Nullable ClientboundPlaceGhostRecipePacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundPlaceGhostRecipePacket packet) {
        final RecipeDisplay recipe = rewrite(player, view, packet.recipeDisplay());
        return recipe == null ? packet : new ClientboundPlaceGhostRecipePacket(packet.containerId(), recipe);
    }

    @Nullable
    private RecipeDisplay rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull RecipeDisplay recipe) {
        switch (recipe) {
            case FurnaceRecipeDisplay furnace -> {
                final SlotDisplay ingredient = rewrite(this.mapper, player, view, furnace.ingredient(), ItemSlot.Recipe.COOKING_INGREDIENT);
                if (ingredient == null) {
                    return null;
                }

                final SlotDisplay fuel = rewrite(this.mapper, player, view, furnace.fuel(), ItemSlot.Recipe.COOKING_FUEL);
                if (fuel == null) {
                    return null;
                }

                final SlotDisplay result = rewrite(this.mapper, player, view, furnace.result(), ItemSlot.Recipe.COOKING_RESULT);
                if (result == null) {
                    return null;
                }

                final SlotDisplay craftingStation = rewrite(this.mapper, player, view, furnace.craftingStation(), ItemSlot.Recipe.CRAFTING_STATION);
                if (craftingStation == null) {
                    return null;
                }

                if (ingredient == SlotDisplay.Empty.INSTANCE && fuel == SlotDisplay.Empty.INSTANCE && result == SlotDisplay.Empty.INSTANCE && craftingStation == SlotDisplay.Empty.INSTANCE) {
                    return recipe;
                }

                return new FurnaceRecipeDisplay(
                        ingredient == SlotDisplay.Empty.INSTANCE ? furnace.ingredient() : ingredient,
                        fuel == SlotDisplay.Empty.INSTANCE ? furnace.fuel() : fuel,
                        result == SlotDisplay.Empty.INSTANCE ? furnace.result() : result,
                        craftingStation == SlotDisplay.Empty.INSTANCE ? furnace.craftingStation() : craftingStation,
                        furnace.duration(),
                        furnace.experience()
                );
            }
            case ShapedCraftingRecipeDisplay shaped -> {
                final List<SlotDisplay> ingredients = rewrite(player, view, shaped.ingredients(), ItemSlot.Recipe.SHAPED_INGREDIENT);
                if (ingredients == null) {
                    return null;
                }

                final SlotDisplay result = rewrite(this.mapper, player, view, shaped.result(), ItemSlot.Recipe.SHAPED_RESULT);
                if (result == null) {
                    return null;
                }

                final SlotDisplay craftingStation = rewrite(this.mapper, player, view, shaped.craftingStation(), ItemSlot.Recipe.CRAFTING_STATION);
                if (craftingStation == null) {
                    return null;
                }

                if (ingredients == EMPTY_LIST && result == SlotDisplay.Empty.INSTANCE && craftingStation == SlotDisplay.Empty.INSTANCE) {
                    return recipe;
                }

                return new ShapedCraftingRecipeDisplay(
                        shaped.width(),
                        shaped.height(),
                        ingredients == EMPTY_LIST ? shaped.ingredients() : ingredients,
                        result == SlotDisplay.Empty.INSTANCE ? shaped.result() : result,
                        craftingStation == SlotDisplay.Empty.INSTANCE ? shaped.craftingStation() : craftingStation
                );
            }
            case ShapelessCraftingRecipeDisplay shapeless -> {
                final List<SlotDisplay> ingredients = rewrite(player, view, shapeless.ingredients(), ItemSlot.Recipe.SHAPELESS_INGREDIENT);
                if (ingredients == null) {
                    return null;
                }

                final SlotDisplay result = rewrite(this.mapper, player, view, shapeless.result(), ItemSlot.Recipe.SHAPELESS_RESULT);
                if (result == null) {
                    return null;
                }

                final SlotDisplay craftingStation = rewrite(this.mapper, player, view, shapeless.craftingStation(), ItemSlot.Recipe.CRAFTING_STATION);
                if (craftingStation == null) {
                    return null;
                }

                if (ingredients == EMPTY_LIST && result == SlotDisplay.Empty.INSTANCE && craftingStation == SlotDisplay.Empty.INSTANCE) {
                    return recipe;
                }

                return new ShapelessCraftingRecipeDisplay(
                        ingredients == EMPTY_LIST ? shapeless.ingredients() : ingredients,
                        result == SlotDisplay.Empty.INSTANCE ? shapeless.result() : result,
                        craftingStation == SlotDisplay.Empty.INSTANCE ? shapeless.craftingStation() : craftingStation
                );
            }
            case SmithingRecipeDisplay smithing -> {
                final SlotDisplay template = rewrite(this.mapper, player, view, smithing.template(), ItemSlot.Recipe.TRANSFORM_TEMPLATE);
                if (template == null) {
                    return null;
                }

                final SlotDisplay base = rewrite(this.mapper, player, view, smithing.base(), ItemSlot.Recipe.TRANSFORM_BASE);
                if (base == null) {
                    return null;
                }

                final SlotDisplay addition = rewrite(this.mapper, player, view, smithing.addition(), ItemSlot.Recipe.TRANSFORM_ADDITION);
                if (addition == null) {
                    return null;
                }

                final SlotDisplay result = rewrite(this.mapper, player, view, smithing.result(), ItemSlot.Recipe.TRANSFORM_RESULT);
                if (result == null) {
                    return null;
                }

                final SlotDisplay craftingStation = rewrite(this.mapper, player, view, smithing.craftingStation(), ItemSlot.Recipe.CRAFTING_STATION);
                if (craftingStation == null) {
                    return null;
                }

                if (template == SlotDisplay.Empty.INSTANCE && base == SlotDisplay.Empty.INSTANCE && addition == SlotDisplay.Empty.INSTANCE && result == SlotDisplay.Empty.INSTANCE && craftingStation == SlotDisplay.Empty.INSTANCE) {
                    return recipe;
                }

                return new SmithingRecipeDisplay(
                        template == SlotDisplay.Empty.INSTANCE ? smithing.template() : template,
                        base == SlotDisplay.Empty.INSTANCE ? smithing.base() : base,
                        addition == SlotDisplay.Empty.INSTANCE ? smithing.addition() : addition,
                        result == SlotDisplay.Empty.INSTANCE ? smithing.result() : result,
                        craftingStation == SlotDisplay.Empty.INSTANCE ? smithing.craftingStation() : craftingStation
                );
            }
            case StonecutterRecipeDisplay stonecutter -> {
                final SlotDisplay input = rewrite(this.mapper, player, view, stonecutter.input(), ItemSlot.Recipe.STONECUTTER_INGREDIENT);
                if (input == null) {
                    return null;
                }

                final SlotDisplay result = rewrite(this.mapper, player, view, stonecutter.result(), ItemSlot.Recipe.STONECUTTER_RESULT);
                if (result == null) {
                    return null;
                }

                final SlotDisplay craftingStation = rewrite(this.mapper, player, view, stonecutter.craftingStation(), ItemSlot.Recipe.CRAFTING_STATION);
                if (craftingStation == null) {
                    return null;
                }

                if (input == SlotDisplay.Empty.INSTANCE && result == SlotDisplay.Empty.INSTANCE && craftingStation == SlotDisplay.Empty.INSTANCE) {
                    return recipe;
                }

                return new StonecutterRecipeDisplay(
                        input == SlotDisplay.Empty.INSTANCE ? stonecutter.input() : input,
                        result == SlotDisplay.Empty.INSTANCE ? stonecutter.result() : result,
                        craftingStation == SlotDisplay.Empty.INSTANCE ? stonecutter.craftingStation() : craftingStation
                );
            }
            default -> {
                return recipe;
            }
        }
    }

    @Nullable
    private List<SlotDisplay> rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull List<SlotDisplay> ingredients, @NotNull ItemSlot[] slots) {
        final List<SlotDisplay> list = new ArrayList<>(ingredients.size());
        boolean edited = false;
        int index = 0;
        for (SlotDisplay ingredient : ingredients) {
            final SlotDisplay result = rewrite(this.mapper, player, view, ingredient, slots[index]);
            if (result == null) {
                return null;
            } else if (result == SlotDisplay.Empty.INSTANCE) {
                list.add(ingredient);
            } else {
                list.add(result);
                edited = true;
            }
            index++;
        }
        return edited ? list : EMPTY_LIST;
    }
}
