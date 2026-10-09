/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.server.packs.FixedPathPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FileSystemUtil;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class VanillaPackResourcesBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Consumer<VanillaPackResourcesBuilder> developmentConfig = builder -> {};
    private final FixedPathPackResources.Builder fullBuilder = new FixedPathPackResources.Builder();
    private final List<FixedPathPackResources.Builder> layeredBuilders = new ArrayList<FixedPathPackResources.Builder>();
    private final Set<String> namespaces = new HashSet<String>();
    private @Nullable ResourceMetadata metadata;
    private static final Map<PackType, Path> ROOT_DIR_BY_TYPE = (Map)Util.make(() -> {
        Class<VanillaPackResources> clazz = VanillaPackResources.class;
        synchronized (VanillaPackResources.class) {
            ImmutableMap.Builder result = ImmutableMap.builder();
            for (PackType type : PackType.values()) {
                String probeName = "/" + type.getDirectory() + "/.mcassetsroot";
                URL probeUrl = VanillaPackResources.class.getResource(probeName);
                if (probeUrl == null) {
                    LOGGER.error("File {} does not exist in classpath", (Object)probeName);
                    continue;
                }
                try {
                    URI probeUri = probeUrl.toURI();
                    String scheme = probeUri.getScheme();
                    if (!"jar".equals(scheme) && !"file".equals(scheme)) {
                        LOGGER.warn("Assets URL '{}' uses unexpected schema", (Object)probeUri);
                    }
                    Path probePath = FileSystemUtil.safeGetPath(probeUri);
                    result.put((Object)type, (Object)probePath.getParent());
                }
                catch (Exception e) {
                    LOGGER.error("Couldn't resolve path to vanilla assets", (Throwable)e);
                }
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return result.build();
        }
    });

    public VanillaPackResourcesBuilder() {
        this.layeredBuilders.add(new FixedPathPackResources.Builder());
    }

    private void forLastLayer(Consumer<FixedPathPackResources.Builder> task) {
        task.accept(this.fullBuilder);
        task.accept(this.layeredBuilders.getLast());
    }

    private void forAllLayers(Consumer<FixedPathPackResources.Builder> task) {
        task.accept(this.fullBuilder);
        for (FixedPathPackResources.Builder layeredBuilder : this.layeredBuilders) {
            task.accept(layeredBuilder);
        }
    }

    private void pushRootPath(Path path) {
        this.forLastLayer(builder -> builder.pushRootPath(path));
    }

    private void pushPathForType(PackType packType, Path path) {
        this.forLastLayer(builder -> builder.pushPathForType(packType, path));
    }

    public VanillaPackResourcesBuilder pushJarResources() {
        ROOT_DIR_BY_TYPE.forEach((packType, path) -> {
            this.pushRootPath(path.getParent());
            this.pushPathForType((PackType)((Object)packType), (Path)path);
        });
        return this;
    }

    public VanillaPackResourcesBuilder pushClasspathResources(PackType packType, Class<?> source) {
        Enumeration<URL> resources = null;
        try {
            resources = source.getClassLoader().getResources(packType.getDirectory() + "/");
        }
        catch (IOException iOException) {
            // empty catch block
        }
        while (resources != null && resources.hasMoreElements()) {
            URL url = resources.nextElement();
            try {
                URI uri = url.toURI();
                if (!"file".equals(uri.getScheme())) continue;
                Path assetsPath = Paths.get(uri);
                this.pushRootPath(assetsPath.getParent());
                this.pushPathForType(packType, assetsPath);
            }
            catch (Exception e) {
                LOGGER.error("Failed to extract path from {}", (Object)url, (Object)e);
            }
        }
        return this;
    }

    public VanillaPackResourcesBuilder applyDevelopmentConfig() {
        developmentConfig.accept(this);
        return this;
    }

    public VanillaPackResourcesBuilder pushUniversalPath(Path path) {
        this.pushRootPath(path);
        for (PackType packType : PackType.values()) {
            this.pushPathForType(packType, path.resolve(packType.getDirectory()));
        }
        return this;
    }

    public VanillaPackResourcesBuilder pushAssetPath(PackType packType, Path path) {
        this.pushRootPath(path);
        this.pushPathForType(packType, path);
        return this;
    }

    public VanillaPackResourcesBuilder setMetadata(ResourceMetadata metadata) {
        this.forAllLayers(builder -> builder.setMetadata(metadata));
        this.metadata = metadata;
        return this;
    }

    public VanillaPackResourcesBuilder exposeNamespace(String ... namespaces) {
        List<String> namespaceList = List.of(namespaces);
        this.forAllLayers(builder -> builder.exposeNamespace(namespaceList));
        this.namespaces.addAll(namespaceList);
        return this;
    }

    public VanillaPackResourcesBuilder pushLayer() {
        FixedPathPackResources.Builder newBuilder = new FixedPathPackResources.Builder();
        this.layeredBuilders.add(newBuilder);
        newBuilder.exposeNamespace(this.namespaces);
        if (this.metadata != null) {
            newBuilder.setMetadata(this.metadata);
        }
        return this;
    }

    public VanillaPackResources build(PackLocationInfo location) {
        return new VanillaPackResources(this.fullBuilder.build(location), this.layeredBuilders.stream().map(builder -> builder.build(location)).collect(Collectors.toUnmodifiableList()));
    }
}

