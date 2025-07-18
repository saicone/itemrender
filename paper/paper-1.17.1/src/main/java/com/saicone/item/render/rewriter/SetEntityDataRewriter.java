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
    @SuppressWarnings("unchecked")
    public @Nullable ClientboundSetEntityDataPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundSetEntityDataPacket packet) {
        final List<SynchedEntityData.DataItem<?>> packedItems = packet.getUnpackedData();
        if (packedItems == null) {
            return packet;
        }
        for (int i = 0; i < packedItems.size(); i++) {
            final SynchedEntityData.DataItem<?> data = packedItems.get(i);
            if (data.getAccessor().getSerializer() == EntityDataSerializers.ITEM_STACK) {
                final var result = this.mapper.apply(player, (ItemStack) data.getValue(), view, null);
                if (result.item() == null) {
                    packedItems.remove(i);
                    i--;
                } else if (result.edited()) {
                    final SynchedEntityData.DataItem<?> newData = new SynchedEntityData.DataItem(data.getAccessor(), result.item());
                    newData.setDirty(data.isDirty());
                    packedItems.set(i, newData);
                }
            }
        }
        return packet;
    }
}
