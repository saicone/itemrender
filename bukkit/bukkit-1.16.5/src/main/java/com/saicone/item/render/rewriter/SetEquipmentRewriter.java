package com.saicone.item.render.rewriter;

import com.mojang.datafixers.util.Pair;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class SetEquipmentRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutEntityEquipment> {

    private static final MethodHandle SLOTS = Lookup.getter(PacketPlayOutEntityEquipment.class, List.class, "b");

    public SetEquipmentRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.EQUIPMENT;
    }

    @Override
    public @Nullable PacketPlayOutEntityEquipment rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayOutEntityEquipment packet) {
        final List<Pair<EnumItemSlot, ItemStack>> slots = Lookup.invoke(SLOTS, packet);
        for (int i = 0; i < slots.size(); i++) {
            final Pair<EnumItemSlot, ItemStack> pair = slots.get(i);
            final var result = this.mapper.apply(player, pair.getSecond(), view, pair.getFirst().ordinal());
            if (result.item() == null) {
                slots.remove(i);
                i--;
            } else if (result.edited()) {
                slots.set(i, new Pair<>(pair.getFirst(), result.item()));
            }
        }
        return packet;
    }
}
