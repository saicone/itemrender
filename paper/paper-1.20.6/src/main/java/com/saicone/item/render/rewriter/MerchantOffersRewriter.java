package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MerchantOffersRewriter<PlayerT> extends PacketRewriter<PlayerT, ItemStack, ClientboundMerchantOffersPacket> {

    public MerchantOffersRewriter(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper) {
        super(mapper);
    }

    @Override
    public @NotNull ItemView view(@NotNull PlayerT player) {
        return ItemView.MERCHANT;
    }

    @Override
    public @Nullable ClientboundMerchantOffersPacket rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundMerchantOffersPacket packet) {
        final MerchantOffers offers = new MerchantOffers();
        boolean edited = false;
        for (MerchantOffer offer : packet.getOffers()) {
            final var costA = this.mapper.apply(player, offer.getBaseCostA(), view, ItemSlot.Merchant.COST_A);
            final var costB = this.mapper.apply(player, offer.getCostB(), view, ItemSlot.Merchant.COST_B);
            final var result = this.mapper.apply(player, offer.getResult(), view, ItemSlot.Merchant.RESULT);

            if (!costA.edited() && !costB.edited() && !result.edited()) {
                offers.add(offer);
            } else {
                final ItemStack itemA = costA.itemOrDefault(ItemStack.EMPTY);
                offers.add(new MerchantOffer(
                        new ItemCost(itemA.getItemHolder(), itemA.getCount(), DataComponentPredicate.allOf(itemA.getComponents()), itemA),
                        Optional.ofNullable(costB.item()).map(item -> new ItemCost(item.getItemHolder(), item.getCount(), DataComponentPredicate.allOf(item.getComponents()), item)),
                        result.itemOrDefault(ItemStack.EMPTY),
                        offer.getUses(),
                        offer.getMaxUses(),
                        offer.getXp(),
                        offer.getPriceMultiplier(),
                        offer.getDemand(),
                        offer.ignoreDiscounts,
                        null
                ));
                edited = true;
            }
        }
        if (edited) {
            return new ClientboundMerchantOffersPacket(packet.getContainerId(), offers, packet.getVillagerLevel(), packet.getVillagerXp(), packet.showProgress(), packet.canRestock());
        }
        return packet;
    }
}
