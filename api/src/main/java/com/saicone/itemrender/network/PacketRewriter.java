package com.saicone.itemrender.network;

import com.saicone.itemrender.ItemView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PacketRewriter<ViewerT, ItemT, PacketT> {

    protected final PacketItemMapper<ViewerT, ItemT> mapper;

    public PacketRewriter(@NotNull PacketItemMapper<ViewerT, ItemT> mapper) {
        this.mapper = mapper;
    }

    @NotNull
    public abstract ItemView view(@NotNull ViewerT player);

    @Nullable
    public abstract PacketT rewrite(@NotNull ViewerT player, @NotNull ItemView view, @NotNull PacketT packet);
}
