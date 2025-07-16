package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SystemChatRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundSystemChatPacket> implements ComponentRewriter<PlayerT> {

    public SystemChatRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.CHAT;
    }

    @Override
    public @Nullable ClientboundSystemChatPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundSystemChatPacket packet) {
        // Actionbar text
        if (packet.overlay()) {
            return packet;
        }

        final Component component = rewrite(this.mapper, player, view, packet.content());
        if (component != null) {
            return new ClientboundSystemChatPacket(component, false);
        }
        return packet;
    }
}
