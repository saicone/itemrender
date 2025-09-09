package com.saicone.item.mapper;

import com.saicone.item.AbstractItemMapperBuilder;
import com.saicone.item.ItemContext;
import com.saicone.item.ItemMapper;
import com.saicone.item.ItemView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ItemMapperImpl<PlayerT, ItemT> implements ItemMapper<PlayerT, ItemT> {

    private final String key;
    private final int priority;
    private final Set<ItemView> views;
    private final Consumer<ItemContext<PlayerT, ItemT>> apply;
    private final BiConsumer<Object, Throwable> report;

    public ItemMapperImpl(@NotNull String key, int priority, @NotNull Set<ItemView> views, @NotNull Consumer<ItemContext<PlayerT, ItemT>> apply, @Nullable BiConsumer<Object, Throwable> report) {
        this.key = key;
        this.priority = priority;
        this.views = views;
        this.apply = apply;
        this.report = report;
    }

    @Override
    public @NotNull String key() {
        return key;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public @NotNull Set<ItemView> views() {
        return Collections.unmodifiableSet(views);
    }

    @Override
    public void apply(@NotNull ItemContext<PlayerT, ItemT> context) {
        apply.accept(context);
    }

    @Override
    public void report(@NotNull Object executor, @NotNull Throwable throwable) {
        if (report != null) {
            report.accept(executor, throwable);
            return;
        }
        ItemMapper.super.report(executor, throwable);
    }

    public static class BuilderImpl<PlayerT, ItemT> extends AbstractItemMapperBuilder<PlayerT, ItemT, ItemMapper<PlayerT, ItemT>, ItemMapper.Builder<PlayerT, ItemT>> implements ItemMapper.Builder<PlayerT, ItemT> {

        public BuilderImpl(@NotNull String key) {
            super(key);
        }

        @Override
        protected BuilderImpl<PlayerT, ItemT> get() {
            return this;
        }

        @Override
        public @NotNull ItemMapper<PlayerT, ItemT> build() {
            Objects.requireNonNull(this.apply, "Cannot create ItemMapper without 'apply' function");
            return new ItemMapperImpl<>(this.key, this.priority, this.views, this.apply, this.report);
        }
    }
}
