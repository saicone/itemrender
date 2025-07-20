package com.saicone.item.render.registry;

import com.saicone.item.network.PacketItemRender;
import com.saicone.item.render.rewriter.ContainerSetContentRewriter;
import com.saicone.item.render.rewriter.ContainerSetSlotRewriter;
import com.saicone.item.render.rewriter.CreativeModeSlotRewriter;
import com.saicone.item.render.rewriter.SetEntityDataRewriter;
import com.saicone.item.render.rewriter.SetEquipmentRewriter;
import com.saicone.item.render.rewriter.SystemChatRewriter;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutSetSlot;
import net.minecraft.server.v1_8_R3.PacketPlayOutWindowItems;
import org.jetbrains.annotations.NotNull;

public class PacketRewriterRegistry {

    public static <PlayerT> void register(@NotNull PacketItemRender<PlayerT, ItemStack, Packet<?>> itemRender) {
        itemRender.register(PacketPlayOutWindowItems.class, ContainerSetContentRewriter::new);
        itemRender.register(PacketPlayOutSetSlot.class, ContainerSetSlotRewriter::new);
        itemRender.register(PacketPlayInSetCreativeSlot.class, CreativeModeSlotRewriter::new);
        itemRender.register(PacketPlayOutEntityMetadata.class, SetEntityDataRewriter::new);
        itemRender.register(PacketPlayOutEntityEquipment.class, SetEquipmentRewriter::new);
        itemRender.register(PacketPlayOutChat.class, SystemChatRewriter::new);
    }
}
