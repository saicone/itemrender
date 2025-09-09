package com.saicone.item.mapper;

import com.saicone.item.AbstractItemMapperBuilder;
import com.saicone.item.ItemContext;
import com.saicone.item.ItemView;
import com.saicone.item.ItemWrapper;
import com.saicone.item.WrappedItemMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WrappedItemMapperImpl<PlayerT, ItemA, ItemB> extends ItemMapperImpl<PlayerT, ItemA> implements WrappedItemMapper<PlayerT, ItemA, ItemB> {

    private final ItemWrapper<ItemA, ItemB> wrapper;

    public WrappedItemMapperImpl(@NotNull String key, int priority, @NotNull Set<ItemView> views, @NotNull Consumer<ItemContext<PlayerT, ItemA>> apply, @Nullable BiConsumer<Object, Throwable> report, @NotNull ItemWrapper<ItemA, ItemB> wrapper) {
        super(key, priority, views, apply, report);
        this.wrapper = wrapper;
    }

    @Override
    public void wrapAndApply(@NotNull ItemContext<PlayerT, ItemB> context) {
        final ItemA item = context.item() == null ? null : wrap(context.item());
        final var wrapped = context(context.player(), item, context.view()).with(context);
        apply(wrapped);
        if (wrapped.edited()) {
            context.update(wrapped.item() == null ? null : unwrap(wrapped.item()), wrapped.edited(), wrapped.cancel());
        }
    }

    @Override
    public @NotNull Class<ItemA> type() {
        return wrapper.type();
    }

    @Override
    public @NotNull ItemA wrap(@NotNull ItemB item) {
        return wrapper.wrap(item);
    }

    @Override
    public @NotNull ItemB unwrap(@NotNull ItemA item) {
        return wrapper.unwrap(item);
    }

    public static class BuilderImpl<PlayerT, ItemA, ItemB> extends AbstractItemMapperBuilder<PlayerT, ItemA, WrappedItemMapper<PlayerT, ItemA, ItemB>, WrappedItemMapper.Builder<PlayerT, ItemA, ItemB>> implements WrappedItemMapper.Builder<PlayerT, ItemA, ItemB> {

        private ItemWrapper<ItemA, ItemB> wrapper;

        public BuilderImpl(@NotNull String key) {
            super(key);
        }

        @Override
        protected WrappedItemMapper.Builder<PlayerT, ItemA, ItemB> get() {
            return this;
        }

        @Override
        public WrappedItemMapper.@NotNull Builder<PlayerT, ItemA, ItemB> wrapper(@NotNull ItemWrapper<ItemA, ItemB> wrapper) {
            this.wrapper = wrapper;
            return this;
        }

        @Override
        public @NotNull WrappedItemMapper<PlayerT, ItemA, ItemB> build() {
            Objects.requireNonNull(this.apply, "Cannot create ItemMapper without 'apply' function");
            Objects.requireNonNull(this.wrapper, "Cannot create ItemMapper without wrapper");
            return new WrappedItemMapperImpl<>(this.key, this.priority, this.views, this.apply, this.report, this.wrapper);
        }
    }
}
