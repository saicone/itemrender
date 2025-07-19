package com.saicone.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemRenderAPI {

    private static ItemRender<Player, Object> ITEM_RENDER;

    @NotNull
    public static ItemRender<Player, ItemStack> bukkit() {
        return minecraft().using(ItemStack.class);
    }

    @NotNull
    public static ItemRender<Player, Object> minecraft() {
        return ITEM_RENDER;
    }
}
