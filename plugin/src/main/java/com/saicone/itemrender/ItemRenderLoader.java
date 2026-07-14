package com.saicone.itemrender;

import com.saicone.itemrender.network.PacketItemRender;
import com.saicone.itemrender.util.MC;
import com.saicone.itemrender.util.MavenMirror;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ItemRenderLoader {

    // Sanitize version
    private static final MC VERSION;
    static {
        MC version = MC.version();
        if (version.isNewerThanOrEquals(MC.V_26_1)) {
            version = MC.fromString(version.major() + "." + version.feature());
        } else {
            for (int i = MC.values().size(); i-- > 0; ) {
                final MC value = MC.values().get(i);
                if (value.major() == version.major() && value.feature() == version.feature() && value.revision() == version.revision()) {
                    version = value;
                    break;
                }
            }
        }
        VERSION = version;
    }

    private static final boolean MOJANG_MAPPED;
    static {
        if (VERSION.isNewerThanOrEquals(MC.V_26_1)) {
            MOJANG_MAPPED = true;
        } else {
            boolean mojangMapped = false;
            try {
                Class.forName("net.minecraft.nbt.CompoundTag");
                mojangMapped = true;
            } catch (ClassNotFoundException ignored) { }
            MOJANG_MAPPED = mojangMapped;
        }
    }

    private static final String DEPENDENCY_GROUP = "property.dependency.group";
    private static final String DEPENDENCY_ARTIFACT;
    private static final String DEPENDENCY_VERSION = "property.dependency.version";
    private static final String DEPENDENCY_CLASSIFIER;
    static {
        if (VERSION.isNewerThanOrEquals(MC.V_1_17)) {
            DEPENDENCY_ARTIFACT = "paper-" + VERSION;
            if (MOJANG_MAPPED) {
                DEPENDENCY_CLASSIFIER = "";
            } else {
                DEPENDENCY_CLASSIFIER = "reobf";
            }
        } else {
            DEPENDENCY_ARTIFACT = "bukkit-" + VERSION;
            DEPENDENCY_CLASSIFIER = "";
        }
    }

    private static final String CLASS_ITEM_RENDER;
    static {
        final String type;
        if (VERSION.isNewerThanOrEquals(MC.V_1_17)) {
            type = "paper";
        } else {
            type = "bukkit";
        }
        CLASS_ITEM_RENDER = "com.saicone.itemrender.{type}.{version}.MinecraftItemRender".replace("{type}", type).replace("{version}", VERSION.name());
    }

    @NotNull
    private static String group() {
        return DEPENDENCY_GROUP;
    }

    @NotNull
    private static String version() {
        return DEPENDENCY_VERSION;
    }

    public static boolean isRenderPresent() {
        try {
            Class.forName(CLASS_ITEM_RENDER);
            return true;
        } catch (ClassNotFoundException ignored) { }
        return false;
    }

    @Nullable
    public static Path localFile() {
        return localFile(ItemRenderLoader.class);
    }

    @Nullable
    public static Path localFile(@NotNull Class<?> source) {
        final String fileName = DEPENDENCY_ARTIFACT + (DEPENDENCY_CLASSIFIER.isEmpty() ? "" : "-" + DEPENDENCY_CLASSIFIER);
        final String entryName = "META-INF/modules/" + fileName + ".jar";

        final CodeSource codeSource = source.getProtectionDomain().getCodeSource();
        try (JarFile jar = new JarFile(new File(codeSource.getLocation().toURI()))) {
            final JarEntry entry = jar.getJarEntry(entryName);
            if (entry != null) {
                final Path tempFile = Files.createTempFile(fileName, ".jar");
                tempFile.toFile().deleteOnExit();

                try (InputStream in = new BufferedInputStream(jar.getInputStream(entry))) {
                    Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                }

                return tempFile;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return null;
    }

    @NotNull
    public static String dependency() {
        return dependency("group:artifact:version:classifier");
    }

    @NotNull
    public static String dependency(@NotNull String format) {
        return format.replace("group", group().replace("{}", "."))
                .replace("artifact", DEPENDENCY_ARTIFACT)
                .replace("version", version())
                .replace(":classifier", DEPENDENCY_CLASSIFIER.isEmpty() ? "" : ":" + DEPENDENCY_CLASSIFIER);
    }

    private final Plugin plugin;

    private PublicClassLoader classLoader;
    private PacketItemRender<Player, Object, Object> itemRender;

    public ItemRenderLoader(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public void init() throws IOException {
        plugin.getLogger().info("Detected version: " + VERSION);

        if (isRenderPresent()) {
            plugin.getLogger().info("The render class is already on class path");
            return;
        }

        final Path localFile = ItemRenderLoader.localFile();
        if (localFile != null) {
            plugin.getLogger().info("Local module was found on .jar file");
            classLoader = new PublicClassLoader(new URL[]{ localFile.toUri().toURL() }, plugin.getClass().getClassLoader());
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
