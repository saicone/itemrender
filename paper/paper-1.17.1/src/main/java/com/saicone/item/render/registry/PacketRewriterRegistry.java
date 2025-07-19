package com.saicone.item.render.registry;

import com.saicone.item.network.PacketItemRender;
import com.saicone.item.render.rewriter.ContainerSetContentRewriter;
import com.saicone.item.render.rewriter.ContainerSetSlotRewriter;
import com.saicone.item.render.rewriter.CreativeModeSlotRewriter;
import com.saicone.item.render.rewriter.MerchantOffersRewriter;
import com.saicone.item.render.rewriter.SetEntityDataRewriter;
import com.saicone.item.render.rewriter.SetEquipmentRewriter;
import com.saicone.item.render.rewriter.SystemChatRewriter;
import com.saicone.item.render.rewriter.UpdateRecipesRewriter;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PacketRewriterRegistry {

    public static <PlayerT> void register(@NotNull PacketItemRender<PlayerT, ItemStack, Packet<?>> itemRender) {
        itemRender.register(ClientboundContainerSetContentPacket.class, ContainerSetContentRewriter::new);
        itemRender.register(ClientboundContainerSetSlotPacket.class, ContainerSetSlotRewriter::new);
        itemRender.register(ServerboundSetCreativeModeSlotPacket.class, CreativeModeSlotRewriter::new);
        itemRender.register(ClientboundMerchantOffersPacket.class, MerchantOffersRewriter::new);
        itemRender.register(ClientboundSetEntityDataPacket.class, SetEntityDataRewriter::new);
        itemRender.register(ClientboundSetEquipmentPacket.class, SetEquipmentRewriter::new);
        itemRender.register(ClientboundChatPacket.class, SystemChatRewriter::new);
        itemRender.register(ClientboundUpdateRecipesPacket.class, UpdateRecipesRewriter::new);
    }
}
