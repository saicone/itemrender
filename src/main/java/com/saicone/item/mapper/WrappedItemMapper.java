package com.saicone.item.mapper;

import com.saicone.item.ItemHolder;
import com.saicone.item.ItemWrapper;
import org.jetbrains.annotations.NotNull;

public abstract class WrappedItemMapper<PlayerT, ItemA, ItemB> extends AbstractItemMapper<PlayerT, ItemA> implements ItemWrapper<ItemA, ItemB> {

    public void wrapAndApply(@NotNull ItemHolder<PlayerT, ItemB> holder) {
        final ItemA item = holder.item() == null ? null : wrap(holder.item());
        final var wrapped = holder(holder.player(), item, holder.view(), holder.slot());
        apply(wrapped);
        holder.update(unwrap(wrapped.item()));
    }
}
