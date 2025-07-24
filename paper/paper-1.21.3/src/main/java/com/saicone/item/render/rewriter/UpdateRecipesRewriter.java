package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UpdateRecipesRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundUpdateRecipesPacket> implements SlotDisplayRewriter<PlayerT> {

    private static final MethodHandle PROPERTY = Lookup.constructor(RecipePropertySet.class, Set.class);
    private static final MethodHandle PROPERTY_ITEMS = Lookup.getter(RecipePropertySet.class, Set.class, "items", "k");

    public UpdateRecipesRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.RECIPE;
    }

    @Override
    public @Nullable ClientboundUpdateRecipesPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundUpdateRecipesPacket packet) {
        final Map<ResourceKey<RecipePropertySet>, RecipePropertySet> itemSets = rewrite(player, view, packet.itemSets());
        final SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes = rewrite(player, view, packet.stonecutterRecipes());
        if (itemSets == null) {
            if (stonecutterRecipes != null) {
                return new ClientboundUpdateRecipesPacket(packet.itemSets(), stonecutterRecipes);
            }
        } else if (stonecutterRecipes == null) {
            return new ClientboundUpdateRecipesPacket(itemSets, packet.stonecutterRecipes());
        } else {
            return new ClientboundUpdateRecipesPacket(itemSets, stonecutterRecipes);
        }

        return packet;
    }

    @Nullable
    private Map<ResourceKey<RecipePropertySet>, RecipePropertySet> rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull Map<ResourceKey<RecipePropertySet>, RecipePropertySet> map) {
        if (map.isEmpty()) {
            return null;
        }

        boolean edited = false;
        final Map<ResourceKey<RecipePropertySet>, RecipePropertySet> itemSets = new HashMap<>();
        for (Map.Entry<ResourceKey<RecipePropertySet>, RecipePropertySet> entry : map.entrySet()) {
            itemSets.put(entry.getKey(), entry.getValue());

            if (entry.getValue() == RecipePropertySet.EMPTY) {
                continue;
            }

            final ItemSlot slot = propertyToSlot(entry.getKey());
            final Set<Holder<Item>> propertyItems = Lookup.invoke(PROPERTY_ITEMS, entry.getValue());
            if (propertyItems.isEmpty()) {
                continue;
            }

            boolean itemsEdited = false;
            final Set<Holder<Item>> items = new HashSet<>();
            for (Holder<Item> item : propertyItems) {
                final var result = this.mapper.apply(player, new ItemStack(item), view, slot);
                if (result.empty()) {
                    items.add(ItemStack.EMPTY.getItemHolder());
                    itemsEdited = true;
                } else if (result.edited()) {
                    items.add(result.item().getItemHolder());
                    itemsEdited = true;
                } else {
                    items.add(item);
                }
            }

            if (itemsEdited) {
                itemSets.put(entry.getKey(), Lookup.invoke(PROPERTY, items));
                edited = true;
            }
        }

        return edited ? itemSets : null;
    }

    @Nullable
    private SelectableRecipe.SingleInputSet<StonecutterRecipe> rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull SelectableRecipe.SingleInputSet<StonecutterRecipe> recipes) {
        if (recipes.isEmpty()) {
            return null;
        }

        boolean edited = false;
        final List<SelectableRecipe.SingleInputEntry<StonecutterRecipe>> stonecutterRecipes = new ArrayList<>();
        for (SelectableRecipe.SingleInputEntry<StonecutterRecipe> entry : recipes.entries()) {
            final SelectableRecipe<StonecutterRecipe> selectable = entry.recipe();
            final SlotDisplay display = rewrite(this.mapper, player, view, selectable.optionDisplay(), ItemSlot.Recipe.STONECUTTER_INGREDIENT);
            if (display == null) {
                stonecutterRecipes.add(entry);
            } else {
                stonecutterRecipes.add(new SelectableRecipe.SingleInputEntry<>(entry.input(), new SelectableRecipe<>(display, selectable.recipe())));
                edited = true;
            }
        }

        if (edited) {
            return new SelectableRecipe.SingleInputSet<>(stonecutterRecipes);
        }

        stonecutterRecipes.clear();
        return null;
    }
    
    @Nullable
    private static ItemSlot propertyToSlot(@NotNull ResourceKey<RecipePropertySet> key) {
        if (key == RecipePropertySet.SMITHING_BASE) {
            return ItemSlot.Recipe.TRANSFORM_BASE;
        } else if (key == RecipePropertySet.SMITHING_TEMPLATE) {
            return ItemSlot.Recipe.TRANSFORM_TEMPLATE;
        } else if (key == RecipePropertySet.SMITHING_ADDITION) {
            return ItemSlot.Recipe.TRANSFORM_ADDITION;
        } else if (key == RecipePropertySet.FURNACE_INPUT || key == RecipePropertySet.BLAST_FURNACE_INPUT || key == RecipePropertySet.SMOKER_INPUT || key == RecipePropertySet.CAMPFIRE_INPUT) {
            return ItemSlot.Recipe.COOKING_INGREDIENT;
        } else {
            return null;
        }
    }
}
