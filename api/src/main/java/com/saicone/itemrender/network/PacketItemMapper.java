package com.saicone.itemrender.network;

import com.saicone.itemrender.ItemMapper;
import org.jetbrains.annotations.NotNull;

public interface PacketItemMapper<ViewerT, ItemT> extends ItemMapper<ViewerT, ItemT> {

    boolean creative(@NotNull ViewerT viewer);
}
