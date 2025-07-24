package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreativeModeSlotRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ServerboundSetCreativeModeSlotPacket> {

    public CreativeModeSlotRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.WINDOW_SERVER;
    }

    @Override
    public @Nullable ServerboundSetCreativeModeSlotPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ServerboundSetCreativeModeSlotPacket packet) {
        final var result = this.mapper.apply(player, packet.getItem(), view, ItemSlot.integer(packet.getSlotNum()));
        if (result.edited()) {
            return new ServerboundSetCreativeModeSlotPacket(packet.getSlotNum(), result.itemOrDefault(ItemStack.EMPTY));
        }
        return packet;
    }
}
