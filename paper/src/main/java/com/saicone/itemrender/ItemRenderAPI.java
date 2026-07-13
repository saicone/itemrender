package com.saicone.itemrender;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemRenderAPI {

    private static Object ITEM_RENDER;

    @NotNull
    public static ItemRender<Player, ItemStack> bukkit() {
        return minecraft().using(ItemStack.class);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static ItemRender<Player, Object> minecraft() {
        return (ItemRender<Player, Object>) ITEM_RENDER;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static ItemRender<Player, net.minecraft.world.item.ItemStack> paper() {
        return (ItemRender<Player, net.minecraft.world.item.ItemStack>) ITEM_RENDER;
    }
}
