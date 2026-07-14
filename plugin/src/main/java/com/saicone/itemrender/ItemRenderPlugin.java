package com.saicone.itemrender;

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
        this.renderLoader = new ItemRenderLoader(this);
        try {
            renderLoader.init();
        } catch (Throwable t) {
            throw new RuntimeException("Cannot initialize ItemRenderLoader", t);
        }
    }

    @Override
    public void onLoad() {
        try {
            renderLoader.load(true, true);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot load ItemRenderLoader", t);
        }
    }

    @Override
    public void onEnable() {
        renderLoader.getItemRender().load();
    }

    @NotNull
    public ItemRenderLoader renderLoader() {
        return renderLoader;
    }
}
