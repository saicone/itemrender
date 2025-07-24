package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.render.registry.ItemRegistry;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.MerchantRecipe;
import net.minecraft.server.v1_14_R1.MerchantRecipeList;
import net.minecraft.server.v1_14_R1.PacketPlayOutOpenWindowMerchant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;

public class MerchantOffersRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutOpenWindowMerchant> {

    private static final MethodHandle OFFERS = Lookup.getter(PacketPlayOutOpenWindowMerchant.class, MerchantRecipeList.class, "b");
    private static final MethodHandle SET_OFFERS = Lookup.setter(PacketPlayOutOpenWindowMerchant.class, MerchantRecipeList.class, "b");

    // Reflection is used due method MerchantRecipe#k() changed to MerchantRecipe#getDemand() on version 1.15.2
    private static final MethodHandle DEMAND = Lookup.getter(MerchantRecipe.class, int.class, "demand");

    public MerchantOffersRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.MERCHANT;
    }

    @Override
    public @Nullable PacketPlayOutOpenWindowMerchant rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull PacketPlayOutOpenWindowMerchant packet) {
        final MerchantRecipeList offers = new MerchantRecipeList();
        boolean edited = false;
        for (MerchantRecipe offer : (MerchantRecipeList) Lookup.invoke(OFFERS, packet)) {
            final var costA = this.mapper.apply(player, offer.a(), view, ItemSlot.Merchant.COST_A);
            final var costB = this.mapper.apply(player, offer.getBuyItem2(), view, ItemSlot.Merchant.COST_B);
            final var result = this.mapper.apply(player, offer.getSellingItem(), view, ItemSlot.Merchant.RESULT);
            if (!costA.edited() && !costB.edited() && !result.edited()) {
                offers.add(offer);
                continue;
            }

            final MerchantRecipe newOffer = new MerchantRecipe(
                    costA.itemOrDefault(ItemRegistry.empty()),
                    costB.itemOrDefault(ItemRegistry.empty()),
                    result.itemOrDefault(ItemRegistry.empty()),
                    offer.getUses(),
                    offer.getMaxUses(),
                    offer.getXp(),
                    offer.getPriceMultiplier(),
                    Lookup.invoke(DEMAND, offer)
            );
            newOffer.setSpecialPrice(offer.getSpecialPrice());
            offers.add(newOffer);
            edited = true;
        }
        if (edited) {
            Lookup.invoke(SET_OFFERS, packet, offers);
        }
        return packet;
    }
}
