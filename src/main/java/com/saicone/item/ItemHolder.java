package com.saicone.item;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemHolder<PlayerT, ItemT> {

    private PlayerT player;
    private ItemT item;
    private ItemView view;
    private ItemSlot slot;

    private transient boolean cancel;
    private transient boolean edited;

    @ApiStatus.Internal
    public void reset(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view, @Nullable ItemSlot slot) {
        this.player = player;
        this.item = item;
        this.view = view;
        this.slot = slot;
        this.edited = false;
        this.cancel = false;
    }

    @ApiStatus.Internal
    public void reset() {
        this.player = null;
        this.item = null;
        this.view = null;
        this.slot = null;
        this.edited = false;
        this.cancel = false;
    }

    @ApiStatus.Internal
    public void update(@Nullable ItemT item, boolean edited, boolean cancel) {
        this.item = item;
        if (!this.edited) {
            this.edited = edited;
        }
        if (!this.cancel) {
            this.cancel = cancel;
        }
    }

    @NotNull
    public PlayerT player() {
        return player;
    }

    public ItemT item() {
        return item;
    }

    @Nullable
    @Contract("!null -> !null")
    public ItemT itemOrDefault(@Nullable ItemT def) {
        return item != null ? item : def;
    }

    @NotNull
    public ItemView view() {
        return view;
    }

    @NotNull
    public ItemSlot slot() {
        return slot;
    }

    public boolean cancel() {
        return cancel;
    }

    public boolean empty() {
        return this.edited && this.item == null;
    }

    public boolean edited() {
        return edited;
    }

    public void item(@Nullable ItemT item) {
        if (!this.edited) {
            this.edited = item != null || this.item != null;
        }
        this.item = item;
    }

    public void cancel(boolean cancel) {
        this.cancel = cancel;
    }
}
