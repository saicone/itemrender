package com.saicone.item;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemHolder<PlayerT, ItemT> {

    private PlayerT player;
    private ItemT item;
    private ItemView view;
    private Object slot;

    private transient boolean edited;

    @ApiStatus.Internal
    public void reset(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view, @Nullable Object slot) {
        this.player = player;
        this.item = item;
        this.view = view;
        this.slot = slot;
        this.edited = false;
    }

    @ApiStatus.Internal
    public void reset() {
        this.player = null;
        this.item = null;
        this.view = null;
        this.slot = null;
        this.edited = false;
    }

    @NotNull
    public PlayerT player() {
        return player;
    }

    public ItemT item() {
        return item;
    }

    @NotNull
    public ItemView view() {
        return view;
    }

    @NotNull
    public Object slot() {
        return slot;
    }

    public boolean edited() {
        return edited;
    }

    public void item(@Nullable ItemT item) {
        this.item = item;
        this.edited = true;
    }
}
