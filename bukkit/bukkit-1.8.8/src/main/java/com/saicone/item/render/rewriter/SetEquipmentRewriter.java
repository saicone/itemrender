package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.render.registry.ItemRegistry;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;

public class SetEquipmentRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutEntityEquipment> {

    private static final ItemSlot.Equipment[] EQUIPMENT = new ItemSlot.Equipment[] {
            ItemSlot.Equipment.MAINHAND,
            ItemSlot.Equipment.FEET,
            ItemSlot.Equipment.LEGS,
            ItemSlot.Equipment.CHEST,
            ItemSlot.Equipment.HEAD
    };

    private static final MethodHandle ENTITY = Lookup.getter(PacketPlayOutEntityEquipment.class, int.class, "a");
    private static final MethodHandle SLOT = Lookup.getter(PacketPlayOutEntityEquipment.class, int.class, "b");
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
                .withSlot(EQUIPMENT[Lookup.<Integer>invoke(SLOT, packet)])
                .apply();
        if (result.edited()) {
            Lookup.invoke(SET_ITEM, packet, result.itemOrDefault(ItemRegistry.empty()));
        }
        return packet;
    }
}
