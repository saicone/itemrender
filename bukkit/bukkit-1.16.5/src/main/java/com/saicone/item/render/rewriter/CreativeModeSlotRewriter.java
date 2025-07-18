package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.PacketPlayInSetCreativeSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;

public class CreativeModeSlotRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayInSetCreativeSlot> {

    private static final MethodHandle SET_ITEM = Lookup.setter(PacketPlayInSetCreativeSlot.class, ItemStack.class, "b");

    public CreativeModeSlotRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.WINDOW_SERVER;
    }

    @Override
    public @Nullable PacketPlayInSetCreativeSlot rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayInSetCreativeSlot packet) {
        final var result = this.mapper.apply(player, packet.getItemStack(), view, packet.b());
        if (result.item() == null) {
            return null;
        } else if (result.edited()) {
            Lookup.invoke(SET_ITEM, packet, result.item());
        }
        return packet;
    }
}
