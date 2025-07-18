package com.saicone.item;

import com.saicone.item.mapper.AbstractItemMapper;
import com.saicone.item.mapper.WrappedItemMapper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class ItemRender<PlayerT, ItemT> extends AbstractItemMapper<PlayerT, ItemT> {

    private final Map<Class<?>, WrappedItemMapper<PlayerT, ?, ItemT>> wrappedMappers = new HashMap<>();

    public abstract void load();

    @NotNull
    @SuppressWarnings("unchecked")
    public <ItemA> WrappedItemMapper<PlayerT, ItemA, ItemT> using(@NotNull Class<ItemA> type) {
        WrappedItemMapper<PlayerT, ItemA, ItemT> wrapped = (WrappedItemMapper<PlayerT, ItemA, ItemT>) wrappedMappers.get(type);
        if (wrapped == null) {
            wrapped = wrapped(type);
            wrappedMappers.put(type, wrapped);
        }
        return wrapped;
    }

    @NotNull
    public <ItemA> WrappedItemMapper<PlayerT, ItemA, ItemT> using(@NotNull ItemWrapper<ItemA, ItemT> wrapper) {
        final WrappedItemMapper<PlayerT, ItemA, ItemT> wrapped = new WrappedItemMapper<>() {
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

    protected abstract <ItemA> WrappedItemMapper<PlayerT, ItemA, ItemT> wrapped(@NotNull Class<ItemA> type);
}
