package com.saicone.item;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractItemMapperBuilder<PlayerT, ItemT, MapperT extends ItemMapper<PlayerT, ItemT>, BuilderT extends ItemMapperBuilder<PlayerT, ItemT, MapperT, BuilderT>> implements ItemMapperBuilder<PlayerT, ItemT, MapperT, BuilderT> {

    protected final String key;

    protected int priority = 0;
    protected final Set<ItemView> views = new HashSet<>();
    protected Consumer<ItemContext<PlayerT, ItemT>> apply;
    protected BiConsumer<Object, Throwable> report;

    protected AbstractItemMapperBuilder(@NotNull String key) {
        this.key = key;
    }

    protected abstract BuilderT get();

    @Override
    public @NotNull BuilderT priority(int priority) {
        this.priority = priority;
        return get();
    }

    @Override
    public @NotNull BuilderT views(@NotNull Iterable<ItemView> views) {
        for (ItemView view : views) {
            this.views.add(view);
        }
        return get();
    }

    @Override
    public @NotNull BuilderT apply(@NotNull Consumer<ItemContext<PlayerT, ItemT>> consumer) {
        this.apply = consumer;
        return get();
    }

    @Override
    public @NotNull BuilderT report(@NotNull BiConsumer<Object, Throwable> consumer) {
        this.report = consumer;
        return get();
    }
}
