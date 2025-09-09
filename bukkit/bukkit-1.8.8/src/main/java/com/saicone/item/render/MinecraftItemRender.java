package com.saicone.item.render;

import com.saicone.item.network.PacketItemRender;
import com.saicone.item.render.registry.PacketRewriterRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class MinecraftItemRender extends PacketItemRender<Player, ItemStack, Packet<?>> implements Listener {

    private final Plugin plugin;
    private final boolean inject;

    public MinecraftItemRender(@NotNull Plugin plugin) {
        this(plugin, true, true);
    }

    public MinecraftItemRender(@NotNull Plugin plugin, boolean inject, boolean register) {
        this.plugin = plugin;
        this.inject = inject;
        if (register) {
            try {
                final Field field = Class.forName("com.saicone.item.ItemRenderAPI").getDeclaredField("ITEM_RENDER");
                field.setAccessible(true);
                field.set(null, this);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    @Override
    public void load() {
        PacketRewriterRegistry.register(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean creative(@NotNull Player player) {
        return player.getGameMode() == GameMode.CREATIVE;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <ItemA> WrappedItemRender<Player, ItemA, ItemStack> wrapped(@NotNull Class<ItemA> type) {
        final WrappedItemRender<Player, ?, ItemStack> result;
        if (type.equals(org.bukkit.inventory.ItemStack.class)) {
            result = new BukkitItemRender(this);
        } else {
            throw new IllegalArgumentException("Cannot create wrapper for " + type.getName());
        }
        return (WrappedItemRender<Player, ItemA, ItemStack>) result;
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
        if (!inject) {
            return;
        }
        final CraftPlayer player = (CraftPlayer) event.getPlayer();
        final EntityPlayer serverPlayer = player.getHandle();
        final Channel channel = serverPlayer.playerConnection.networkManager.channel;
        final ChannelPipeline pipeline = channel.pipeline();
        pipeline.addAfter("decoder", "item_render_decoder", new ChannelDuplexHandler() {
            @Override
            public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
                if (msg instanceof Packet) {
                    msg = rewrite(player, (Packet<?>) msg); // receive
                    if (msg == null) {
                        return;
                    }
                }
                super.channelRead(ctx, msg);
            }
        });
        pipeline.addAfter("encoder", "item_render_encoder", new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof Packet) {
                    msg = rewrite(player, (Packet<?>) msg); // send
                    if (msg == null) {
                        return;
                    }
                }
                super.write(ctx, msg, promise);
            }
        });
    }
}
