package com.saicone.item.render.registry;

import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class ItemRegistry {

    private static final ItemStack EMPTY = new ItemStack((Item) null);

    ItemRegistry() {
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <I> I empty() {
        return (I) EMPTY;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <I> I create(@NotNull Object compound) {
        return (I) ItemStack.createStack((NBTTagCompound) compound);
    }
}
