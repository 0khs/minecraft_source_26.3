/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackMetadataResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FileUtil;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FixedPathPackResources
implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;
    private final ResourceMetadata builtInMetadata;
    private @Nullable ResourceMetadata resourceMetadata;
    private final Set<String> namespaces;
    private final List<Path> rootPaths;
    private final Map<PackType, List<Path>> pathsForType;

    private FixedPathPackResources(PackLocationInfo location, ResourceMetadata metadata, Set<String> namespaces, List<Path> rootPaths, Map<PackType, List<Path>> pathsForType) {
        this.location = location;
        this.builtInMetadata = metadata;
        this.namespaces = namespaces;
        this.rootPaths = rootPaths;
        this.pathsForType = pathsForType;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String ... path) {
        FileUtil.validatePath(path);
        List<String> pathList = List.of(path);
        for (Path rootPath : this.rootPaths) {
            Path pathInRoot = FileUtil.resolvePath(rootPath, pathList);
            if (!Files.exists(pathInRoot, new LinkOption[0]) || !PathPackResources.validatePath(pathInRoot)) continue;
            return IoSupplier.create(pathInRoot);
        }
        return null;
    }

    public void listRawPaths(PackType type, Identifier resource, Consumer<Path> output) {
        FileUtil.decomposePath(resource.getPath()).ifSuccess(decomposedPath -> {
            String namespace = resource.getNamespace();
            for (Path typePath : this.pathsForType.get((Object)type)) {
                Path namespacedPath = typePath.resolve(namespace);
                output.accept(FileUtil.resolvePath(namespacedPath, decomposedPath));
            }
        }).ifError(error -> LOGGER.error("Invalid path {}: {}", (Object)resource, (Object)error.message()));
    }

    @Override
    public void listResources(PackType type, String namespace, String directory, PackResources.ResourceOutput output) {
        FileUtil.decomposePath(directory).ifSuccess(decomposedPath -> {
            List<Path> paths = this.pathsForType.get((Object)type);
            int pathsSize = paths.size();
            if (pathsSize == 1) {
                FixedPathPackResources.getResources(output, namespace, paths.getFirst(), decomposedPath);
            } else if (pathsSize > 1) {
                HashMap<Identifier, IoSupplier<InputStream>> resources = new HashMap<Identifier, IoSupplier<InputStream>>();
                for (int i = 0; i < pathsSize - 1; ++i) {
                    FixedPathPackResources.getResources(resources::putIfAbsent, namespace, paths.get(i), decomposedPath);
                }
                Path lastPath = paths.getLast();
                if (resources.isEmpty()) {
                    FixedPathPackResources.getResources(output, namespace, lastPath, decomposedPath);
                } else {
                    FixedPathPackResources.getResources(resources::putIfAbsent, namespace, lastPath, decomposedPath);
                    resources.forEach(output);
                }
            }
        }).ifError(error -> LOGGER.error("Invalid path {}: {}", (Object)directory, (Object)error.message()));
    }

    private static void getResources(PackResources.ResourceOutput result, String namespace, Path root, List<String> directory) {
        Path namespaceDir = root.resolve(namespace);
        PathPackResources.listPath(namespace, namespaceDir, directory, result);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        return (IoSupplier)FileUtil.decomposePath(location.getPath()).mapOrElse(decomposedPath -> {
            String namespace = location.getNamespace();
            for (Path typePath : this.pathsForType.get((Object)type)) {
                Path resource = FileUtil.resolvePath(typePath.resolve(namespace), decomposedPath);
                if (!Files.exists(resource, new LinkOption[0]) || !PathPackResources.validatePath(resource)) continue;
                return IoSupplier.create(resource);
            }
            return null;
        }, error -> {
            LOGGER.error("Invalid path {}: {}", (Object)location, (Object)error.message());
            return null;
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return this.namespaces;
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> metadataSerializer) {
        try {
            Optional<T> section;
            if (this.resourceMetadata == null) {
                this.resourceMetadata = AbstractPackMetadataResources.loadMetadata(this);
            }
            if ((section = this.resourceMetadata.getSection(metadataSerializer)).isPresent()) {
                return section.get();
            }
        }
        catch (Exception e) {
            LOGGER.warn("Failed to parse vanilla pack metadata", (Throwable)e);
        }
        return this.builtInMetadata.getSection(metadataSerializer).orElse(null);
    }

    @Override
    public PackLocationInfo location() {
        return this.location;
    }

    @Override
    public void close() {
    }

    public static class Builder {
        private final Set<Path> rootPaths = new LinkedHashSet<Path>();
        private final Map<PackType, Set<Path>> pathsForType = new EnumMap<PackType, Set<Path>>(PackType.class);
        private ResourceMetadata metadata = ResourceMetadata.EMPTY;
        private final Set<String> namespaces = new HashSet<String>();

        private static boolean validateDirPath(Path path) {
            if (!Files.exists(path, new LinkOption[0])) {
                return false;
            }
            if (!Files.isDirectory(path, new LinkOption[0])) {
                throw new IllegalArgumentException("Path " + String.valueOf(path.toAbsolutePath()) + " is not directory");
            }
            return true;
        }

        public void pushRootPath(Path path) {
            if (Builder.validateDirPath(path)) {
                this.rootPaths.add(path);
            }
        }

        public void pushPathForType(PackType packType2, Path path) {
            if (Builder.validateDirPath(path)) {
                this.pathsForType.computeIfAbsent(packType2, packType -> new LinkedHashSet()).add(path);
            }
        }

        public void setMetadata(ResourceMetadata metadata) {
            this.metadata = metadata;
        }

        public void exposeNamespace(Collection<String> namespaces) {
            this.namespaces.addAll(namespaces);
        }

        public FixedPathPackResources build(PackLocationInfo location) {
            return new FixedPathPackResources(location, this.metadata, Set.copyOf(this.namespaces), Builder.copyAndReverse(this.rootPaths), Util.makeEnumMap(PackType.class, packType -> Builder.copyAndReverse(this.pathsForType.getOrDefault(packType, Set.of()))));
        }

        private static List<Path> copyAndReverse(Collection<Path> input) {
            ArrayList<Path> paths = new ArrayList<Path>(input);
            Collections.reverse(paths);
            return List.copyOf(paths);
        }
    }
}

