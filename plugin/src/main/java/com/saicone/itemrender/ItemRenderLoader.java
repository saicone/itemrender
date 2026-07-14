package com.saicone.itemrender;

import com.saicone.itemrender.network.PacketItemRender;
import com.saicone.itemrender.util.MC;
import com.saicone.itemrender.util.MavenMirror;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;

public class ItemRenderLoader {

    private static final String DEPENDENCY_GROUP = "${dependency_group}";
    private static final String DEPENDENCY_VERSION = "${dependency_version}";
    private static final String CLASS_ITEM_RENDER;
    static {
        final String type;
        if (MC.version().isNewerThanOrEquals(MC.V_1_17)) {
            type = "paper";
        } else {
            type = "bukkit";
        }
        CLASS_ITEM_RENDER = "com.saicone.itemrender.{type}.{version}.MinecraftItemRender".replace("{type}", type).replace("{version}", MC.version().name());
    }
    private static final boolean MOJANG_MAPPED;
    static {
        boolean mojangMapped = false;
        try {
            Class.forName("net.minecraft.nbt.CompoundTag");
            mojangMapped = true;
        } catch (ClassNotFoundException ignored) { }
        MOJANG_MAPPED = mojangMapped;
    }

    public static boolean isRenderPresent() {
        try {
            Class.forName(CLASS_ITEM_RENDER);
            return true;
        } catch (ClassNotFoundException ignored) { }
        return false;
    }

    @NotNull
    public static String dependency() {
        return dependency("group:artifact:version:classifier");
    }

    @NotNull
    public static String dependency(@NotNull String format) {
        final String artifact;
        final String classifier;
        if (MC.version().isNewerThanOrEquals(MC.V_1_17)) {
            artifact = "paper-" + MC.version();
            if (MOJANG_MAPPED) {
                classifier = "";
            } else {
                classifier = ":reobf";
            }
        } else {
            artifact = "bukkit-" + MC.version();
            classifier = "";
        }
        return format.replace("group", DEPENDENCY_GROUP.replace("{}", "."))
                .replace("artifact", artifact)
                .replace("version", DEPENDENCY_VERSION)
                .replace(":classifier", classifier);
    }

    private final Plugin plugin;

    private PublicClassLoader classLoader;
    private PacketItemRender<Player, Object, Object> itemRender;

    public ItemRenderLoader(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public void init() throws IOException {
        if (isRenderPresent()) {
            return;
        }

        final String[] dependency = dependency().split(":");

        final StringBuilder path = new StringBuilder();
        // group
        path.append(dependency[0].replace('.', '/'));
        // artifact
        path.append('/').append(dependency[1]);
        // version
        path.append('/').append(dependency[2]);
        // file name
        path.append('/').append(dependency[1]).append('-').append(dependency[2]);
        if (path.length() > 3) {
            path.append('-').append(dependency[3]);
        }
        path.append(".jar");

        File file = new File("libraries");
        for (String name : path.toString().split("/")) {
            file = new File(file, name);
        }

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            final URLConnection con = new URL(MavenMirror.getOrDefault(MavenMirror.DEFAULT) + path).openConnection();
            con.addRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream in = con.getInputStream(); OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
        }

        classLoader = new PublicClassLoader(new URL[]{ file.toURI().toURL() }, plugin.getClass().getClassLoader());
    }

    @SuppressWarnings("unchecked")
    public void load(boolean inject, boolean register) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final Class<? extends ItemRender> renderType = Class.forName(CLASS_ITEM_RENDER, true, getClassLoader()).asSubclass(ItemRender.class);
        itemRender = (PacketItemRender<Player, Object, Object>) renderType.getDeclaredConstructor(Plugin.class, boolean.class, boolean.class).newInstance(plugin, inject, register);
    }

    @NotNull
    public ClassLoader getClassLoader() {
        return classLoader == null ? plugin.getClass().getClassLoader() : classLoader;
    }

    @NotNull
    public PacketItemRender<Player, Object, Object> getItemRender() {
        return itemRender;
    }

    public static class PublicClassLoader extends URLClassLoader {

        public PublicClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }
    }
}
