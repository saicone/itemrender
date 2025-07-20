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
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.Packet;
import net.minecraft.server.v1_14_R1.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_14_R1.PacketPlayOutOpenWindowMerchant;
import net.minecraft.server.v1_14_R1.PacketPlayOutRecipeUpdate;
import net.minecraft.server.v1_14_R1.PacketPlayOutSetSlot;
import net.minecraft.server.v1_14_R1.PacketPlayOutWindowItems;
import net.minecraft.server.v1_14_R1.PacketPlayOutChat;
import org.jetbrains.annotations.NotNull;

public class PacketRewriterRegistry {

    public static <PlayerT> void register(@NotNull PacketItemRender<PlayerT, ItemStack, Packet<?>> itemRender) {
        itemRender.register(PacketPlayOutWindowItems.class, ContainerSetContentRewriter.class);
        itemRender.register(PacketPlayOutSetSlot.class, ContainerSetSlotRewriter.class);
        itemRender.register(PacketPlayInSetCreativeSlot.class, CreativeModeSlotRewriter.class);
        itemRender.register(PacketPlayOutOpenWindowMerchant.class, MerchantOffersRewriter.class);
        itemRender.register(PacketPlayOutEntityMetadata.class, SetEntityDataRewriter.class);
        itemRender.register(PacketPlayOutEntityEquipment.class, SetEquipmentRewriter.class);
        itemRender.register(PacketPlayOutChat.class, SystemChatRewriter.class);
        itemRender.register(PacketPlayOutRecipeUpdate.class, UpdateRecipesRewriter.class);
    }
}
