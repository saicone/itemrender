package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ContainerSetContentRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundContainerSetContentPacket> {

    public ContainerSetContentRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return this.mapper.creative(player) ? ItemView.WINDOW_CREATIVE : ItemView.WINDOW;
    }

    @Override
    public @Nullable ClientboundContainerSetContentPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundContainerSetContentPacket packet) {
        final List<ItemStack> items = packet.getItems();
        if (!items.isEmpty()) {
            for (int slot = 0; slot < items.size(); slot++) {
                final var result = this.mapper.context(player, items.get(slot), view)
                        .withContainer(packet.getContainerId(), ItemSlot.integer(slot))
                        .apply();
                if (result.edited()) {
                    items.set(slot, result.itemOrDefault(ItemStack.EMPTY));
                }
            }
        }

        final var result = this.mapper.context(player, packet.getCarriedItem(), view)
                .withContainer(packet.getContainerId(), ItemSlot.Window.CURSOR)
                .apply();
        if (result.edited()) {
            return new ClientboundContainerSetContentPacket(packet.getContainerId(), packet.getStateId(), NonNullList.of(ItemStack.EMPTY, items.toArray(new ItemStack[0])), result.itemOrDefault(ItemStack.EMPTY));
        }

        return packet;
    }
}
