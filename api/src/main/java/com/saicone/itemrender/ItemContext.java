package com.saicone.itemrender;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemContext<ViewerT, ItemT> {

    private final ItemMapper<ViewerT, ItemT> owner;

    private ViewerT player;
    private ItemT item;
    private ItemView view;
    private ItemSlot slot;
    private Integer containerId;
    private Integer entityId;

    private transient boolean cancel; // Magic value, for now doesn't do anything
    private transient boolean edited;

    public ItemContext(@NotNull ItemMapper<ViewerT, ItemT> owner) {
        this.owner = owner;
    }

    @ApiStatus.Internal
    public void rotate(@NotNull ViewerT player, @Nullable ItemT item, @NotNull ItemView view) {
        this.player = player;
        this.item = item;
        this.view = view;
        this.slot = null;
        this.containerId = null;
        this.entityId = null;

        this.edited = false;
        this.cancel = false;
    }

    @ApiStatus.Internal
    public void clear() {
        this.player = null;
        this.item = null;
        this.view = null;
        this.slot = null;
        this.containerId = null;
        this.entityId = null;

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
    @Contract("_ -> this")
    public ItemContext<ViewerT, ItemT> with(@NotNull ItemContext<?, ?> context) {
        this.slot = context.slot;
        this.containerId = context.containerId;
        this.entityId = context.entityId;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public ItemContext<ViewerT, ItemT> withSlot(@Nullable ItemSlot slot) {
        this.slot = slot;
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public ItemContext<ViewerT, ItemT> withContainer(int containerId, @NotNull ItemSlot slot) {
        this.slot = slot;
        this.containerId = containerId;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public ItemContext<ViewerT, ItemT> withEntity(int entityId) {
        this.entityId = entityId;
        return this;
    }

    @NotNull
    @Contract("-> this")
    public ItemContext<ViewerT, ItemT> apply() {
        owner.apply(this);
        return this;
    }

    public boolean isAt(@NotNull ItemSlot slot) {
        return this.slot != null && slot.matches(this.slot);
    }

    @NotNull
    public ViewerT player() {
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

    public ItemSlot slot() {
        return slot;
    }

    public Integer containerId() {
        return containerId;
    }

    public Integer entityId() {
        return entityId;
    }

    public boolean cancel() {
        return cancel;
    }

    public boolean empty() {
        return this.edited && this.item == null;
    }

    @ApiStatus.Internal
    public boolean edited() {
        return edited;
    }

    public void setItem(@Nullable ItemT item) {
        if (!this.edited) {
            this.edited = item != null || this.item != null;
        }
        this.item = item;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
