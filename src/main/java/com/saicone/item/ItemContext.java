package com.saicone.item;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemContext<PlayerT, ItemT> {

    private final ItemMapper<PlayerT, ItemT> owner;

    private PlayerT player;
    private ItemT item;
    private ItemView view;
    private ItemSlot slot;
    private Integer containerId;
    private Integer entityId;
    private Integer recipeId;
    private Object recipeKey;

    private transient boolean cancel;
    private transient boolean edited;

    public ItemContext(@NotNull ItemMapper<PlayerT, ItemT> owner) {
        this.owner = owner;
    }

    @ApiStatus.Internal
    public void rotate(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view) {
        this.player = player;
        this.item = item;
        this.view = view;
        this.slot = null;
        this.containerId = null;
        this.entityId = null;
        this.recipeId = null;
        this.recipeKey = null;

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
        this.recipeId = null;
        this.recipeKey = null;

        this.edited = false;
        this.cancel = false; // Magic value, for now doesn't do anything
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
    public ItemContext<PlayerT, ItemT> with(@NotNull ItemContext<?, ?> context) {
        this.slot = context.slot;
        this.containerId = context.containerId;
        this.entityId = context.entityId;
        this.recipeId = context.recipeId;
        this.recipeKey = context.recipeKey;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public ItemContext<PlayerT, ItemT> withSlot(@Nullable ItemSlot slot) {
        this.slot = slot;
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public ItemContext<PlayerT, ItemT> withContainer(int containerId, @NotNull ItemSlot slot) {
        this.slot = slot;
        this.containerId = containerId;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public ItemContext<PlayerT, ItemT> withEntity(int entityId) {
        this.entityId = entityId;
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public ItemContext<PlayerT, ItemT> withRecipe(@NotNull Object recipeKey, @NotNull ItemSlot slot) {
        this.slot = slot;
        this.recipeKey = recipeKey;
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public ItemContext<PlayerT, ItemT> withRecipe(int recipeId, @NotNull ItemSlot slot) {
        this.slot = slot;
        this.recipeId = recipeId;
        return this;
    }

    @NotNull
    @Contract("-> this")
    public ItemContext<PlayerT, ItemT> apply() {
        owner.apply(this);
        return this;
    }

    public boolean isAt(@NotNull ItemSlot slot) {
        return this.slot != null && slot.matches(this.slot);
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

    public ItemSlot slot() {
        return slot;
    }

    public Integer containerId() {
        return containerId;
    }

    public Integer entityId() {
        return entityId;
    }

    public Integer recipeId() {
        return recipeId;
    }

    public String recipeKey() {
        return recipeKey.toString();
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
