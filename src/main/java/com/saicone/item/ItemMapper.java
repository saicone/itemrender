package com.saicone.item;

import org.jetbrains.annotations.NotNull;

public interface ItemMapper<PlayerT, ItemT> {

    @NotNull
    default String key() {
        return type().getName();
    }

    @NotNull
    Class<ItemT> type();

    void apply(@NotNull ItemHolder<PlayerT, ItemT> holder);

}
