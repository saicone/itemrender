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

    void apply(@NotNull ItemHolder<PlayerT, ItemT> holder);

    @NotNull
    ItemHolder<PlayerT, ItemT> apply(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view, @Nullable Object slot);

}
