package com.saicone.itemrender;

import com.saicone.itemrender.mapper.ItemMapperImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ItemMapper<ViewerT, ItemT> {

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

    void apply(@NotNull ItemContext<ViewerT, ItemT> context);

    default void report(@NotNull Object executor, @NotNull Throwable throwable) {
        new RuntimeException("There is an error while executing " + key() + " on " + executor.getClass().getName(), throwable).printStackTrace();
    }

    @NotNull
    default ItemContext<ViewerT, ItemT> context(@NotNull ViewerT player, @Nullable ItemT item, @NotNull ItemView view) {
        final ItemContext<ViewerT, ItemT> context = new ItemContext<>(this);
        context.rotate(player, item, view);
        return context;
    }

    @NotNull
    static <ViewerT, ItemT> Builder<ViewerT, ItemT> builder(@NotNull String key) {
        return new ItemMapperImpl.BuilderImpl<>(key);
    }

    @NotNull
    static <ViewerT, ItemT> Builder<ViewerT, ItemT> builder(@NotNull String key, @NotNull Class<ViewerT> playerClass, @NotNull Class<ItemT> itemClass) {
        return new ItemMapperImpl.BuilderImpl<>(key);
    }

    interface Builder<ViewerT, ItemT> extends ItemMapperBuilder<ViewerT, ItemT, ItemMapper<ViewerT, ItemT>, Builder<ViewerT, ItemT>> {
    }

}
