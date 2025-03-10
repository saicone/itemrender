package com.saicone.item.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface PacketRewriter<PlayerT, PacketT> {

    @Nullable
    PacketT rewrite(@NotNull PlayerT player, @NotNull PacketT packet);
}
