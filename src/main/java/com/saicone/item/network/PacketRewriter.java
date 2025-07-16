package com.saicone.item.network;

import com.saicone.item.ItemView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PacketRewriter<PlayerT, ItemT, PacketT> {

    protected final PacketItemMapper<PlayerT, ItemT> mapper;

    public PacketRewriter(@NotNull PacketItemMapper<PlayerT, ItemT> mapper) {
        this.mapper = mapper;
    }

    @NotNull
    public abstract ItemView view(@NotNull PlayerT player);

    @Nullable
    public abstract PacketT rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketT packet);
}
