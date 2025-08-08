package com.saicone.item.render.rewriter;

import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceGhostRecipeRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundPlaceGhostRecipePacket> implements SlotDisplayRewriter<PlayerT> {

    public PlaceGhostRecipeRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.RECIPE;
    }

    @Override
    public @Nullable ClientboundPlaceGhostRecipePacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundPlaceGhostRecipePacket packet) {
        final RecipeDisplay recipe = rewrite(packet.recipeDisplay(),
                (item, slot) -> this.mapper.context(player, item, view)
                        .withContainer(packet.containerId(), slot)
        );
        return recipe == null ? packet : new ClientboundPlaceGhostRecipePacket(packet.containerId(), recipe);
    }
}
