/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.packs;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

public interface PackResources
extends PackMetadataResources {
    public static final String METADATA_EXTENSION = ".mcmeta";
    public static final String PACK_META = "pack.mcmeta";

    public @Nullable IoSupplier<InputStream> getResource(PackType var1, Identifier var2);

    public void listResources(PackType var1, String var2, String var3, ResourceOutput var4);

    public Set<String> getNamespaces(PackType var1);

    default public String packId() {
        return this.location().id();
    }

    default public Optional<KnownPack> knownPackInfo() {
        return this.location().knownPackInfo();
    }

    @FunctionalInterface
    public static interface Filter {
        public boolean isFiltered(Identifier var1);
    }

    @FunctionalInterface
    public static interface ResourceOutput
    extends BiConsumer<Identifier, IoSupplier<InputStream>> {
    }
}

