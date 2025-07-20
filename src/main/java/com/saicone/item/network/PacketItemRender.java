package com.saicone.item.network;

import com.saicone.item.ItemRender;
import com.saicone.item.ItemView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class PacketItemRender<PlayerT, ItemT, PacketT> extends ItemRender<PlayerT, ItemT> implements PacketItemMapper<PlayerT, ItemT> {

    private final Map<Class<? extends PacketT>, PacketRewriter<PlayerT, ItemT, ? extends PacketT>> rewriter;

    public PacketItemRender() {
        this(new HashMap<>());
    }

    public PacketItemRender(@NotNull Map<Class<? extends PacketT>, PacketRewriter<PlayerT, ItemT, ? extends PacketT>> rewriter) {
        this.rewriter = rewriter;
    }

    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public void register(@NotNull Class<?> type, @NotNull Class<?> rewriter) {
        try {
            this.rewriter.put((Class<? extends PacketT>) type, (PacketRewriter<PlayerT, ItemT, ? extends PacketT>) rewriter.getDeclaredConstructor(PacketItemMapper.class).newInstance(this));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public <T extends PacketT> void register(@NotNull Class<T> type, @NotNull Function<PacketItemMapper<PlayerT, ItemT>, PacketRewriter<PlayerT, ItemT, T>> rewriter) {
        this.rewriter.put(type, rewriter.apply(this));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends PacketT> T rewrite(@NotNull PlayerT player, @NotNull T packet) {
        final PacketRewriter<PlayerT, ItemT, T> rewriter = (PacketRewriter<PlayerT, ItemT, T>) this.rewriter.get(packet.getClass());
        if (rewriter != null) {
            final ItemView view = rewriter.view(player);
            if (allow(view)) {
                return rewriter.rewrite(player, view, packet);
            }
        }
        return packet;
    }
}
