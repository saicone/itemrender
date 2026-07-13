package com.saicone.itemrender.impl.registry;

import com.saicone.itemrender.network.PacketItemRender;
import com.saicone.itemrender.impl.rewriter.ContainerSetContentRewriter;
import com.saicone.itemrender.impl.rewriter.ContainerSetSlotRewriter;
import com.saicone.itemrender.impl.rewriter.CreativeModeSlotRewriter;
import com.saicone.itemrender.impl.rewriter.MerchantOffersRewriter;
import com.saicone.itemrender.impl.rewriter.SetCursorItemRewriter;
import com.saicone.itemrender.impl.rewriter.SetEntityDataRewriter;
import com.saicone.itemrender.impl.rewriter.SetEquipmentRewriter;
import com.saicone.itemrender.impl.rewriter.SystemChatRewriter;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PacketRewriterRegistry {

    public static <PlayerT> void register(@NotNull PacketItemRender<PlayerT, ItemStack, Packet<?>> itemRender) {
        itemRender.register(ClientboundContainerSetContentPacket.class, ContainerSetContentRewriter::new);
        itemRender.register(ClientboundContainerSetSlotPacket.class, ContainerSetSlotRewriter::new);
        itemRender.register(ServerboundSetCreativeModeSlotPacket.class, CreativeModeSlotRewriter::new);
        itemRender.register(ClientboundMerchantOffersPacket.class, MerchantOffersRewriter::new);
        itemRender.register(ClientboundSetCursorItemPacket.class, SetCursorItemRewriter::new);
        itemRender.register(ClientboundSetEntityDataPacket.class, SetEntityDataRewriter::new);
        itemRender.register(ClientboundSetEquipmentPacket.class, SetEquipmentRewriter::new);
        itemRender.register(ClientboundSystemChatPacket.class, SystemChatRewriter::new);
    }
}
