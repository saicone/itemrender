package com.saicone.item;

import com.saicone.item.mapper.AbstractItemMapper;
import com.saicone.item.render.WrappedItemRender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class ItemRender<PlayerT, ItemT> extends AbstractItemMapper<PlayerT, ItemT> {

    private final Map<Class<?>, WrappedItemRender<PlayerT, ?, ItemT>> wrappedMappers = new HashMap<>();

    public abstract void load();

    @NotNull
    @SuppressWarnings("unchecked")
    public <ItemA> WrappedItemRender<PlayerT, ItemA, ItemT> using(@NotNull Class<ItemA> type) {
        WrappedItemRender<PlayerT, ItemA, ItemT> wrapped = (WrappedItemRender<PlayerT, ItemA, ItemT>) wrappedMappers.get(type);
        if (wrapped == null) {
            wrapped = wrapped(type);
            wrappedMappers.put(type, wrapped);
        }
        return wrapped;
    }

    @NotNull
    public <ItemA> WrappedItemRender<PlayerT, ItemA, ItemT> using(@NotNull ItemWrapper<ItemA, ItemT> wrapper) {
        final WrappedItemRender<PlayerT, ItemA, ItemT> wrapped = new WrappedItemRender<>() {
            @Override
            protected @NotNull AbstractItemMapper<PlayerT, ?> parent() {
                return ItemRender.this;
            }

            @Override
            public @NotNull Class<ItemA> type() {
                return wrapper.type();
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
        wrappedMappers.put(wrapper.type(), wrapped);
        return wrapped;
    }

    protected abstract <ItemA> WrappedItemRender<PlayerT, ItemA, ItemT> wrapped(@NotNull Class<ItemA> type);
}
