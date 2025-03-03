package com.saicone.item.mapper;

import com.saicone.item.ItemHolder;
import org.jetbrains.annotations.NotNull;

public abstract class WrappedItemMapper<PlayerT, ItemA, ItemB> extends AbstractItemMapper<PlayerT, ItemA> {

    @NotNull
    public abstract ItemA wrap(@NotNull ItemB item);

    @NotNull
    public abstract ItemB unwrap(@NotNull ItemA item);

    public void wrapAndApply(@NotNull ItemHolder<PlayerT, ItemB> holder) {
        apply(holder(holder.player(), wrap(holder.item()), holder.view(), holder.slot()));
    }
}
