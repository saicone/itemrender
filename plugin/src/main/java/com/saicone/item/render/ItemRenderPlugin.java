package com.saicone.item.render;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ItemRenderPlugin extends JavaPlugin {

    private static ItemRenderPlugin instance;

    @NotNull
    public static ItemRenderPlugin get() {
        return instance;
    }

    private final ItemRenderLoader renderLoader;

    public ItemRenderPlugin() {
        this(true);
    }

    public ItemRenderPlugin(boolean initializeLoader) {
        this.renderLoader = new ItemRenderLoader(this);
        if (initializeLoader) {
            try {
                renderLoader.init();
            } catch (Throwable t) {
                throw new RuntimeException("Cannot initialize ItemRenderLoader", t);
            }
        }
    }

    @Override
    public void onLoad() {
        try {
            renderLoader.load();
        } catch (Throwable t) {
            throw new RuntimeException("Cannot load ItemRenderLoader", t);
        }
    }

    @NotNull
    public ItemRenderLoader renderLoader() {
        return renderLoader;
    }
}
