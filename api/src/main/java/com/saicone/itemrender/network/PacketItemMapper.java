package com.saicone.itemrender.network;

import com.saicone.itemrender.ItemMapper;
import org.jetbrains.annotations.NotNull;

public interface PacketItemMapper<PlayerT, ItemT> extends ItemMapper<PlayerT, ItemT> {

    boolean creative(@NotNull PlayerT playerT);
}
