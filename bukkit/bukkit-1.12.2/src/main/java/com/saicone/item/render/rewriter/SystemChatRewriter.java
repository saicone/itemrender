package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;

public class SystemChatRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutChat> implements ComponentRewriter<PlayerT, ItemStack> {

    private static final MethodHandle MESSAGE = Lookup.getter(PacketPlayOutChat.class, IChatBaseComponent.class, "a");
    private static final MethodHandle SET_MESSAGE = Lookup.setter(PacketPlayOutChat.class, IChatBaseComponent.class, "a");
    private static final MethodHandle TYPE = Lookup.getter(PacketPlayOutChat.class, ChatMessageType.class, "b");

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
        if (Lookup.<ChatMessageType>invoke(TYPE, packet) == ChatMessageType.GAME_INFO) {
            return packet;
        }

        if (packet.components != null) { // Bungeecord
            final IChatBaseComponent fromJson = IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(packet.components));
            if (fromJson == null) {
                return packet;
            }
            final IChatBaseComponent component = rewrite(this.mapper, player, view, fromJson);
            if (component != null) {
                packet.components = null;
                Lookup.invoke(SET_MESSAGE, packet, component);
            }
        } else { // Vanilla
            final IChatBaseComponent message = Lookup.invoke(MESSAGE, packet);
            if (message != null) {
                rewrite(this.mapper, player, view, message);
            } else {
                return packet;
            }
        }

        return packet;
    }
}
