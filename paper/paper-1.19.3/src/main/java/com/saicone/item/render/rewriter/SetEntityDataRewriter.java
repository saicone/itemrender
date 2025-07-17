package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetEntityDataRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundSetEntityDataPacket> {

    public SetEntityDataRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.METADATA;
    }

    @Override
    public @Nullable ClientboundSetEntityDataPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundSetEntityDataPacket packet) {
        final List<SynchedEntityData.DataValue<?>> packedItems = packet.packedItems();
        for (int i = 0; i < packedItems.size(); i++) {
            final SynchedEntityData.DataValue<?> dataValue = packedItems.get(i);
            if (dataValue.serializer() == EntityDataSerializers.ITEM_STACK) {
                final var result = this.mapper.apply(player, (ItemStack) dataValue.value(), view, null);
                if (result.item() == null) {
                    packedItems.remove(i);
                    i--;
                } else if (result.edited()) {
                    packedItems.set(i, new SynchedEntityData.DataValue<>(dataValue.id(), EntityDataSerializers.ITEM_STACK, result.item()));
                }
            }
        }
        return packet;
    }
}
