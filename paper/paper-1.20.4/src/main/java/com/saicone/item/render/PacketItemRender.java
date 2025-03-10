package com.saicone.item.render;

import com.mojang.datafixers.util.Pair;
import com.saicone.item.ItemRender;
import com.saicone.item.ItemView;
import com.saicone.item.render.rewriter.MerchantOffersRewriter;
import com.saicone.item.render.rewriter.RecipesRewriter;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Same as 1.20.1
public abstract class PacketItemRender<PlayerT> extends ItemRender<PlayerT, ItemStack> {

    private RecipesRewriter<PlayerT> recipesRewriter;
    private MerchantOffersRewriter<PlayerT> merchantOffersRewriter;

    public PacketItemRender() {
        this(null, null);
    }

    public PacketItemRender(@Nullable RecipesRewriter<PlayerT> recipesRewriter, @Nullable MerchantOffersRewriter<PlayerT> merchantOffersRewriter) {
        this.recipesRewriter = recipesRewriter != null ? recipesRewriter : new RecipesRewriter<>(this);
        this.merchantOffersRewriter = merchantOffersRewriter != null ? merchantOffersRewriter : new MerchantOffersRewriter<>(this);
    }

    @NotNull
    public RecipesRewriter<PlayerT> getRecipesRewriter() {
        return recipesRewriter;
    }

    @NotNull
    public MerchantOffersRewriter<PlayerT> getMerchantOffersRewriter() {
        return merchantOffersRewriter;
    }

    public void setRecipesRewriter(@NotNull RecipesRewriter<PlayerT> recipesRewriter) {
        this.recipesRewriter = recipesRewriter;
    }

    public void setMerchantOffersRewriter(@NotNull MerchantOffersRewriter<PlayerT> merchantOffersRewriter) {
        this.merchantOffersRewriter = merchantOffersRewriter;
    }

    public abstract boolean isCreative(@NotNull PlayerT player);

    @Nullable
    protected Packet<?> onPacketReceive(@NotNull PlayerT player, @NotNull Packet<?> packet) {
        if (packet instanceof ServerboundSetCreativeModeSlotPacket) {
            if (contains(ItemView.WINDOW_SERVER)) {
                return rewrite(player, (ServerboundSetCreativeModeSlotPacket) packet);
            }
        }
        return packet;
    }

    @Nullable
    protected Packet<?> onPacketSend(@NotNull PlayerT player, @NotNull Packet<?> packet) {
        if (packet instanceof ClientboundContainerSetSlotPacket) {
            if (isCreative(player)) {
                if (contains(ItemView.WINDOW_CREATIVE)) {
                    return rewrite(player, ItemView.WINDOW_CREATIVE, (ClientboundContainerSetSlotPacket) packet);
                }
            } else if (contains(ItemView.WINDOW)) {
                return rewrite(player, ItemView.WINDOW, (ClientboundContainerSetSlotPacket) packet);
            }
        } else if (packet instanceof ClientboundContainerSetContentPacket) {
            if (isCreative(player)) {
                if (contains(ItemView.WINDOW_CREATIVE)) {
                    return rewrite(player, ItemView.WINDOW_CREATIVE, (ClientboundContainerSetContentPacket) packet);
                }
            } else if (contains(ItemView.WINDOW)) {
                return rewrite(player, ItemView.WINDOW, (ClientboundContainerSetContentPacket) packet);
            }
        } else if (packet instanceof ClientboundUpdateRecipesPacket) {
            if (contains(ItemView.RECIPE)) {
                return recipesRewriter.rewrite(player, (ClientboundUpdateRecipesPacket) packet);
            }
        } else if (packet instanceof ClientboundMerchantOffersPacket) {
            if (contains(ItemView.MERCHANT)) {
                return merchantOffersRewriter.rewrite(player, (ClientboundMerchantOffersPacket) packet);
            }
        } else if (packet instanceof ClientboundSetEquipmentPacket) {
            if (contains(ItemView.EQUIPMENT)) {
                return rewrite(player, (ClientboundSetEquipmentPacket) packet);
            }
        } else if (packet instanceof ClientboundSetEntityDataPacket) {
            if (contains(ItemView.METADATA)) {
                return rewrite(player, (ClientboundSetEntityDataPacket) packet);
            }
        }
        return packet;
    }

    @Nullable
    protected Packet<?> rewrite(@NotNull PlayerT player, @NotNull ServerboundSetCreativeModeSlotPacket packet) {
        final var result = apply(player, packet.getItem(), ItemView.WINDOW_SERVER, packet.getSlotNum());
        if (result.item() == null) {
            return null;
        } else if (result.edited()) {
            return new ServerboundSetCreativeModeSlotPacket(packet.getSlotNum(), result.item());
        }
        return packet;
    }

    @Nullable
    protected Packet<?> rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundContainerSetSlotPacket packet) {
        final var result = apply(player, packet.getItem(), view, packet.getSlot());
        if (result.item() == null) {
            return null;
        } else if (result.edited()) {
            return new ClientboundContainerSetSlotPacket(packet.getContainerId(), packet.getStateId(), packet.getSlot(), result.item());
        }
        return packet;
    }

    @Nullable
    protected Packet<?> rewrite(@NotNull PlayerT player, @NotNull ItemView view, @NotNull ClientboundContainerSetContentPacket packet) {
        final List<ItemStack> items = packet.getItems();
        int empty = 0;
        for (int slot = 0; slot < items.size(); slot++) {
            final var result = apply(player, items.get(slot), view, slot);
            if (result.item() == null) {
                items.set(slot, ItemStack.EMPTY);
                empty++;
            } else if (result.edited()) {
                items.set(slot, result.item());
            }
        }
        if (empty == items.size()) {
            return null;
        }
        return packet;
    }

    @Nullable
    protected Packet<?> rewrite(@NotNull PlayerT player, @NotNull ClientboundSetEquipmentPacket packet) {
        final List<Pair<EquipmentSlot, ItemStack>> slots = packet.getSlots();
        for (int i = 0; i < slots.size(); i++) {
            final Pair<EquipmentSlot, ItemStack> pair = slots.get(i);
            final var result = apply(player, pair.getSecond(), ItemView.EQUIPMENT, pair.getFirst());
            if (result.item() == null) {
                slots.remove(i);
                i--;
            } else if (result.edited()) {
                slots.set(i, new Pair<>(pair.getFirst(), result.item()));
            }
        }
        return packet;
    }

    @Nullable
    protected Packet<?> rewrite(@NotNull PlayerT player, @NotNull ClientboundSetEntityDataPacket packet) {
        final List<SynchedEntityData.DataValue<?>> packedItems = packet.packedItems();
        for (int i = 0; i < packedItems.size(); i++) {
            final SynchedEntityData.DataValue<?> dataValue = packedItems.get(i);
            if (dataValue.serializer() == EntityDataSerializers.ITEM_STACK) {
                final var result = apply(player, (ItemStack) dataValue.value(), ItemView.METADATA, null);
                if (result.item() == null) {
                    packedItems.remove(i);
                    i--;
                } else if (result.edited()) {
                    packedItems.set(i, new SynchedEntityData.DataValue<>(dataValue.id(), EntityDataSerializers.ITEM_STACK, result.item()));
                }
            }
        }
        return packet;
    }
}
