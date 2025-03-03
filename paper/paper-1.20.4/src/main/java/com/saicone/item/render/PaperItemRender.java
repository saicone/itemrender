package com.saicone.item.render;

import com.mojang.datafixers.util.Pair;
import com.saicone.item.ItemRender;
import com.saicone.item.ItemHolder;
import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.mapper.AbstractItemMapper;
import com.saicone.item.mapper.WrappedItemMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.core.NonNullList;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_20_R3.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class PaperItemRender extends ItemRender<Player, ItemStack> implements Listener {

    private final Plugin plugin;

    public PaperItemRender(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Class<ItemStack> type() {
        return ItemStack.class;
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <ItemA> WrappedItemMapper<Player, ItemA, ItemStack> wrapped(@NotNull Class<ItemA> type) {
        final WrappedItemMapper<Player, ?, ItemStack> result;
        if (type.equals(org.bukkit.inventory.ItemStack.class)) {
            result = new WrappedItemMapper<Player, org.bukkit.inventory.ItemStack, ItemStack>() {
                @Override
                protected @NotNull AbstractItemMapper<Player, ?> parent() {
                    return PaperItemRender.this;
                }

                @Override
                public @NotNull Class<org.bukkit.inventory.ItemStack> type() {
                    return org.bukkit.inventory.ItemStack.class;
                }

                @Override
                public @NotNull org.bukkit.inventory.ItemStack wrap(@NotNull ItemStack item) {
                    return CraftItemStack.asCraftMirror(item);
                }

                @Override
                public @NotNull ItemStack unwrap(@NotNull org.bukkit.inventory.ItemStack item) {
                    if (item instanceof CraftItemStack) {
                        return ((CraftItemStack) item).handle;
                    }
                    return CraftItemStack.asNMSCopy(item);
                }
            };
        } else {
            throw new IllegalArgumentException("Cannot create wrapper for " + type.getName());
        }
        return (WrappedItemMapper<Player, ItemA, ItemStack>) result;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(@NotNull PlayerGameModeChangeEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            if (event.getNewGameMode() == GameMode.CREATIVE) return;
        } else if (event.getNewGameMode() != GameMode.CREATIVE) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, player::updateInventory);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        final CraftPlayer player = (CraftPlayer) event.getPlayer();
        final ServerPlayer serverPlayer = player.getHandle();
        final Channel channel = serverPlayer.connection.connection.channel;
        final ChannelPipeline pipeline = channel.pipeline();
        pipeline.addAfter("encoder", "paper_item_render_receive", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
                if (msg instanceof Packet) {
                    msg = onPacketReceive(player, (Packet<?>) msg);
                    if (msg == null) {
                        return;
                    }
                }
                super.channelRead(ctx, msg);
            }
        });
        pipeline.addAfter("packet_handler", "paper_item_render_send", new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof Packet) {
                    msg = onPacketSend(player, (Packet<?>) msg);
                    if (msg == null) {
                        return;
                    }
                }
                super.write(ctx, msg, promise);
            }
        });
    }

    @Nullable
    protected Packet<?> onPacketReceive(@NotNull CraftPlayer player, @NotNull Packet<?> packet) {
        if (packet instanceof ServerboundSetCreativeModeSlotPacket) {
            if (contains(ItemView.WINDOW_SERVER)) {
                return onPacketReceive(player, (ServerboundSetCreativeModeSlotPacket) packet);
            }
        }
        return packet;
    }

    @Nullable
    protected Packet<?> onPacketReceive(@NotNull CraftPlayer player, @NotNull ServerboundSetCreativeModeSlotPacket packet) {
        final var result = apply(player, packet.getItem(), ItemView.WINDOW_SERVER, packet.getSlotNum());
        if (result.item() == null) {
            return null;
        } else if (result.edited()) {
            return new ServerboundSetCreativeModeSlotPacket(packet.getSlotNum(), result.item());
        }
        return packet;
    }

    @Nullable
    protected Packet<?> onPacketSend(@NotNull CraftPlayer player, @NotNull Packet<?> packet) {
        if (packet instanceof ClientboundContainerSetSlotPacket) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                if (contains(ItemView.WINDOW_CREATIVE)) {
                    return onPacketSend(player, ItemView.WINDOW_CREATIVE, (ClientboundContainerSetSlotPacket) packet);
                }
            } else if (contains(ItemView.WINDOW)) {
                return onPacketSend(player, ItemView.WINDOW, (ClientboundContainerSetSlotPacket) packet);
            }
        } else if (packet instanceof ClientboundContainerSetContentPacket) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                if (contains(ItemView.WINDOW_CREATIVE)) {
                    return onPacketSend(player, ItemView.WINDOW_CREATIVE, (ClientboundContainerSetContentPacket) packet);
                }
            } else if (contains(ItemView.WINDOW)) {
                return onPacketSend(player, ItemView.WINDOW, (ClientboundContainerSetContentPacket) packet);
            }
        } else if (packet instanceof ClientboundUpdateRecipesPacket) {
            if (contains(ItemView.RECIPE)) {
                return onPacketSend(player, (ClientboundUpdateRecipesPacket) packet);
            }
        } else if (packet instanceof ClientboundMerchantOffersPacket) {
            if (contains(ItemView.MERCHANT)) {
                return onPacketSend(player, (ClientboundMerchantOffersPacket) packet);
            }
        } else if (packet instanceof ClientboundSetEquipmentPacket) {
            if (contains(ItemView.EQUIPMENT)) {
                return onPacketSend(player, (ClientboundSetEquipmentPacket) packet);
            }
        } else if (packet instanceof ClientboundSetEntityDataPacket) {
            if (contains(ItemView.METADATA)) {
                return onPacketSend(player, (ClientboundSetEntityDataPacket) packet);
            }
        }
        return packet;
    }

    @Nullable
    protected Packet<?> onPacketSend(@NotNull CraftPlayer player, @NotNull ItemView view, @NotNull ClientboundContainerSetSlotPacket packet) {
        final var result = apply(player, packet.getItem(), view, packet.getSlot());
        if (result.item() == null) {
            return null;
        } else if (result.edited()) {
            return new ClientboundContainerSetSlotPacket(packet.getContainerId(), packet.getStateId(), packet.getSlot(), result.item());
        }
        return packet;
    }

    @Nullable
    protected Packet<?> onPacketSend(@NotNull CraftPlayer player, @NotNull ItemView view, @NotNull ClientboundContainerSetContentPacket packet) {
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
    protected Packet<?> onPacketSend(@NotNull CraftPlayer player, @NotNull ClientboundUpdateRecipesPacket packet) {
        final List<RecipeHolder<?>> recipes = packet.getRecipes();
        for (int i = 0; i < recipes.size(); i++) {
            final RecipeHolder<?> holder = recipes.get(i);
            final Object recipe = apply(player, holder.value());
            if (recipe == null) {
                recipes.remove(i);
                i--;
            } else if (recipe != DUMMY_OBJECT) {
                recipes.set(i, new RecipeHolder<>(holder.id(), (Recipe<?>) recipe));
            }
        }
        if (recipes.isEmpty()) {
            return null;
        }
        return packet;
    }

    @Nullable
    protected Packet<?> onPacketSend(@NotNull CraftPlayer player, @NotNull ClientboundMerchantOffersPacket packet) {
        final MerchantOffers offers = new MerchantOffers();
        boolean edited = false;
        for (MerchantOffer offer : packet.getOffers()) {
            final ItemStack[] items = applyItems(
                    () -> apply(player, offer.getBaseCostA(), ItemView.MERCHANT, ItemSlot.Merchant.COST_A),
                    () -> apply(player, offer.getCostB(), ItemView.MERCHANT, ItemSlot.Merchant.COST_B),
                    () -> apply(player, offer.getResult(), ItemView.MERCHANT, ItemSlot.Merchant.RESULT)
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
    protected Packet<?> onPacketSend(@NotNull CraftPlayer player, @NotNull ClientboundSetEquipmentPacket packet) {
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
    protected Packet<?> onPacketSend(@NotNull CraftPlayer player, @NotNull ClientboundSetEntityDataPacket packet) {
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

    private final NonNullList<?> EMPTY_LIST = NonNullList.create();
    private final Object DUMMY_OBJECT = new Object();

    private static final MethodHandle COOKING_INGREDIENT;

    private static final MethodHandle SINGLE_INGREDIENT;

    private static final MethodHandle TRANSFORM_TEMPLATE;
    private static final MethodHandle TRANSFORM_BASE;
    private static final MethodHandle TRANSFORM_ADDITION;

    private static final MethodHandle TRIM_TEMPLATE;
    private static final MethodHandle TRIM_BASE;
    private static final MethodHandle TRIM_ADDITION;

    static {
        MethodHandle cooking$ingredient = null;

        MethodHandle single$ingredient = null;

        MethodHandle transform$template = null;
        MethodHandle transform$base = null;
        MethodHandle transform$addition = null;

        MethodHandle trim$template = null;
        MethodHandle trim$base = null;
        MethodHandle trim$addition = null;
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            final Field AbstractCookingRecipe$ingredient = AbstractCookingRecipe.class.getDeclaredField("d");
            AbstractCookingRecipe$ingredient.setAccessible(true);
            cooking$ingredient = lookup.unreflectGetter(AbstractCookingRecipe$ingredient);

            final Field SingleItemRecipe$ingredient = SingleItemRecipe.class.getDeclaredField("a");
            SingleItemRecipe$ingredient.setAccessible(true);
            single$ingredient = lookup.unreflectGetter(SingleItemRecipe$ingredient);

            final Field SmithingTransformRecipe$template = SmithingTransformRecipe.class.getDeclaredField("a");
            SmithingTransformRecipe$template.setAccessible(true);
            transform$template = lookup.unreflectGetter(SmithingTransformRecipe$template);
            final Field SmithingTransformRecipe$base = SmithingTransformRecipe.class.getDeclaredField("b");
            SmithingTransformRecipe$base.setAccessible(true);
            transform$base = lookup.unreflectGetter(SmithingTransformRecipe$base);
            final Field SmithingTransformRecipe$addition = SmithingTransformRecipe.class.getDeclaredField("c");
            SmithingTransformRecipe$addition.setAccessible(true);
            transform$addition = lookup.unreflectGetter(SmithingTransformRecipe$addition);

            final Field SmithingTrimRecipe$template = SmithingTrimRecipe.class.getDeclaredField("a");
            SmithingTrimRecipe$template.setAccessible(true);
            trim$template = lookup.unreflectGetter(SmithingTrimRecipe$template);
            final Field SmithingTrimRecipe$base = SmithingTrimRecipe.class.getDeclaredField("b");
            SmithingTrimRecipe$base.setAccessible(true);
            trim$base = lookup.unreflectGetter(SmithingTrimRecipe$base);
            final Field SmithingTrimRecipe$addition = SmithingTrimRecipe.class.getDeclaredField("c");
            SmithingTrimRecipe$addition.setAccessible(true);
            trim$addition = lookup.unreflectGetter(SmithingTrimRecipe$addition);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        COOKING_INGREDIENT = cooking$ingredient;

        SINGLE_INGREDIENT = single$ingredient;

        TRANSFORM_TEMPLATE = transform$template;
        TRANSFORM_BASE = transform$base;
        TRANSFORM_ADDITION = transform$addition;

        TRIM_TEMPLATE = trim$template;
        TRIM_BASE = trim$base;
        TRIM_ADDITION = trim$addition;
    }

    @Nullable
    protected Object apply(@NotNull CraftPlayer player, @NotNull Recipe<?> recipe) {
        if (recipe instanceof AbstractCookingRecipe cooking) {
            final var result = apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), ItemView.RECIPE, ItemSlot.Recipe.COOKING_RESULT);
            if (result.item() == null) {
                return null;
            }

            final Ingredient ingredient;
            try {
                ingredient = (Ingredient) COOKING_INGREDIENT.invoke(cooking);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            Ingredient applied = apply(player, ingredient, ItemSlot.Recipe.COOKING_INGREDIENT);
            if (applied == null) {
                return null;
            } else if (applied == Ingredient.EMPTY) {
                if (!result.edited()) {
                    return DUMMY_OBJECT;
                }
                applied = ingredient;
            }

            if (recipe instanceof BlastingRecipe) {
                return new BlastingRecipe(cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof CampfireCookingRecipe) {
                return new CampfireCookingRecipe(cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof SmeltingRecipe) {
                return new SmeltingRecipe(cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            } else if (recipe instanceof SmokingRecipe) {
                return new SmokingRecipe(cooking.getGroup(), cooking.category(), applied, result.item(), cooking.getExperience(), cooking.getCookingTime());
            }
        } else if (recipe instanceof ShapedRecipe shaped) {
            final var result = apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), ItemView.RECIPE, ItemSlot.Recipe.SHAPED_RESULT);
            if (result.item() == null) {
                return null;
            }

            NonNullList<Ingredient> ingredients = apply(player, shaped.getIngredients(), "shaped:ingredient", ItemSlot.Recipe.SHAPED_INGREDIENT);
            if (ingredients == null) {
                return null;
            } else if (ingredients == EMPTY_LIST) {
                if (!result.edited()) {
                    return DUMMY_OBJECT;
                }
                ingredients = shaped.getIngredients();
            }

            return new ShapedRecipe(shaped.getGroup(), shaped.category(), new ShapedRecipePattern(shaped.getWidth(), shaped.getHeight(), ingredients, Optional.empty()), result.item(), shaped.showNotification());
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            final var result = apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), ItemView.RECIPE, ItemSlot.Recipe.SHAPELESS_RESULT);
            if (result.item() == null) {
                return null;
            }

            NonNullList<Ingredient> ingredients = apply(player, shapeless.getIngredients(), "shapeless:ingredient", ItemSlot.Recipe.SHAPELESS_INGREDIENT);
            if (ingredients == null) {
                return null;
            } else if (ingredients == EMPTY_LIST) {
                if (!result.edited()) {
                    return DUMMY_OBJECT;
                }
                ingredients = shapeless.getIngredients();
            }

            return new ShapelessRecipe(shapeless.getGroup(), shapeless.category(), result.item(), ingredients);
        } else if (recipe instanceof SmithingTransformRecipe transform) {
            final var result = apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), ItemView.RECIPE, ItemSlot.Recipe.TRANSFORM_RESULT);
            if (result.item() == null) {
                return null;
            }

            final Ingredient[] ingredients = apply(
                    () -> apply(player, transform, TRANSFORM_TEMPLATE, ItemSlot.Recipe.TRANSFORM_TEMPLATE),
                    () -> apply(player, transform, TRANSFORM_BASE, ItemSlot.Recipe.TRANSFORM_BASE),
                    () -> apply(player, transform, TRANSFORM_ADDITION, ItemSlot.Recipe.TRANSFORM_ADDITION)
            );
            if (ingredients == null) {
                return null;
            } else if (ingredients.length == 0) {
                return DUMMY_OBJECT;
            }

            return new SmithingTransformRecipe(ingredients[0], ingredients[1], ingredients[2], result.item());
        } else if (recipe instanceof SmithingTrimRecipe trim) {
            final Ingredient[] ingredients = apply(
                    () -> apply(player, trim, TRIM_TEMPLATE, ItemSlot.Recipe.TRIM_TEMPLATE),
                    () -> apply(player, trim, TRIM_BASE, ItemSlot.Recipe.TRIM_BASE),
                    () -> apply(player, trim, TRIM_ADDITION, ItemSlot.Recipe.TRIM_ADDITION)
            );
            if (ingredients == null) {
                return null;
            } else if (ingredients.length == 0) {
                return DUMMY_OBJECT;
            }

            return new SmithingTrimRecipe(ingredients[0], ingredients[1], ingredients[2]);
        } else if (recipe instanceof StonecutterRecipe stonecutter) {
            final var result = apply(player, recipe.getResultItem(CraftRegistry.getMinecraftRegistry()), ItemView.RECIPE, ItemSlot.Recipe.STONECUTTER_RESULT);
            if (result.item() == null) {
                return null;
            }

            final Ingredient ingredient;
            try {
                ingredient = (Ingredient) SINGLE_INGREDIENT.invoke(stonecutter);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            Ingredient applied = apply(player, ingredient, ItemSlot.Recipe.STONECUTTER_INGREDIENT);
            if (applied == null) {
                return null;
            } else if (applied == Ingredient.EMPTY) {
                if (!result.edited()) {
                    return DUMMY_OBJECT;
                }
                applied = ingredient;
            }

            return new StonecutterRecipe(stonecutter.getGroup(), applied, result.item());
        }
        return DUMMY_OBJECT;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected NonNullList<Ingredient> apply(@NotNull CraftPlayer player, @NotNull NonNullList<Ingredient> ingredients, @NotNull String slotType, @NotNull ItemSlot[] slots) {
        final NonNullList<Ingredient> list = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
        int empty = 0;
        boolean edited = false;
        for (int i = 0; i < ingredients.size(); i++) {
            final Ingredient ingredient = ingredients.get(i);
            final Ingredient result = apply(player, ingredient, i < 9 ? slots[i] : ItemSlot.pair(slotType, i));
            if (result == Ingredient.EMPTY) {
                list.add(ingredient);
                continue;
            }
            if (result == null) {
                list.add(Ingredient.EMPTY);
                empty++;
            } else {
                list.add(result);
            }
            edited = true;
        }
        if (empty == list.size()) {
            return null;
        }
        list.clear();
        return edited ? list : (NonNullList<Ingredient>) EMPTY_LIST;
    }

    @Nullable
    protected Ingredient[] apply(@NotNull Supplier<Ingredient>... ingredients) {
        final Ingredient[] array = new Ingredient[ingredients.length];
        boolean edited = false;
        for (int i = 0; i < ingredients.length; i++) {
            final Ingredient ingredient = ingredients[i].get();
            if (ingredient == null) {
                return null;
            } else {
                array[i] = ingredient;
                if (ingredient != Ingredient.EMPTY) {
                    edited = true;
                }
            }
        }
        return edited ? array : new Ingredient[0];
    }

    @Nullable
    protected Ingredient apply(@NotNull CraftPlayer player, @NotNull SmithingRecipe recipe, @NotNull MethodHandle field, @NotNull ItemSlot slot) {
        try {
            return apply(player, (Ingredient) field.invoke(recipe), slot);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Nullable
    protected Ingredient apply(@NotNull CraftPlayer player, @NotNull Ingredient ingredient, @NotNull Object slot) {
        if (ingredient == Ingredient.EMPTY) {
            return Ingredient.EMPTY;
        }
        final List<Ingredient.Value> items = new ArrayList<>(ingredient.getItems().length);
        boolean edited = false;
        for (ItemStack item : ingredient.getItems()) {
            if (item == null) {
                return null;
            }
            final var result = apply(player, item, ItemView.RECIPE, slot);
            items.add(new Ingredient.ItemValue(result.item()));
            if (result.edited()) {
                edited = true;
            }
        }
        if (edited) {
            return new Ingredient(items.stream());
        }
        items.clear();
        return Ingredient.EMPTY;
    }

    @Nullable
    protected ItemStack[] applyItems(@NotNull Supplier<ItemHolder<Player, ItemStack>>... items) {
        final ItemStack[] array = new ItemStack[items.length];
        boolean edited = false;
        for (int i = 0; i < items.length; i++) {
            final ItemHolder<Player, ItemStack> item = items[i].get();
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
