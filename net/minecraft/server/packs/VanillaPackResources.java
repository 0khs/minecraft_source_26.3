/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FixedPathPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;

public class VanillaPackResources {
    private final FixedPathPackResources fullResources;
    private final List<PackResources> resourceLayers;

    public VanillaPackResources(FixedPathPackResources fullResources, List<PackResources> resourceLayers) {
        this.fullResources = fullResources;
        this.resourceLayers = resourceLayers;
    }

    public void listRawPaths(PackType type, Identifier resource, Consumer<Path> output) {
        this.fullResources.listRawPaths(type, resource, output);
    }

    public PackResources fullResources() {
        return this.fullResources;
    }

    public Pack.ResourcesSupplier asResourcesSupplier() {
        return new Pack.ResourcesSupplier(this){
            final /* synthetic */ VanillaPackResources this$0;
            {
                VanillaPackResources vanillaPackResources = this$0;
                Objects.requireNonNull(vanillaPackResources);
                this.this$0 = vanillaPackResources;
            }

            @Override
            public PackMetadataResources openMetadata(PackLocationInfo location) {
                return this.this$0.fullResources;
            }

            @Override
            public Stream<PackResources> openResources(PackLocationInfo location, Pack.Metadata metadata) {
                return this.this$0.resourceLayers.stream();
            }
        };
    }

    public ResourceManager asResourceManager() {
        FallbackResourceManager vanillaOnlyResourceManager = new FallbackResourceManager(PackType.CLIENT_RESOURCES, "minecraft");
        vanillaOnlyResourceManager.push(this.fullResources);
        return vanillaOnlyResourceManager;
    }
}

