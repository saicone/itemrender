package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.render.registry.ItemRegistry;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PacketPlayOutWindowItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;

public class ContainerSetContentRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutWindowItems> {

    private static final MethodHandle CONTAINER_ID = Lookup.getter(PacketPlayOutWindowItems.class, int.class, "a");
    private static final MethodHandle ITEMS = Lookup.getter(PacketPlayOutWindowItems.class, ItemStack[].class, "b");

    public ContainerSetContentRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return this.mapper.creative(player) ? ItemView.WINDOW_CREATIVE : ItemView.WINDOW;
    }

    @Override
    public @Nullable PacketPlayOutWindowItems rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayOutWindowItems packet) {
        final ItemStack[] items = Lookup.invoke(ITEMS, packet);
        if (items.length == 0) {
            return packet;
        }
        final int containerId = Lookup.invoke(CONTAINER_ID, packet);
        for (int slot = 0; slot < items.length; slot++) {
            final var result = this.mapper.context(player, items[slot], view)
                    .withContainer(containerId, ItemSlot.integer(slot))
                    .apply();
            if (result.edited()) {
                items[slot] = result.itemOrDefault(ItemRegistry.empty());
            }
        }
        return packet;
    }
}
