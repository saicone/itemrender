package com.saicone.item;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public abstract class ItemMapperBus<PlayerT, ItemT> implements ItemMapper<PlayerT, ItemT> {

    private final ThreadLocal<ItemContext<PlayerT, ItemT>> context = ThreadLocal.withInitial(() -> new ItemContext<>(this));
    protected final Map<ItemView, List<ItemMapper<PlayerT, ?>>> mappers = new HashMap<>();

    @Nullable
    @ApiStatus.Internal
    protected ItemMapperBus<PlayerT, ?> parent() {
        return null;
    }

    @NotNull
    public List<ItemMapper<PlayerT, ?>> mappers(@NotNull ItemView view) {
        final List<ItemMapper<PlayerT, ?>> list = this.mappers.get(view);
        if (list == null) {
            return List.of();
        }
        return Collections.unmodifiableList(list);
    }

    public boolean contains(@NotNull String key) {
        for (Map.Entry<ItemView, List<ItemMapper<PlayerT, ?>>> entry : this.mappers.entrySet()) {
            for (ItemMapper<PlayerT, ?> mapper : entry.getValue()) {
                if (mapper.key().equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean contains(@NotNull ItemView view, @NotNull String key) {
        final List<ItemMapper<PlayerT, ?>> list = this.mappers.get(view);
        if (list == null) {
            return false;
        }
        for (ItemMapper<PlayerT, ?> mapper : list) {
            if (mapper.key().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public void register(@NotNull ItemMapper<PlayerT, ItemT> mapper) {
        compute(mapper);
    }

    public void register(@NotNull WrappedItemMapper<PlayerT, ?, ItemT> mapper) {
        compute(mapper);
    }

    public void unregister(@NotNull ItemMapper<PlayerT, ?> mapper) {
        removeIf((view, element) -> element == mapper);
    }

    public void unregister(@NotNull String key) {
        removeIf((view, element) -> element.key().equals(key));
    }

    protected void compute(@NotNull ItemMapper<PlayerT, ?> mapper) {
        for (ItemView view : mapper.views()) {
            List<ItemMapper<PlayerT, ?>> list = this.mappers.get(view);
            if (list == null) {
                list = new ArrayList<>();
                this.mappers.put(view, list);
            }
            if (list.contains(mapper)) {
                continue;
            }
            if (!list.isEmpty()) {
                if (mapper.priority() < list.get(0).priority()) {
                    list.add(0, mapper);
                    continue;
                } else if (mapper.priority() >= list.get(list.size() - 1).priority()) {
                    list.add(mapper);
                    continue;
                }
            }
            final ListIterator<ItemMapper<PlayerT, ?>> iterator = list.listIterator();
            while (iterator.hasNext()) {
                final ItemMapper<PlayerT, ?> element = iterator.next();
                if (mapper.priority() >= element.priority()) {
                    iterator.add(mapper);
                    break;
                }
            }
        }

        final ItemMapperBus<PlayerT, ?> parent = parent();
        if (parent != null) {
            parent.removeIf((view, element) -> element == this && !this.mappers.containsKey(view));
        }
    }

    protected void removeIf(@NotNull BiPredicate<ItemView, ItemMapper<PlayerT, ?>> predicate) {
        final Iterator<Map.Entry<ItemView, List<ItemMapper<PlayerT, ?>>>> iterator = this.mappers.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<ItemView, List<ItemMapper<PlayerT, ?>>> entry = iterator.next();
            entry.getValue().removeIf(element -> predicate.test(entry.getKey(), element));
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }

        final ItemMapperBus<PlayerT, ?> parent = parent();
        if (parent != null) {
            parent.removeIf((view, element) -> element == this && !this.mappers.containsKey(view));
        }
    }

    // ItemMapper methods

    @Override
    public @NotNull Set<ItemView> views() {
        return this.mappers.keySet();
    }

    @Override
    public boolean allow(@NotNull ItemView view) {
        return this.mappers.containsKey(view);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void apply(@NotNull ItemContext<PlayerT, ItemT> context) {
        if (this.mappers.isEmpty()) {
            return;
        }
        final List<ItemMapper<PlayerT, ?>> mappers = this.mappers.get(context.view());
        if (mappers == null) {
            return;
        }
        for (ItemMapper<PlayerT, ?> mapper : mappers) {
            try {
                if (mapper instanceof WrappedItemMapper) {
                    final WrappedItemMapper<PlayerT, Object, ItemT> wrappedMapper = (WrappedItemMapper<PlayerT, Object, ItemT>) mapper;
                    wrappedMapper.wrapAndApply(context);
                } else {
                    ((ItemMapper<PlayerT, ItemT>) mapper).apply(context);
                }
            } catch (Throwable t) {
                mapper.report(this, t);
            }
        }
    }

    @Override
    public @NotNull ItemContext<PlayerT, ItemT> context(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view) {
        // Instead of regular ItemMapper, this instance create an ItemContext per thread to save resources
        final ItemContext<PlayerT, ItemT> context = this.context.get();
        context.rotate(player, item, view);
        return context;
    }
}
