/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.packs;

import java.io.IOException;
import java.io.InputStream;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

public interface PackMetadataResources
extends AutoCloseable {
    public PackLocationInfo location();

    public @Nullable IoSupplier<InputStream> getRootResource(String ... var1);

    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> var1) throws IOException;

    @Override
    public void close();
}

