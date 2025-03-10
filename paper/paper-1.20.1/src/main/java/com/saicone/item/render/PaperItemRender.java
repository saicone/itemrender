package com.saicone.item.render;

import com.saicone.item.mapper.AbstractItemMapper;
import com.saicone.item.mapper.WrappedItemMapper;
import com.saicone.item.render.rewriter.MerchantOffersRewriter;
import com.saicone.item.render.rewriter.RecipesRewriter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperItemRender extends PacketItemRender<Player> implements Listener {

    private final Plugin plugin;

    public PaperItemRender(@NotNull Plugin plugin) {
        this(plugin, null, null);
    }

    public PaperItemRender(@NotNull Plugin plugin, @Nullable RecipesRewriter<Player> recipesRewriter, @Nullable MerchantOffersRewriter<Player> merchantOffersRewriter) {
        super(recipesRewriter, merchantOffersRewriter);
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
    public boolean isCreative(@NotNull Player player) {
        return player.getGameMode() == GameMode.CREATIVE;
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
}
