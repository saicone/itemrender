package com.saicone.item.render.rewriter;

import com.mojang.datafixers.util.Pair;
import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetEquipmentRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundSetEquipmentPacket> {

    public SetEquipmentRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.EQUIPMENT;
    }

    @Override
    public @Nullable ClientboundSetEquipmentPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundSetEquipmentPacket packet) {
        final List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> slots = packet.getSlots();
        for (int i = 0; i < slots.size(); i++) {
            final Pair<EquipmentSlot, net.minecraft.world.item.ItemStack> pair = slots.get(i);
            final var result = this.mapper.context(player, pair.getSecond(), view)
                    .withEntity(packet.getEntity())
                    .withSlot(ItemSlot.Equipment.of(pair.getFirst()))
                    .apply();
            if (result.edited()) {
                slots.set(i, new Pair<>(pair.getFirst(), result.itemOrDefault(ItemStack.EMPTY)));
            }
        }
        return packet;
    }
}
