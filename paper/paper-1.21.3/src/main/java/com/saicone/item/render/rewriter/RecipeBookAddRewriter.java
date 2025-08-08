package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ListIterator;

public class RecipeBookAddRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundRecipeBookAddPacket> implements SlotDisplayRewriter<PlayerT> {

    public RecipeBookAddRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.RECIPE;
    }

    @Override
    public @Nullable ClientboundRecipeBookAddPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundRecipeBookAddPacket packet) {
        final ListIterator<ClientboundRecipeBookAddPacket.Entry> iterator = packet.entries().listIterator();
        while (iterator.hasNext()) {
            final ClientboundRecipeBookAddPacket.Entry entry = iterator.next();
            final RecipeDisplayEntry result = rewrite(player, view, entry.contents());
            if (result != null) {
                iterator.set(new ClientboundRecipeBookAddPacket.Entry(result, entry.flags()));
            }
        }
        return packet;
    }

    @Nullable
    private RecipeDisplayEntry rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull RecipeDisplayEntry entry) {
        // TODO: Check this rewriter later to process RecipeDisplayEntry ingredients

        final RecipeDisplay display = rewrite(entry.display(),
                (item, slot) -> this.mapper.context(player, item, view)
                        .withRecipe(entry.id().index(), slot)
        );

        if (display == null) {
            return null;
        }
        return new RecipeDisplayEntry(
                entry.id(),
                display,
                entry.group(),
                entry.category(),
                entry.craftingRequirements()
        );
    }
}
