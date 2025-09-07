package com.saicone.item.mapper;

import com.saicone.item.ItemContext;
import com.saicone.item.ItemMapper;
import com.saicone.item.ItemView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleItemMapper<PlayerT, ItemT> implements ItemMapper<PlayerT, ItemT> {

    private final Class<ItemT> type;
    private final Consumer<ItemContext<PlayerT, ItemT>> consumer;
    private Set<ItemView> views;
    private BiConsumer<Object, Throwable> onReport;

    public SimpleItemMapper(@NotNull Class<ItemT> type, @NotNull Consumer<ItemContext<PlayerT, ItemT>> consumer) {
        this.type = type;
        this.consumer = consumer;
    }

    @NotNull
    @Contract("_ -> this")
    public SimpleItemMapper<PlayerT, ItemT> when(@NotNull ItemView... views) {
        if (this.views == null) {
            this.views = new HashSet<>();
        }
        Collections.addAll(this.views, views);
        final AbstractItemMapper<PlayerT, ItemT> parent = parent();
        if (parent != null) {
            return parent.register(key(), this);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SimpleItemMapper<PlayerT, ItemT> before(@NotNull String existingKey) {
        final AbstractItemMapper<PlayerT, ItemT> parent = parent();
        if (parent != null) {
            return parent.registerBefore(existingKey, key(), this);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SimpleItemMapper<PlayerT, ItemT> after(@NotNull String existingKey) {
        final AbstractItemMapper<PlayerT, ItemT> parent = parent();
        if (parent != null) {
            return parent.registerAfter(existingKey, key(), this);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SimpleItemMapper<PlayerT, ItemT> onReport(@NotNull BiConsumer<Object, Throwable> onReport) {
        this.onReport = onReport;
        return this;
    }

    @Nullable
    @ApiStatus.Internal
    protected AbstractItemMapper<PlayerT, ItemT> parent() {
        return null;
    }

    @Override
    public @NotNull Class<ItemT> type() {
        return type;
    }

    @NotNull
    public Consumer<ItemContext<PlayerT, ItemT>> consumer() {
        return consumer;
    }

    @NotNull
    public Set<ItemView> views() {
        return views == null ? Set.of() : Collections.unmodifiableSet(views);
    }

    @Override
    public boolean allow(@NotNull ItemView view) {
        return views.contains(view);
    }

    @Override
    public void apply(@NotNull ItemContext<PlayerT, ItemT> context) {
        consumer.accept(context);
    }

    @Override
    public void report(@NotNull Object executor, @NotNull Throwable throwable) {
        if (this.onReport != null) {
            this.onReport.accept(executor, throwable);
            return;
        }
        ItemMapper.super.report(executor, throwable);
    }
}
