package com.saicone.item.render;

import com.saicone.item.mapper.AbstractItemMapper;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BukkitItemRender extends WrappedItemRender<Player, org.bukkit.inventory.ItemStack, ItemStack> {

    private final AbstractItemMapper<Player, ?> parent;

    public BukkitItemRender(@NotNull AbstractItemMapper<Player, ?> parent) {
        this.parent = parent;
    }

    @Override
    protected @NotNull AbstractItemMapper<Player, ?> parent() {
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
