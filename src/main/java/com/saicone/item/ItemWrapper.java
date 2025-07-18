package com.saicone.item;

import org.jetbrains.annotations.NotNull;

public interface ItemWrapper<ItemA, ItemB> {

    @NotNull
    Class<ItemA> type();

    @NotNull
    ItemA wrap(@NotNull ItemB item);

    @NotNull
    ItemB unwrap(@NotNull ItemA item);
}
