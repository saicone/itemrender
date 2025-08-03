package com.saicone.item.render;

import com.saicone.item.mapper.AbstractItemMapper;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;

public class BukkitItemRender extends WrappedItemRender<Player, org.bukkit.inventory.ItemStack, ItemStack> {

    private static final MethodHandle HANDLE = Lookup.getter(CraftItemStack.class, ItemStack.class, "handle");

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
            return Lookup.invoke(HANDLE, item);
        }
        return CraftItemStack.asNMSCopy(item);
    }
}
