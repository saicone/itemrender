package com.saicone.item;

import com.saicone.item.render.WrappedItemRender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class ItemRender<PlayerT, ItemT> extends ItemMapperBus<PlayerT, ItemT> {

    private final Map<Class<?>, ItemRender<PlayerT, ?>> subRenders = new HashMap<>();

    @NotNull
    public abstract Class<PlayerT> playerType();

    public abstract void load();

    @NotNull
    @SuppressWarnings("unchecked")
    public <ItemA> ItemRender<PlayerT, ItemA> using(@NotNull Class<ItemA> type) {
        ItemRender<PlayerT, ItemA> render = (WrappedItemRender<PlayerT, ItemA, ItemT>) subRenders.get(type);
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
    public <ItemA> ItemRender<PlayerT, ItemA> using(@NotNull ItemWrapper<ItemA, ItemT> wrapper) {
        final WrappedItemRender<PlayerT, ItemA, ItemT> wrapped = new WrappedItemRender<>() {
            @Override
            protected @NotNull ItemMapperBus<PlayerT, ?> parent() {
                return ItemRender.this;
            }

            @Override
            public @NotNull Class<PlayerT> playerType() {
                return ItemRender.this.playerType();
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
    protected <ItemA> ItemRender<PlayerT, ItemA> createSubRender(@NotNull Class<ItemA> type) {
        return null;
    }
}
