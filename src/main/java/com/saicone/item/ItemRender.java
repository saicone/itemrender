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

    protected abstract <ItemA> WrappedItemMapper<PlayerT, ItemA, ItemT> wrapped(@NotNull Class<ItemA> type);
}
