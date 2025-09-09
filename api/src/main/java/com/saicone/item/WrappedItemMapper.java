package com.saicone.item;

import com.saicone.item.mapper.WrappedItemMapperImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface WrappedItemMapper<PlayerT, ItemA, ItemB> extends ItemMapper<PlayerT, ItemA>, ItemWrapper<ItemA, ItemB> {

    int DEFAULT_PRIORITY = 10000;

    @Override
    default int priority() {
        return DEFAULT_PRIORITY;
    }

    void wrapAndApply(@NotNull ItemContext<PlayerT, ItemB> context);

    @NotNull
    static <PlayerT, ItemA, ItemB> Builder<PlayerT, ItemA, ItemB> builder(@NotNull String key) {
        return new WrappedItemMapperImpl.BuilderImpl<>(key);
    }

    interface Builder<PlayerT, ItemA, ItemB> extends ItemMapperBuilder<PlayerT, ItemA, WrappedItemMapper<PlayerT, ItemA, ItemB>, Builder<PlayerT, ItemA, ItemB>> {

        @NotNull
        @Contract("_ -> this")
        Builder<PlayerT, ItemA, ItemB> wrapper(@NotNull ItemWrapper<ItemA, ItemB> wrapper);

    }

}
