package com.saicone.item.render.rewriter;

import com.google.common.base.Optional;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_9_R1.DataWatcher;
import net.minecraft.server.v1_9_R1.DataWatcherRegistry;
import net.minecraft.server.v1_9_R1.ItemStack;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class SetEntityDataRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutEntityMetadata> {

    // NOTE:
    // v1_9_R1  = DataWatcherRegistry.f = com.google.common.base.Optional<ItemStack>
    // v1_12_R1 = DataWatcherRegistry.f = ItemStack
    // v1_13_R1 = DataWatcherRegistry.g = ItemStack
    //
    // Can be optimized? Yes. But will require a more complex reflection usage by looking fields as ParameterizedType

    private static final MethodHandle ID = Lookup.getter(PacketPlayOutEntityMetadata.class, int.class, "a");
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
        final int entityId = Lookup.invoke(ID, packet);
        for (int i = 0; i < packedItems.size(); i++) {
            final DataWatcher.Item<?> data = packedItems.get(i);
            if (data.a().b() == DataWatcherRegistry.f) {
                final var result = this.mapper.context(player, ((Optional<ItemStack>) data.b()).orNull(), view)
                        .withEntity(entityId)
                        .apply();
                if (result.edited()) {
                    final DataWatcher.Item<?> newData = new DataWatcher.Item(data.a(), Optional.fromNullable(result.item()));
                    newData.a(data.c());
                    packedItems.set(i, newData);
                }
            }
        }
        return packet;
    }
}
