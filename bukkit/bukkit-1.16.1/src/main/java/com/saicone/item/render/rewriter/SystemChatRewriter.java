package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R1.ChatMessageType;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.PacketPlayOutChat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

public class SystemChatRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutChat> implements ComponentRewriter<PlayerT, ItemStack> {

    private static final MethodHandle MESSAGE = Lookup.getter(PacketPlayOutChat.class, IChatBaseComponent.class, "a");
    private static final MethodHandle SENDER = Lookup.getter(PacketPlayOutChat.class, UUID.class, "c");

    public SystemChatRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.CHAT;
    }

    @Override
    public @Nullable PacketPlayOutChat rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayOutChat packet) {
        // Actionbar text
        if (packet.d() == ChatMessageType.GAME_INFO) {
            return packet;
        }

        final IChatBaseComponent component;
        if (packet.components != null) { // Bungeecord
            final IChatBaseComponent fromJson = IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(packet.components));
            if (fromJson == null) {
                return packet;
            }
            component = rewrite(this.mapper, player, view, fromJson);
        } else { // Vanilla
            final IChatBaseComponent message = Lookup.invoke(MESSAGE, packet);
            if (message != null) {
                component = rewrite(this.mapper, player, view, message);
            } else {
                return packet;
            }
        }

        if (component != null) {
            return new PacketPlayOutChat(component, packet.d(), Lookup.invoke(SENDER, packet));
        }

        return packet;
    }
}
