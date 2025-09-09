package com.saicone.item;

import com.saicone.item.mapper.ItemMapperImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ItemMapper<PlayerT, ItemT> {

    @NotNull
    default String key() {
        return getClass().getName();
    }

    default int priority() {
        return 0;
    }

    @NotNull
    Set<ItemView> views();

    default boolean allow(@NotNull ItemView view) {
        return views().contains(view);
    }

    void apply(@NotNull ItemContext<PlayerT, ItemT> context);

    default void report(@NotNull Object executor, @NotNull Throwable throwable) {
        new RuntimeException("There is an error while executing " + key() + " on " + executor.getClass().getName(), throwable).printStackTrace();
    }

    @NotNull
    default ItemContext<PlayerT, ItemT> context(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view) {
        final ItemContext<PlayerT, ItemT> context = new ItemContext<>(this);
        context.rotate(player, item, view);
        return context;
    }

    @NotNull
    static <PlayerT, ItemT> Builder<PlayerT, ItemT> builder(@NotNull String key) {
        return new ItemMapperImpl.BuilderImpl<>(key);
    }

    interface Builder<PlayerT, ItemT> extends ItemMapperBuilder<PlayerT, ItemT, ItemMapper<PlayerT, ItemT>, Builder<PlayerT, ItemT>> {
    }

}
