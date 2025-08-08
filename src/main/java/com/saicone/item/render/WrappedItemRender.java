package com.saicone.item.render;

import com.saicone.item.ItemContext;
import com.saicone.item.ItemRender;
import com.saicone.item.mapper.WrappedItemMapper;
import org.jetbrains.annotations.NotNull;

public abstract class WrappedItemRender<PlayerT, ItemA, ItemB> extends ItemRender<PlayerT, ItemA> implements WrappedItemMapper<PlayerT, ItemA, ItemB> {

    @Override
    public void load() {
        // empty method
    }

    @Override
    public void wrapAndApply(@NotNull ItemContext<PlayerT, ItemB> context) {
        final ItemA item = context.item() == null ? null : wrap(context.item());
        final var wrapped = context(context.player(), item, context.view()).with(context.slot(), context.containerId(), context.entityId(), context.recipeId());
        apply(wrapped);
        if (wrapped.edited()) {
            context.update(wrapped.item() == null ? null : unwrap(wrapped.item()), wrapped.edited(), wrapped.cancel());
        }
    }

    @Override
    protected <ItemA1> WrappedItemRender<PlayerT, ItemA1, ItemA> wrapped(@NotNull Class<ItemA1> type) {
        throw new IllegalArgumentException("Cannot create wrapper for " + type.getName());
    }
}
