package com.saicone.item.render;

import com.saicone.item.ItemContext;
import com.saicone.item.ItemRender;
import com.saicone.item.WrappedItemMapper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public abstract class WrappedItemRender<PlayerT, ItemA, ItemB> extends ItemRender<PlayerT, ItemA> implements WrappedItemMapper<PlayerT, ItemA, ItemB> {

    @Override
    public void load() {
        // empty method
    }

    @Override
    public void wrapAndApply(@NotNull ItemContext<PlayerT, ItemB> context) {
        final ItemA item = context.item() == null ? null : wrap(context.item());
        final var wrapped = context(context.player(), item, context.view()).with(context);
        apply(wrapped);
        if (wrapped.edited()) {
            context.update(wrapped.item() == null ? null : unwrap(wrapped.item()), wrapped.edited(), wrapped.cancel());
        }
    }

    @Override
    protected <ItemA1> ItemRender<PlayerT, ItemA1> createSubRender(@NotNull Class<ItemA1> type) {
        throw new IllegalStateException("The sub-render " + getClass().getName() + " have no compatibility with sub-render creation using Class object, please provide your own ItemWrapper<" + type.getSimpleName() + ", " + itemType().getSimpleName() + "> to #using() method");
    }
}
