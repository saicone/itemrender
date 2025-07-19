package com.saicone.item.render;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class ItemRenderBootstrap implements PluginBootstrap, PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact(ItemRenderLoader.dependency("group:artifact:classifier:version")), null));
        resolver.addRepository(new RemoteRepository.Builder("jitpack", "default", ItemRenderLoader.repository()).build());

        classpathBuilder.addLibrary(resolver);
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // empty method
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new ItemRenderPlugin(false);
    }
}
