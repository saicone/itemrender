package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import io.papermc.paper.adventure.PaperAdventure;
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

        final Component component;
        if (packet.adventure$content() != null) { // Adventure
            component = rewrite(this.mapper, player, view, PaperAdventure.asVanilla(packet.adventure$content()));
        } else if (packet.content() != null) { // Vanilla
            final Component fromJson = Component.Serializer.fromJson(packet.content());
            if (fromJson == null) {
                return packet;
            }
            component = rewrite(this.mapper, player, view, fromJson);
        } else {
            return packet;
        }

        if (component != null) {
            return new ClientboundSystemChatPacket(component, false);
        }

        return packet;
    }
}
