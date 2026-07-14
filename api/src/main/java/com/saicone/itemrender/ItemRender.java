package com.saicone.itemrender;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class ItemRender<ViewerT, ItemT> extends ItemMapperBus<ViewerT, ItemT> {

    private final Map<Class<?>, ItemRender<ViewerT, ?>> subRenders = new HashMap<>();

    @NotNull
    public abstract Class<ViewerT> viewerType();

    public abstract void load();

    @NotNull
    @SuppressWarnings("unchecked")
    public <ItemA> ItemRender<ViewerT, ItemA> using(@NotNull Class<ItemA> type) {
        ItemRender<ViewerT, ItemA> render = (WrappedItemRender<ViewerT, ItemA, ItemT>) subRenders.get(type);
        if (render == null) {
            render = createSubRender(type);
            if (render == null) {
                throw new IllegalArgumentException("Cannot create sub-render for " + type.getName() + " due lack of compatibility, please provide your own ItemWrapper to #using() method");
            }
            subRenders.put(type, render);
        }
        return render;
    }

    @NotNull
    public <ItemA> ItemRender<ViewerT, ItemA> using(@NotNull ItemWrapper<ItemA, ItemT> wrapper) {
        final WrappedItemRender<ViewerT, ItemA, ItemT> wrapped = new WrappedItemRender<>() {
            @Override
            protected @NotNull ItemMapperBus<ViewerT, ?> parent() {
                return ItemRender.this;
            }

            @Override
            public @NotNull Class<ViewerT> viewerType() {
                return ItemRender.this.viewerType();
            }

            @Override
            public @NotNull Class<ItemA> itemType() {
                return wrapper.itemType();
            }

            @Override
            public @NotNull ItemA wrap(@NotNull ItemT item) {
                return wrapper.wrap(item);
            }

            @Override
            public @NotNull ItemT unwrap(@NotNull ItemA item) {
                return wrapper.unwrap(item);
            }
        };
        subRenders.put(wrapper.itemType(), wrapped);
        return wrapped;
    }

    @ApiStatus.Experimental
    @Nullable
    protected <ItemA> ItemRender<ViewerT, ItemA> createSubRender(@NotNull Class<ItemA> type) {
        return null;
    }
}
