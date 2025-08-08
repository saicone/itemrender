package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetCursorItemRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundSetCursorItemPacket> {

    public SetCursorItemRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return this.mapper.creative(player) ? ItemView.WINDOW_CREATIVE : ItemView.WINDOW;
    }

    @Override
    public @Nullable ClientboundSetCursorItemPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundSetCursorItemPacket packet) {
        final var result = this.mapper.context(player, packet.contents(), view)
                .withContainer(-1, ItemSlot.Window.CURSOR)
                .apply();
        if (result.edited()) {
            return new ClientboundSetCursorItemPacket(result.itemOrDefault(ItemStack.EMPTY));
        }
        return packet;
    }
}
