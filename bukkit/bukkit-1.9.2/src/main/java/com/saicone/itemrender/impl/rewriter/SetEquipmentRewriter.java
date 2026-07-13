package com.saicone.itemrender.impl.rewriter;

import com.saicone.itemrender.ItemSlot;
import com.saicone.itemrender.ItemView;
import com.saicone.itemrender.network.PacketItemMapper;
import com.saicone.itemrender.network.PacketRewriter;
import com.saicone.itemrender.impl.registry.ItemRegistry;
import com.saicone.itemrender.util.Lookup;
import net.minecraft.server.v1_9_R1.EnumItemSlot;
import net.minecraft.server.v1_9_R1.ItemStack;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;

public class SetEquipmentRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutEntityEquipment> {

    private static final MethodHandle ENTITY = Lookup.getter(PacketPlayOutEntityEquipment.class, int.class, "a");
    private static final MethodHandle SLOT = Lookup.getter(PacketPlayOutEntityEquipment.class, EnumItemSlot.class, "b");
    private static final MethodHandle ITEM = Lookup.getter(PacketPlayOutEntityEquipment.class, ItemStack.class, "c");
    private static final MethodHandle SET_ITEM = Lookup.setter(PacketPlayOutEntityEquipment.class, ItemStack.class, "c");

    public SetEquipmentRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.EQUIPMENT;
    }

    @Override
    public @Nullable PacketPlayOutEntityEquipment rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayOutEntityEquipment packet) {
        final var result = this.mapper.context(player, Lookup.invoke(ITEM, packet), view)
                .withEntity(Lookup.<Integer>invoke(ENTITY, packet))
                .withSlot(ItemSlot.Equipment.of(Lookup.<EnumItemSlot>invoke(SLOT, packet)))
                .apply();
        if (result.edited()) {
            Lookup.invoke(SET_ITEM, packet, result.itemOrDefault(ItemRegistry.empty()));
        }
        return packet;
    }
}
