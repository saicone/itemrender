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
                final SlotDisplay fuel = rewrite(this.mapper, player, view, furnace.fuel(), ItemSlot.Recipe.COOKING_FUEL);
                final SlotDisplay result = rewrite(this.mapper, player, view, furnace.result(), ItemSlot.Recipe.COOKING_RESULT);
                final SlotDisplay craftingStation = rewrite(this.mapper, player, view, furnace.craftingStation(), ItemSlot.Recipe.CRAFTING_STATION);

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
                final List<SlotDisplay> ingredients = rewrite(player, view, shaped.ingredients(), ItemSlot.Recipe.SHAPED_INGREDIENT);
                final SlotDisplay result = rewrite(this.mapper, player, view, shaped.result(), ItemSlot.Recipe.SHAPED_RESULT);
                final SlotDisplay craftingStation = rewrite(this.mapper, player, view, shaped.craftingStation(), ItemSlot.Recipe.CRAFTING_STATION);

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
                final List<SlotDisplay> ingredients = rewrite(player, view, shapeless.ingredients(), ItemSlot.Recipe.SHAPELESS_INGREDIENT);
                final SlotDisplay result = rewrite(this.mapper, player, view, shapeless.result(), ItemSlot.Recipe.SHAPELESS_RESULT);
                final SlotDisplay craftingStation = rewrite(this.mapper, player, view, shapeless.craftingStation(), ItemSlot.Recipe.CRAFTING_STATION);

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
                final SlotDisplay template = rewrite(this.mapper, player, view, smithing.template(), ItemSlot.Recipe.TRANSFORM_TEMPLATE);
                final SlotDisplay base = rewrite(this.mapper, player, view, smithing.base(), ItemSlot.Recipe.TRANSFORM_BASE);
                final SlotDisplay addition = rewrite(this.mapper, player, view, smithing.addition(), ItemSlot.Recipe.TRANSFORM_ADDITION);
                final SlotDisplay result = rewrite(this.mapper, player, view, smithing.result(), ItemSlot.Recipe.TRANSFORM_RESULT);
                final SlotDisplay craftingStation = rewrite(this.mapper, player, view, smithing.craftingStation(), ItemSlot.Recipe.CRAFTING_STATION);

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
                final SlotDisplay input = rewrite(this.mapper, player, view, stonecutter.input(), ItemSlot.Recipe.STONECUTTER_INGREDIENT);
                final SlotDisplay result = rewrite(this.mapper, player, view, stonecutter.result(), ItemSlot.Recipe.STONECUTTER_RESULT);
                final SlotDisplay craftingStation = rewrite(this.mapper, player, view, stonecutter.craftingStation(), ItemSlot.Recipe.CRAFTING_STATION);

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
    private List<SlotDisplay> rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull List<SlotDisplay> ingredients, @NotNull ItemSlot[] slots) {
        final List<SlotDisplay> list = new ArrayList<>(ingredients.size());
        boolean edited = false;
        int index = 0;
        for (SlotDisplay ingredient : ingredients) {
            final SlotDisplay result = rewrite(this.mapper, player, view, ingredient, slots[index]);
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
