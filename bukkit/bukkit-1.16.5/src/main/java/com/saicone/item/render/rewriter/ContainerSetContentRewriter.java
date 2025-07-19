package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.PacketPlayOutWindowItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class ContainerSetContentRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutWindowItems> {

    private static final MethodHandle ITEMS = Lookup.getter(PacketPlayOutWindowItems.class, List.class, "b");

    public ContainerSetContentRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return this.mapper.creative(player) ? ItemView.WINDOW_CREATIVE : ItemView.WINDOW;
    }

    @Override
    public @Nullable PacketPlayOutWindowItems rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayOutWindowItems packet) {
        final List<ItemStack> items = Lookup.invoke(ITEMS, packet);
        int empty = 0;
        for (int slot = 0; slot < items.size(); slot++) {
            final var result = this.mapper.apply(player, items.get(slot), view, ItemSlot.integer(slot));
            if (result.item() == null) {
                items.set(slot, ItemStack.b);
                empty++;
            } else if (result.edited()) {
                items.set(slot, result.item());
            }
        }
        if (empty == items.size()) {
            return null;
        }
        return packet;
    }
}
