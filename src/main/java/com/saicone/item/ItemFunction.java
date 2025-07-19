package com.saicone.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemFunction<PlayerT, ItemT> {

    @Nullable
    ItemT apply(@NotNull PlayerT player, @Nullable ItemT item, @Nullable ItemSlot slot);

    @NotNull
    default ItemFunction<PlayerT, ItemT> andThen(@NotNull ItemFunction<PlayerT, ItemT> after) {
        return (player, item, slot) -> {
            final ItemT result = apply(player, item, slot);
            return after.apply(player, result, slot);
        };
    }

    @NotNull
    default ItemFunction<PlayerT, ItemT> compose(@NotNull ItemFunction<PlayerT, ItemT> after) {
        return (player, item, slot) -> {
            final ItemT result = after.apply(player, item, slot);
            return apply(player, result, slot);
        };
    }
}
