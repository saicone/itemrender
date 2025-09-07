package com.saicone.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemMapper<PlayerT, ItemT> {

    @NotNull
    default String key() {
        return getClass().getName();
    }

    @NotNull
    Class<ItemT> type();

    boolean allow(@NotNull ItemView view);

    void apply(@NotNull ItemContext<PlayerT, ItemT> context);

    @NotNull
    default ItemContext<PlayerT, ItemT> context(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view) {
        throw new IllegalStateException("Cannot create ItemContext using a " + getClass().getName() + " instance");
    }

    default void report(@NotNull Object executor, @NotNull Throwable throwable) {
        new RuntimeException("There is an error while executing " + key() + " on " + executor.getClass().getName(), throwable).printStackTrace();
    }
}
