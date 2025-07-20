package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import net.minecraft.server.v1_13_R1.ChatComponentText;
import net.minecraft.server.v1_13_R1.ChatHoverable;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.ItemStack;
import net.minecraft.server.v1_13_R1.MojangsonParser;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ComponentRewriter<PlayerT, ItemT> {

    // NOTE:
    // v1_8_R3  = ItemStack#createStack()
    // v1_11_R1 = new ItemStack()
    // v1_13_R1 = ItemStack#a()
    //
    // Can be optimized? Yes. But will require a method lookup

    @Nullable
    @SuppressWarnings("unchecked")
    default <ComponentT> ComponentT rewrite(@NotNull PacketItemMapper<PlayerT, ItemT> mapper, @NotNull PlayerT player, @NotNull ItemView view, @NotNull ComponentT c) {
        final IChatBaseComponent component = (IChatBaseComponent) c;

        boolean edited = false;
        final ChatHoverable event = component.getChatModifier().i();
        if (event != null && event.a() == ChatHoverable.EnumHoverAction.SHOW_ITEM) {
            try {
                final ItemStack item = ItemStack.a(MojangsonParser.parse(event.b().getText()));
                final var result = mapper.apply(player, (ItemT) item, view, null);
                if (result.item() == null) {
                    component.setChatModifier(component.getChatModifier().setChatHoverable(null));
                    edited = true;
                } else if (result.edited()) {
                    component.setChatModifier(component.getChatModifier().setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_ITEM, new ChatComponentText(item.save(new NBTTagCompound()).toString()))));
                    edited = true;
                }
            } catch (Throwable ignored) { }
        }

        final List<IChatBaseComponent> siblings = component.a();
        for (int i = 0; i < siblings.size(); i++) {
            final IChatBaseComponent sibling = rewrite(mapper, player, view, siblings.get(i));
            if (sibling != null) {
                siblings.set(i, sibling);
                edited = true;
            }
        }

        return edited ? c : null;
    }
}