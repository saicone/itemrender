package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.render.registry.ItemRegistry;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PacketPlayOutSetSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;

public class ContainerSetSlotRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutSetSlot> {

    private static final MethodHandle CONTAINER_ID = Lookup.getter(PacketPlayOutSetSlot.class, int.class, "a");
    private static final MethodHandle SLOT = Lookup.getter(PacketPlayOutSetSlot.class, int.class, "b");
    private static final MethodHandle ITEM = Lookup.getter(PacketPlayOutSetSlot.class, ItemStack.class, "c");
    private static final MethodHandle SET_ITEM = Lookup.setter(PacketPlayOutSetSlot.class, ItemStack.class, "c");

    public ContainerSetSlotRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return this.mapper.creative(player) ? ItemView.WINDOW_CREATIVE : ItemView.WINDOW;
    }

    @Override
    public @Nullable PacketPlayOutSetSlot rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayOutSetSlot packet) {
        final int slot = Lookup.invoke(SLOT, packet);
        final var result = this.mapper.apply(player, Lookup.invoke(ITEM, packet), view, Lookup.<Integer>invoke(CONTAINER_ID, packet) == -1 ? ItemSlot.Window.CURSOR : ItemSlot.integer(slot));
        if (result.edited()) {
            Lookup.invoke(SET_ITEM, packet, result.itemOrDefault(ItemRegistry.empty()));
        }
        return packet;
    }
}
