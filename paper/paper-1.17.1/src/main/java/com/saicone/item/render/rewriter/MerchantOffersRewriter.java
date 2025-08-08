package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import com.saicone.item.network.PacketRewriter;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            final var costA = this.mapper.context(player, offer.getBaseCostA(), view)
                    .withContainer(packet.getContainerId(), ItemSlot.Merchant.COST_A)
                    .apply();
            final var costB = this.mapper.context(player, offer.getCostB(), view)
                    .withContainer(packet.getContainerId(), ItemSlot.Merchant.COST_B)
                    .apply();
            final var result = this.mapper.context(player, offer.getResult(), view)
                    .withContainer(packet.getContainerId(), ItemSlot.Merchant.RESULT)
                    .apply();

            if (!costA.edited() && !costB.edited() && !result.edited()) {
                offers.add(offer);
            } else {
                offers.add(new MerchantOffer(
                        costA.itemOrDefault(ItemStack.EMPTY),
                        costB.itemOrDefault(ItemStack.EMPTY),
                        result.itemOrDefault(ItemStack.EMPTY),
                        offer.getUses(),
                        offer.getMaxUses(),
                        offer.getXp(),
                        offer.getPriceMultiplier(),
                        offer.getDemand(),
                        offer.ignoreDiscounts
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
