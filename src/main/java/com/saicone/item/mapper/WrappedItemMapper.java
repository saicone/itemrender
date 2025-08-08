package com.saicone.item.mapper;

import com.saicone.item.ItemContext;
import com.saicone.item.ItemMapper;
import com.saicone.item.ItemWrapper;
import org.jetbrains.annotations.NotNull;

public interface WrappedItemMapper<PlayerT, ItemA, ItemB> extends ItemMapper<PlayerT, ItemA>, ItemWrapper<ItemA, ItemB> {

    void wrapAndApply(@NotNull ItemContext<PlayerT, ItemB> holder);
}
