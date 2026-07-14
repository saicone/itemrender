package com.saicone.itemrender.mapper;

import com.saicone.itemrender.AbstractItemMapperBuilder;
import com.saicone.itemrender.ItemContext;
import com.saicone.itemrender.ItemView;
import com.saicone.itemrender.ItemWrapper;
import com.saicone.itemrender.WrappedItemMapper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ApiStatus.Internal
public class WrappedItemMapperImpl<ViewerT, ItemA, ItemB> extends ItemMapperImpl<ViewerT, ItemA> implements WrappedItemMapper<ViewerT, ItemA, ItemB> {

    private final ItemWrapper<ItemA, ItemB> wrapper;

    public WrappedItemMapperImpl(@NotNull String key, int priority, @NotNull Set<ItemView> views, @NotNull Consumer<ItemContext<ViewerT, ItemA>> apply, @Nullable BiConsumer<Object, Throwable> report, @NotNull ItemWrapper<ItemA, ItemB> wrapper) {
        super(key, priority, views, apply, report);
        this.wrapper = wrapper;
    }

    @Override
    public void wrapAndApply(@NotNull ItemContext<ViewerT, ItemB> context) {
        final ItemA item = context.item() == null ? null : wrap(context.item());
        final var wrapped = context(context.player(), item, context.view()).with(context);
        apply(wrapped);
        if (wrapped.edited()) {
            context.update(wrapped.item() == null ? null : unwrap(wrapped.item()), wrapped.edited(), wrapped.cancel());
        }
    }

    @Override
    public @NotNull Class<ItemA> itemType() {
        return wrapper.itemType();
    }

    @Override
    public @NotNull ItemA wrap(@NotNull ItemB item) {
        return wrapper.wrap(item);
    }

    @Override
    public @NotNull ItemB unwrap(@NotNull ItemA item) {
        return wrapper.unwrap(item);
    }

    @ApiStatus.Internal
    public static class BuilderImpl<ViewerT, ItemA, ItemB> extends AbstractItemMapperBuilder<ViewerT, ItemA, WrappedItemMapper<ViewerT, ItemA, ItemB>, WrappedItemMapper.Builder<ViewerT, ItemA, ItemB>> implements WrappedItemMapper.Builder<ViewerT, ItemA, ItemB> {

        private ItemWrapper<ItemA, ItemB> wrapper;

        public BuilderImpl(@NotNull String key) {
            super(key);
        }

        @Override
        protected WrappedItemMapper.Builder<ViewerT, ItemA, ItemB> get() {
            return this;
        }

        @Override
        public WrappedItemMapper.@NotNull Builder<ViewerT, ItemA, ItemB> wrapper(@NotNull ItemWrapper<ItemA, ItemB> wrapper) {
            this.wrapper = wrapper;
            return this;
        }

        @Override
        public @NotNull WrappedItemMapper<ViewerT, ItemA, ItemB> build() {
            Objects.requireNonNull(this.apply, "Cannot create ItemMapper without 'apply' function");
            Objects.requireNonNull(this.wrapper, "Cannot create ItemMapper without wrapper");
            return new WrappedItemMapperImpl<>(this.key, this.priority, this.views, this.apply, this.report, this.wrapper);
        }
    }
}
