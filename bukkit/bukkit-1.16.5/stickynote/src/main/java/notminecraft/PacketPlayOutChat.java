package notminecraft;

import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import net.minecraft.server.v1_16_R3.PacketListenerPlayOut;

public class PacketPlayOutChat implements Packet<PacketListenerPlayOut> {

    public Component adventure$message;
    public BaseComponent[] components;

    public PacketPlayOutChat(IChatBaseComponent ichatbasecomponent, ChatMessageType chatmessagetype, UUID uuid) {
        // empty constructor
    }

    @Override
    public void a(PacketDataSerializer packetDataSerializer) {
        // empty method
    }

    @Override
    public void b(PacketDataSerializer packetDataSerializer) {
        // empty method
    }

    @Override
    public void a(PacketListenerPlayOut packetListenerPlayOut) {
        // empty method
    }

    public ChatMessageType d() {
        throw new RuntimeException("empty method");
    }
}
