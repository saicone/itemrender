package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_16_R1.ChatHoverable;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;
import net.minecraft.server.v1_16_R1.IChatMutableComponent;
import net.minecraft.server.v1_16_R1.Item;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;

public interface ComponentRewriter<PlayerT, ItemT> {

    @Nullable
    @SuppressWarnings("unchecked")
    default <ComponentT> ComponentT rewrite(@NotNull PacketItemMapper<PlayerT, ItemT> mapper, @NotNull PlayerT player, @NotNull ItemView view, @NotNull ComponentT c) {
        final IChatBaseComponent component = (IChatBaseComponent) c;
        boolean edited = false;
        final IChatMutableComponent mutable = component instanceof IChatMutableComponent ? (IChatMutableComponent) component : component.mutableCopy();

        final ChatHoverable event = mutable.getChatModifier().getHoverEvent();
        if (event != null) {
            final ChatHoverable.c info = event.a(ChatHoverable.EnumHoverAction.SHOW_ITEM);
            if (info != null) {
                final var result = mapper.context(player, (ItemT) ItemStackInfo.getItemStack(info), view)
                        .apply();
                if (result.empty()) {
                    mutable.setChatModifier(mutable.getChatModifier().setChatHoverable(null));
                    edited = true;
                } else if (result.edited()) {
                    mutable.setChatModifier(mutable.getChatModifier().setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_ITEM, new ChatHoverable.c((ItemStack) result.item()))));
                    edited = true;
                }
            }
        }

        final List<IChatBaseComponent> siblings = mutable.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            final IChatBaseComponent sibling = rewrite(mapper, player, view, siblings.get(i));
            if (sibling != null) {
                siblings.set(i, sibling);
                edited = true;
            }
        }

        return edited ? (ComponentT) mutable : null;
    }

    final class ItemStackInfo {

        private static final MethodHandle ITEM = Lookup.getter(ChatHoverable.c.class, Item.class, "a");
        private static final MethodHandle COUNT = Lookup.getter(ChatHoverable.c.class, int.class, "b");
        private static final MethodHandle TAG = Lookup.getter(ChatHoverable.c.class, NBTTagCompound.class, "c");

        @NotNull
        private static ItemStack getItemStack(@NotNull ChatHoverable.c info) {
            final ItemStack item = new ItemStack(Lookup.invoke(ITEM, info), Lookup.invoke(COUNT, info));
            final NBTTagCompound tag = Lookup.invoke(TAG, info);
            if (tag != null) {
                item.setTag(tag);
            }
            return item;
        }
    }
}
