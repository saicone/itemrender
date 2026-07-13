package com.saicone.itemrender.impl.rewriter;

import com.saicone.itemrender.ItemSlot;
import com.saicone.itemrender.ItemView;
import com.saicone.itemrender.network.PacketItemMapper;
import com.saicone.itemrender.network.PacketRewriter;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContainerSetSlotRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundContainerSetSlotPacket> {

    public ContainerSetSlotRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return this.mapper.creative(player) ? ItemView.WINDOW_CREATIVE : ItemView.WINDOW;
    }

    @Override
    public @Nullable ClientboundContainerSetSlotPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundContainerSetSlotPacket packet) {
        final var result = this.mapper.context(player, packet.getItem(), view)
                .withContainer(packet.getContainerId(), packet.getContainerId() == -1 ? ItemSlot.Window.CURSOR : ItemSlot.integer(packet.getSlot()))
                .apply();
        if (result.edited()) {
            return new ClientboundContainerSetSlotPacket(packet.getContainerId(), packet.getStateId(), packet.getSlot(), result.itemOrDefault(ItemStack.EMPTY));
        }
        return packet;
    }
}
