package com.saicone.item.render.registry;

import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class ItemRegistry {

    ItemRegistry() {
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <I> I empty() {
        return (I) ItemStack.b;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <I> I create(@NotNull Object compound) {
        return (I) ItemStack.a((NBTTagCompound) compound);
    }
}
