package com.saicone.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemMapper<PlayerT, ItemT> {

    @NotNull
    default String key() {
        return type().getName();
    }

    @NotNull
    Class<ItemT> type();

    boolean allow(@NotNull ItemView view);

    void apply(@NotNull ItemContext<PlayerT, ItemT> context);

    @NotNull
    ItemContext<PlayerT, ItemT> context(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view);

}
