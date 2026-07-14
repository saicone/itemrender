package com.saicone.itemrender.network;

import com.saicone.itemrender.ItemRender;
import com.saicone.itemrender.ItemView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class PacketItemRender<ViewerT, ItemT, PacketT> extends ItemRender<ViewerT, ItemT> implements PacketItemMapper<ViewerT, ItemT> {

    private final Map<Class<? extends PacketT>, PacketRewriter<ViewerT, ItemT, ? extends PacketT>> rewriter;

    public PacketItemRender() {
        this(new HashMap<>());
    }

    public PacketItemRender(@NotNull Map<Class<? extends PacketT>, PacketRewriter<ViewerT, ItemT, ? extends PacketT>> rewriter) {
        this.rewriter = rewriter;
    }

    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public void register(@NotNull Class<?> type, @NotNull Class<?> rewriter) {
        try {
            this.rewriter.put((Class<? extends PacketT>) type, (PacketRewriter<ViewerT, ItemT, ? extends PacketT>) rewriter.getDeclaredConstructor(PacketItemMapper.class).newInstance(this));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public <T extends PacketT> void register(@NotNull Class<T> type, @NotNull Function<PacketItemMapper<ViewerT, ItemT>, PacketRewriter<ViewerT, ItemT, T>> rewriter) {
        this.rewriter.put(type, rewriter.apply(this));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends PacketT> T rewrite(@NotNull ViewerT player, @NotNull T packet) {
        final PacketRewriter<ViewerT, ItemT, T> rewriter = (PacketRewriter<ViewerT, ItemT, T>) this.rewriter.get(packet.getClass());
        if (rewriter != null) {
            final ItemView view = rewriter.view(player);
            if (allow(view)) {
                return rewriter.rewrite(player, view, packet);
            }
        }
        return packet;
    }
}
