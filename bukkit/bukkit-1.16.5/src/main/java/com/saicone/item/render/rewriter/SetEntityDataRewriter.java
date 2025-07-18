package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.DataWatcherRegistry;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class SetEntityDataRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutEntityMetadata> {

    private static final MethodHandle PACKED_ITEMS = Lookup.getter(PacketPlayOutEntityMetadata.class, List.class, "b");

    public SetEntityDataRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.METADATA;
    }

    @Override
    public @Nullable PacketPlayOutEntityMetadata rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayOutEntityMetadata packet) {
        final List<DataWatcher.Item<?>> packedItems = Lookup.invoke(PACKED_ITEMS, packet);
        if (packedItems == null) {
            return packet;
        }
        for (int i = 0; i < packedItems.size(); i++) {
            final DataWatcher.Item<?> data = packedItems.get(i);
            if (data.a().b() == DataWatcherRegistry.g) {
                final var result = this.mapper.apply(player, (ItemStack) data.b(), view, null);
                if (result.item() == null) {
                    packedItems.remove(i);
                    i--;
                } else if (result.edited()) {
                    final DataWatcher.Item<?> newData = new DataWatcher.Item(data.a(), result.item());
                    newData.a(data.c());
                    packedItems.set(i, newData);
                }
            }
        }
        return packet;
    }
}
