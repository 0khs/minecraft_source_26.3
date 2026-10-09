/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener<T>
extends SimplePreparableReloadListener<Map<Identifier, T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DynamicOps<JsonElement> ops;
    private final Codec<T> codec;
    private final FileToIdConverter lister;

    protected SimpleJsonResourceReloadListener(Codec<T> codec, FileToIdConverter lister) {
        this((DynamicOps<JsonElement>)JsonOps.INSTANCE, codec, lister);
    }

    private SimpleJsonResourceReloadListener(DynamicOps<JsonElement> ops, Codec<T> codec, FileToIdConverter lister) {
        this.ops = ops;
        this.codec = codec;
        this.lister = lister;
    }

    @Override
    protected Map<Identifier, T> prepare(ResourceManager manager, ProfilerFiller profiler) {
        HashMap result = new HashMap();
        for (Map.Entry<Identifier, Resource> entry : this.lister.listMatchingResources(manager).entrySet()) {
            Identifier location = entry.getKey();
            Identifier id = this.lister.fileToId(location);
            try {
                BufferedReader reader = entry.getValue().openAsReader();
                try {
                    this.codec.parse(this.ops, (Object)StrictJsonParser.parse(reader)).ifSuccess(parsed -> {
                        if (result.putIfAbsent(id, parsed) != null) {
                            throw new IllegalStateException("Duplicate data file ignored with ID " + String.valueOf(id));
                        }
                    }).ifError(error -> LOGGER.error("Couldn't parse data file '{}' from '{}': {}", new Object[]{id, location, error}));
                }
                finally {
                    if (reader == null) continue;
                    ((Reader)reader).close();
                }
            }
            catch (JsonParseException | IOException | IllegalArgumentException e) {
                LOGGER.error("Couldn't parse data file '{}' from '{}'", new Object[]{id, location, e});
            }
        }
        return result;
    }
}

