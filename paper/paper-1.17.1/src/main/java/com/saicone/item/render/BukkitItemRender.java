package com.saicone.item.render;

import com.saicone.item.ItemMapperBus;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BukkitItemRender extends WrappedItemRender<Player, org.bukkit.inventory.ItemStack, ItemStack> {

    private final ItemMapperBus<Player, ?> parent;

    public BukkitItemRender(@NotNull ItemMapperBus<Player, ?> parent) {
        this.parent = parent;
    }

    @Override
    protected @NotNull ItemMapperBus<Player, ?> parent() {
        return parent;
    }

    @Override
    public @NotNull Class<org.bukkit.inventory.ItemStack> type() {
        return org.bukkit.inventory.ItemStack.class;
    }

    @Override
    public @NotNull org.bukkit.inventory.ItemStack wrap(@NotNull ItemStack item) {
        return CraftItemStack.asCraftMirror(item);
    }

    @Override
    public @NotNull ItemStack unwrap(@NotNull org.bukkit.inventory.ItemStack item) {
        if (item instanceof CraftItemStack) {
            return ((CraftItemStack) item).handle;
        }
        return CraftItemStack.asNMSCopy(item);
    }
}
