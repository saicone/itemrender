package com.saicone.item.render;

import com.saicone.item.ItemHolder;
import com.saicone.item.ItemRender;
import com.saicone.item.mapper.WrappedItemMapper;
import org.jetbrains.annotations.NotNull;

public abstract class WrappedItemRender<PlayerT, ItemA, ItemB> extends ItemRender<PlayerT, ItemA> implements WrappedItemMapper<PlayerT, ItemA, ItemB> {

    @Override
    public void load() {
        // empty method
    }

    @Override
    public void wrapAndApply(@NotNull ItemHolder<PlayerT, ItemB> holder) {
        final ItemA item = holder.item() == null ? null : wrap(holder.item());
        final var wrapped = holder(holder.player(), item, holder.view(), holder.slot());
        apply(wrapped);
        holder.update(wrapped.item() == null ? null : unwrap(wrapped.item()), wrapped.edited(), wrapped.cancel());
    }

    @Override
    protected <ItemA1> WrappedItemRender<PlayerT, ItemA1, ItemA> wrapped(@NotNull Class<ItemA1> type) {
        throw new IllegalArgumentException("Cannot create wrapper for " + type.getName());
    }
}
