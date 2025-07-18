package com.saicone.item.render.rewriter;

import com.saicone.item.ItemHolder;
import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import com.saicone.item.util.Lookup;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.MerchantRecipe;
import net.minecraft.server.v1_16_R3.MerchantRecipeList;
import net.minecraft.server.v1_16_R3.PacketPlayOutOpenWindowMerchant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

public class MerchantOffersRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, PacketPlayOutOpenWindowMerchant> {

    private static final MethodHandle OFFERS = Lookup.getter(PacketPlayOutOpenWindowMerchant.class, MerchantRecipeList.class, "b");
    private static final MethodHandle SET_OFFERS = Lookup.setter(PacketPlayOutOpenWindowMerchant.class, MerchantRecipeList.class, "b");

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
            final ItemStack[] items = applyItems(
                    () -> this.mapper.apply(player, offer.a(), view, ItemSlot.Merchant.COST_A),
                    () -> this.mapper.apply(player, offer.getBuyItem2(), view, ItemSlot.Merchant.COST_B),
                    () -> this.mapper.apply(player, offer.getSellingItem(), view, ItemSlot.Merchant.RESULT)
            );
            if (items != null) {
                if (items.length == 0) {
                    offers.add(offer);
                } else {
                    final MerchantRecipe newOffer = new MerchantRecipe(
                            items[0],
                            items[1],
                            items[2],
                            offer.getUses(),
                            offer.getMaxUses(),
                            offer.getXp(),
                            offer.getPriceMultiplier(),
                            offer.getDemand()
                    );
                    newOffer.setSpecialPrice(offer.getSpecialPrice());
                    offers.add(newOffer);
                    edited = true;
                }
            }
        }
        if (edited) {
            Lookup.invoke(SET_OFFERS, packet, offers);
        }
        return packet;
    }

    @Nullable
    protected ItemStack[] applyItems(@NotNull Supplier<ItemHolder<PlayerT, ItemStack>>... items) {
        final ItemStack[] array = new ItemStack[items.length];
        boolean edited = false;
        for (int i = 0; i < items.length; i++) {
            final ItemHolder<PlayerT, ItemStack> item = items[i].get();
            if (item == null) {
                return null;
            } else {
                array[i] = item.item();
                if (item.edited()) {
                    edited = true;
                }
            }
        }
        return edited ? array : new ItemStack[0];
    }
}
