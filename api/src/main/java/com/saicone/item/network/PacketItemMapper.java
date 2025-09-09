package com.saicone.item.network;

import com.saicone.item.ItemMapper;
import org.jetbrains.annotations.NotNull;

public interface PacketItemMapper<PlayerT, ItemT> extends ItemMapper<PlayerT, ItemT> {

    boolean creative(@NotNull PlayerT playerT);
}
