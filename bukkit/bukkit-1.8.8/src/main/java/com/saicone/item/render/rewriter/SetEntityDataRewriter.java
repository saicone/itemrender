package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.render.registry.ItemRegistry;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class SetEntityDataRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutEntityMetadata> {

    private static final int ITEM_STACK = 5;

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
        final List<DataWatcher.WatchableObject> packedItems = Lookup.invoke(PACKED_ITEMS, packet);
        if (packedItems == null) {
            return packet;
        }
        final int entityId = Lookup.invoke(ID, packet);
        for (final DataWatcher.WatchableObject data : packedItems) {
            if (data.c() == ITEM_STACK) {
                final var result = this.mapper.context(player, (ItemStack) data.b(), view)
                        .withEntity(entityId)
                        .apply();
                if (result.edited()) {
                    data.a(result.itemOrDefault(ItemRegistry.empty()));
                }
            }
        }
        return packet;
    }
}
