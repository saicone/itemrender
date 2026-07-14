package com.saicone.itemrender;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public abstract class WrappedItemRender<ViewerT, ItemA, ItemB> extends ItemRender<ViewerT, ItemA> implements WrappedItemMapper<ViewerT, ItemA, ItemB> {

    @Override
    public void load() {
        // empty method
    }

    @Override
    public void wrapAndApply(@NotNull ItemContext<ViewerT, ItemB> context) {
        final ItemA item = context.item() == null ? null : wrap(context.item());
        final var wrapped = context(context.player(), item, context.view()).with(context);
        apply(wrapped);
        if (wrapped.edited()) {
            context.update(wrapped.item() == null ? null : unwrap(wrapped.item()), wrapped.edited(), wrapped.cancel());
        }
    }

    @Override
    protected <ItemA1> ItemRender<ViewerT, ItemA1> createSubRender(@NotNull Class<ItemA1> type) {
        throw new IllegalStateException("The sub-render " + getClass().getName() + " have no compatibility with sub-render creation using Class object, please provide your own ItemWrapper<" + type.getSimpleName() + ", " + itemType().getSimpleName() + "> to #using() method");
    }
}
