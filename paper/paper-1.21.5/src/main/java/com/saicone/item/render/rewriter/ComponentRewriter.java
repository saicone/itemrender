package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ComponentRewriter<PlayerT> {

    @Nullable
    default Component rewrite(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper, @NotNull PlayerT player, @NotNull ItemView view, @NotNull Component component) {
        boolean edited = false;
        final MutableComponent mutable = component instanceof MutableComponent ? (MutableComponent) component : component.copy();

        final HoverEvent event = mutable.getStyle().getHoverEvent();
        if (event != null && event.action() == HoverEvent.Action.SHOW_ITEM) {
            final HoverEvent.ShowItem showItem = (HoverEvent.ShowItem) event;
            final var result = mapper.apply(player, showItem.item(), view, null);
            if (result.item() == null) {
                mutable.setStyle(mutable.getStyle().withHoverEvent(null));
                edited = true;
            } else if (result.edited()) {
                mutable.setStyle(mutable.getStyle().withHoverEvent(new HoverEvent.ShowItem(result.item())));
                edited = true;
            }
        }

        final List<Component> siblings = mutable.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            final Component sibling = rewrite(mapper, player, view, siblings.get(i));
            if (sibling != null) {
                siblings.set(i, sibling);
                edited = true;
            }
        }

        return edited ? mutable : null;
    }
}
