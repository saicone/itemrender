package com.saicone.itemrender.mapper;

import com.saicone.itemrender.AbstractItemMapperBuilder;
import com.saicone.itemrender.ItemContext;
import com.saicone.itemrender.ItemMapper;
import com.saicone.itemrender.ItemView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ApiStatus.Internal
public class ItemMapperImpl<ViewerT, ItemT> implements ItemMapper<ViewerT, ItemT> {

    private final String key;
    private final int priority;
    private final Set<ItemView> views;
    private final Consumer<ItemContext<ViewerT, ItemT>> apply;
    private final BiConsumer<Object, Throwable> report;

    public ItemMapperImpl(@NotNull String key, int priority, @NotNull Set<ItemView> views, @NotNull Consumer<ItemContext<ViewerT, ItemT>> apply, @Nullable BiConsumer<Object, Throwable> report) {
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
    public void apply(@NotNull ItemContext<ViewerT, ItemT> context) {
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

    @ApiStatus.Internal
    public static class BuilderImpl<ViewerT, ItemT> extends AbstractItemMapperBuilder<ViewerT, ItemT, ItemMapper<ViewerT, ItemT>, ItemMapper.Builder<ViewerT, ItemT>> implements ItemMapper.Builder<ViewerT, ItemT> {

        public BuilderImpl(@NotNull String key) {
            super(key);
        }

        @Override
        protected BuilderImpl<ViewerT, ItemT> get() {
            return this;
        }

        @Override
        public @NotNull ItemMapper<ViewerT, ItemT> build() {
            Objects.requireNonNull(this.apply, "Cannot create ItemMapper without 'apply' function");
            return new ItemMapperImpl<>(this.key, this.priority, this.views, this.apply, this.report);
        }
    }
}
