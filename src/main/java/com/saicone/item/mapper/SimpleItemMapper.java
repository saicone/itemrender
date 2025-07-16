package com.saicone.item.mapper;

import com.saicone.item.ItemHolder;
import com.saicone.item.ItemMapper;
import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class SimpleItemMapper<PlayerT, ItemT> implements ItemMapper<PlayerT, ItemT> {

    private final Class<ItemT> type;
    private final BiFunction<PlayerT, ItemT, ItemT> function;
    private BiPredicate<PlayerT, ItemT> predicate;
    private Set<ItemView> views;
    private Set<ItemSlot> slots;

    public SimpleItemMapper(@NotNull Class<ItemT> type, @NotNull Function<ItemT, ItemT> function) {
        this(type, (player, item) -> function.apply(item));
    }

    public SimpleItemMapper(@NotNull Class<ItemT> type, @NotNull BiFunction<PlayerT, ItemT, ItemT> function) {
        this.type = type;
        this.function = function;
    }

    @NotNull
    @Contract("_ -> this")
    public SimpleItemMapper<PlayerT, ItemT> check(@NotNull Predicate<ItemT> predicate) {
        return check((player, item) -> predicate.test(item));
    }

    @NotNull
    @Contract("_ -> this")
    public SimpleItemMapper<PlayerT, ItemT> check(@NotNull BiPredicate<PlayerT, ItemT> predicate) {
        this.predicate = predicate;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SimpleItemMapper<PlayerT, ItemT> when(@NotNull ItemView... views) {
        if (this.views == null) {
            this.views = new HashSet<>();
        }
        Collections.addAll(this.views, views);
        final AbstractItemMapper<PlayerT, ItemT> parent = parent();
        if (parent != null) {
            return parent.register(key(), this);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SimpleItemMapper<PlayerT, ItemT> at(@NotNull ItemSlot... slots) {
        if (this.slots == null) {
            this.slots = new HashSet<>();
        }
        Collections.addAll(this.slots, slots);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SimpleItemMapper<PlayerT, ItemT> before(@NotNull String existingKey) {
        final AbstractItemMapper<PlayerT, ItemT> parent = parent();
        if (parent != null) {
            return parent.registerBefore(existingKey, key(), this);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SimpleItemMapper<PlayerT, ItemT> after(@NotNull String existingKey) {
        final AbstractItemMapper<PlayerT, ItemT> parent = parent();
        if (parent != null) {
            return parent.registerAfter(existingKey, key(), this);
        }
        return this;
    }

    @Nullable
    @ApiStatus.Internal
    protected AbstractItemMapper<PlayerT, ItemT> parent() {
        return null;
    }

    @Override
    public @NotNull Class<ItemT> type() {
        return type;
    }

    @NotNull
    public BiFunction<PlayerT, ItemT, ItemT> function() {
        return function;
    }

    @NotNull
    public Set<ItemView> views() {
        return views == null ? Set.of() : Collections.unmodifiableSet(views);
    }

    @NotNull
    public Set<ItemSlot> slots() {
        return slots == null ? Set.of() : Collections.unmodifiableSet(slots);
    }

    @Override
    public boolean allow(@NotNull ItemView view) {
        return views.contains(view);
    }

    @Override
    public void apply(@NotNull ItemHolder<PlayerT, ItemT> holder) {
        if (isValidItem(holder) && isValidSlot(holder.slot())) {
            holder.item(function.apply(holder.player(), holder.item()));
        }
    }

    @Override
    public @NotNull ItemHolder<PlayerT, ItemT> apply(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view, @Nullable Object slot) {
        final ItemHolder<PlayerT, ItemT> holder = new ItemHolder<>();
        holder.reset(player, item, view, slot);
        apply(holder);
        return holder;
    }

    public boolean isValidItem(@NotNull ItemHolder<PlayerT, ItemT> holder) {
        if (predicate == null) {
            return true;
        }
        return predicate.test(holder.player(), holder.item());
    }

    public boolean isValidSlot(@Nullable Object slot) {
        if (slot == null || slots == null || slots.isEmpty() || slots.contains(slot)) {
            return true;
        }
        for (ItemSlot itemSlot : slots) {
            if (itemSlot == slot || itemSlot.isValid(slot)) {
                return true;
            }
        }
        return false;
    }
}
