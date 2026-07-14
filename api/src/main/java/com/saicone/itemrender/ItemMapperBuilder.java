package com.saicone.itemrender;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ItemMapperBuilder<ViewerT, ItemT, MapperT extends ItemMapper<ViewerT, ItemT>, BuilderT extends ItemMapperBuilder<ViewerT, ItemT, MapperT, BuilderT>> {

    @NotNull
    @Contract("_ -> this")
    BuilderT priority(int priority);

    @NotNull
    @Contract("_ -> this")
    default BuilderT views(@NotNull ItemView... views) {
        return views(List.of(views));
    }

    @NotNull
    @Contract("_ -> this")
    BuilderT views(@NotNull Iterable<ItemView> views);

    @NotNull
    @Contract("_ -> this")
    BuilderT apply(@NotNull Consumer<ItemContext<ViewerT, ItemT>> consumer);

    @NotNull
    @Contract("_ -> this")
    BuilderT report(@NotNull BiConsumer<Object, Throwable> consumer);

    @NotNull
    MapperT build();
}
