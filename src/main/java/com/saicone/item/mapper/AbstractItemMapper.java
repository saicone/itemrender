package com.saicone.item.mapper;

import com.saicone.item.ItemHolder;
import com.saicone.item.ItemMapper;
import com.saicone.item.ItemView;
import com.saicone.item.util.LabeledList;
import com.saicone.item.util.LinkedLabeledList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractItemMapper<PlayerT, ItemT> implements ItemMapper<PlayerT, ItemT> {

    private final ThreadLocal<ItemHolder<PlayerT, ItemT>> holder = ThreadLocal.withInitial(ItemHolder::new);
    private final Map<ItemView, LabeledList<ItemMapper<PlayerT, ?>>> mappers = new HashMap<>() {
        @Override
        public LabeledList<ItemMapper<PlayerT, ?>> put(ItemView key, LabeledList<ItemMapper<PlayerT, ?>> value) {
            final AbstractItemMapper<PlayerT, ?> parent = AbstractItemMapper.this.parent();
            if (parent != null && !parent.contains(key, AbstractItemMapper.this.key())) {
                parent.register(key, AbstractItemMapper.this.key(), AbstractItemMapper.this);
            }
            return super.put(key, value);
        }

        @Override
        public LabeledList<ItemMapper<PlayerT, ?>> remove(Object key) {
            final AbstractItemMapper<PlayerT, ?> parent = AbstractItemMapper.this.parent();
            if (parent != null) {
                parent.unregister((ItemView) key, AbstractItemMapper.this.key());
            }
            return super.remove(key);
        }
    };

    @Nullable
    @ApiStatus.Internal
    protected AbstractItemMapper<PlayerT, ?> parent() {
        return null;
    }

    @NotNull
    public Map<ItemView, LabeledList<ItemMapper<PlayerT, ?>>> mappers() {
        return Collections.unmodifiableMap(mappers);
    }

    @NotNull
    protected LabeledList<ItemMapper<PlayerT, ?>> mappers(@NotNull ItemView view) {
        LabeledList<ItemMapper<PlayerT, ?>> list = mappers.get(view);
        if (list == null) {
            list = new LinkedLabeledList<>();
            mappers.put(view, list);
        }
        return list;
    }

    protected boolean contains(@NotNull ItemView view) {
        return mappers.containsKey(view);
    }

    protected boolean contains(@NotNull String key) {
        for (Map.Entry<ItemView, LabeledList<ItemMapper<PlayerT, ?>>> entry : mappers.entrySet()) {
            if (entry.getValue().contains(key)) {
                return true;
            }
        }
        return false;
    }

    protected boolean contains(@NotNull ItemView view, @NotNull String key) {
        final LabeledList<ItemMapper<PlayerT, ?>> list = mappers.get(view);
        return list != null && list.contains(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void apply(@NotNull ItemHolder<PlayerT, ItemT> holder) {
        if (this.mappers.isEmpty()) {
            return;
        }
        final LabeledList<ItemMapper<PlayerT, ?>> mappers = this.mappers.get(holder.view());
        if (mappers == null) {
            return;
        }
        for (ItemMapper<PlayerT, ?> mapper : mappers) {
            if (mapper.type().equals(this.type())) {
                ((ItemMapper<PlayerT, ItemT>) mapper).apply(holder);
            } else {
                final WrappedItemMapper<PlayerT, Object, ItemT> wrappedMapper = (WrappedItemMapper<PlayerT, Object, ItemT>) mapper;
                wrappedMapper.wrapAndApply(holder);
            }
            if (holder.item() == null) {
                return;
            }
        }
    }

    @NotNull
    protected ItemHolder<PlayerT, ItemT> apply(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view, @Nullable Object slot) {
        final ItemHolder<PlayerT, ItemT> holder = holder(player, item, view, slot);
        apply(holder);
        return holder;
    }

    @NotNull
    public SimpleItemMapper<PlayerT, ItemT> register(@NotNull String key, @NotNull Function<ItemT, ItemT> function) {
        return simple(key, function);
    }

    @NotNull
    public SimpleItemMapper<PlayerT, ItemT> register(@NotNull String key, @NotNull BiFunction<PlayerT, ItemT, ItemT> function) {
        return simple(key, function);
    }

    @NotNull
    @Contract("_, _ -> param2")
    public SimpleItemMapper<PlayerT, ItemT> register(@NotNull String key, @NotNull SimpleItemMapper<PlayerT, ItemT> mapper) {
        unregister(key);
        for (ItemView view : mapper.views()) {
            register(view, key, mapper);
        }
        return mapper;
    }

    protected void register(@NotNull ItemView view, @NotNull String key, @NotNull ItemMapper<PlayerT, ?> mapper) {
        final LabeledList<ItemMapper<PlayerT, ?>> list = mappers(view);
        if (list.isEmpty() || mapper instanceof WrappedItemMapper<?,?,?>) {
            list.addLast(key, mapper);
        } else {
            for (int i = list.size() - 1; i >= 0; i--) {
                final ItemMapper<PlayerT, ?> saved = list.get(i);
                if (saved instanceof WrappedItemMapper<?,?,?>) {
                    continue;
                }
                list.addAfter(saved.key(), key, mapper);
                return;
            }
            list.addFirst(key, mapper);
        }
    }

    @NotNull
    @Contract("_, _, _ -> param3")
    public SimpleItemMapper<PlayerT, ItemT> registerBefore(@NotNull String existingKey, @NotNull String key, @NotNull SimpleItemMapper<PlayerT, ItemT> mapper) {
        unregister(key);
        for (ItemView view : mapper.views()) {
            mappers(view).addBefore(existingKey, key, mapper);
        }
        return mapper;
    }

    @NotNull
    @Contract("_, _, _ -> param3")
    public SimpleItemMapper<PlayerT, ItemT> registerAfter(@NotNull String existingKey, @NotNull String key, @NotNull SimpleItemMapper<PlayerT, ItemT> mapper) {
        unregister(key);
        for (ItemView view : mapper.views()) {
            mappers(view).addAfter(existingKey, key, mapper);
        }
        return mapper;
    }

    protected void unregister(@NotNull String key) {
        final Set<ItemView> remove = new HashSet<>();
        for (Map.Entry<ItemView, LabeledList<ItemMapper<PlayerT, ?>>> entry : mappers.entrySet()) {
            entry.getValue().removeKey(key);
            if (entry.getValue().isEmpty()) {
                remove.add(entry.getKey());
            }
        }
        for (ItemView view : remove) {
            mappers.remove(view);
        }
    }

    protected void unregister(@NotNull ItemView view, @NotNull String key) {
        final LabeledList<ItemMapper<PlayerT, ?>> list = mappers.get(view);
        if (list == null) {
            return;
        }
        list.removeKey(key);
        if (list.isEmpty()) {
            mappers.remove(view);
        }
    }

    @NotNull
    protected SimpleItemMapper<PlayerT, ItemT> simple(@NotNull String key, @NotNull Function<ItemT, ItemT> function) {
        return simple(key, (player, item) -> function.apply(item));
    }

    @NotNull
    protected SimpleItemMapper<PlayerT, ItemT> simple(@NotNull String key, @NotNull BiFunction<PlayerT, ItemT, ItemT> function) {
        return new SimpleItemMapper<>(type(), function) {
            @Override
            protected @NotNull AbstractItemMapper<PlayerT, ItemT> parent() {
                return AbstractItemMapper.this;
            }

            @Override
            public @NotNull String key() {
                return key;
            }
        };
    }

    protected ItemHolder<PlayerT, ItemT> holder(@NotNull PlayerT player, @Nullable ItemT item, @NotNull ItemView view, @Nullable Object slot) {
        final ItemHolder<PlayerT, ItemT> holder = this.holder.get();
        holder.reset(player, item, view, slot);
        return holder;
    }
}
