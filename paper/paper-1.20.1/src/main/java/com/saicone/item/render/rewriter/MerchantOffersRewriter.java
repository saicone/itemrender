package com.saicone.item.render.rewriter;

import com.saicone.item.ItemHolder;
import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.mapper.AbstractItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MerchantOffersRewriter<PlayerT> implements PacketRewriter<PlayerT, ClientboundMerchantOffersPacket> {

    protected final AbstractItemMapper<PlayerT, ItemStack> mapper;

    public MerchantOffersRewriter(@NotNull AbstractItemMapper<PlayerT, ItemStack> mapper) {
        this.mapper = mapper;
    }

    @Override
    public @Nullable ClientboundMerchantOffersPacket rewrite(@NotNull PlayerT player, @NotNull ClientboundMerchantOffersPacket packet) {
        final MerchantOffers offers = new MerchantOffers();
        boolean edited = false;
        for (MerchantOffer offer : packet.getOffers()) {
            final ItemStack[] items = applyItems(
                    () -> this.mapper.apply(player, offer.getBaseCostA(), ItemView.MERCHANT, ItemSlot.Merchant.COST_A),
                    () -> this.mapper.apply(player, offer.getCostB(), ItemView.MERCHANT, ItemSlot.Merchant.COST_B),
                    () -> this.mapper.apply(player, offer.getResult(), ItemView.MERCHANT, ItemSlot.Merchant.RESULT)
            );
            if (items != null) {
                if (items.length == 0) {
                    offers.add(offer);
                } else {
                    offers.add(new MerchantOffer(items[0], items[1], items[2], offer.getUses(), offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier(), offer.getDemand(), offer.ignoreDiscounts));
                    edited = true;
                }
            }
        }
        if (edited) {
            return new ClientboundMerchantOffersPacket(packet.getContainerId(), offers, packet.getVillagerLevel(), packet.getVillagerXp(), packet.showProgress(), packet.canRestock());
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
