package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import io.papermc.paper.adventure.PaperAdventure;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SystemChatRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundChatPacket> implements ComponentRewriter<PlayerT> {

    public SystemChatRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.CHAT;
    }

    @Override
    public @Nullable ClientboundChatPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundChatPacket packet) {
        // Actionbar text
        if (packet.getType() == ChatType.GAME_INFO) {
            return packet;
        }

        Component component = null;
        try {
            if (packet.adventure$message != null) { // Adventure
                component = rewrite(this.mapper, player, view, PaperAdventure.asVanilla(packet.adventure$message));
            }
        } catch (Throwable ignored) { }
        if (component == null) {
            if (packet.components != null) { // Bungeecord
                final Component fromJson = Component.Serializer.fromJson(ComponentSerializer.toString(packet.components));
                if (fromJson == null) {
                    return packet;
                }
                component = rewrite(this.mapper, player, view, fromJson);
            } else if (packet.getMessage() != null) { // Vanilla
                component = rewrite(this.mapper, player, view, packet.getMessage());
            } else {
                return packet;
            }
        }

        if (component != null) {
            return new ClientboundChatPacket(component, packet.getType(), packet.getSender());
        }

        return packet;
    }
}
