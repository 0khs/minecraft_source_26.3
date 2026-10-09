/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.shaderc.ShadercIncludeResult
 */
package com.mojang.renderpearl.api.pipeline;

import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.ShadercIncludeResult;

public interface ShaderSource
extends AutoCloseable {
    public @Nullable String getShader(Identifier var1, ShaderType var2);

    public @Nullable CachedIncludeSource getInclude(Identifier var1);

    @Override
    public void close();

    public record CachedIncludeSource(long includeResultPtr) implements AutoCloseable
    {
        public static CachedIncludeSource create(Identifier id, String source) {
            ShadercIncludeResult result = ShadercIncludeResult.calloc();
            result.source_name(MemoryUtil.memUTF8((CharSequence)id.toString(), (boolean)false));
            result.content(MemoryUtil.memUTF8((CharSequence)source, (boolean)false));
            return new CachedIncludeSource(result.address());
        }

        public static CachedIncludeSource createError(String message) {
            ShadercIncludeResult result = ShadercIncludeResult.calloc();
            result.source_name(MemoryUtil.memUTF8((CharSequence)"", (boolean)false));
            result.content(MemoryUtil.memUTF8((CharSequence)message, (boolean)false));
            return new CachedIncludeSource(result.address());
        }

        @Override
        public void close() {
            MemoryUtil.nmemFree((long)MemoryUtil.memGetAddress((long)(this.includeResultPtr + (long)ShadercIncludeResult.SOURCE_NAME)));
            MemoryUtil.nmemFree((long)MemoryUtil.memGetAddress((long)(this.includeResultPtr + (long)ShadercIncludeResult.CONTENT)));
            MemoryUtil.nmemFree((long)this.includeResultPtr);
        }
    }
}

