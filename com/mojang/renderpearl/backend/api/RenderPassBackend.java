/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.PointerBuffer
 */
package com.mojang.renderpearl.backend.api;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;

public interface RenderPassBackend {
    public void pushDebugGroup(Supplier<String> var1);

    public void popDebugGroup();

    public void setPipeline(BackendRenderPipeline var1);

    public void setUniform(int var1, @Nullable Object var2);

    public void pushConstants(ByteBuffer var1);

    public void enableScissor(int var1, int var2, int var3, int var4);

    public void disableScissor();

    public void setVertexBuffer(int var1, @Nullable GpuBufferSlice var2);

    public void setIndexBuffer(GpuBuffer var1, IndexType var2);

    public void drawIndexed(int var1, int var2, int var3, int var4, int var5);

    public void multiDrawIndexed(IntBuffer var1, int var2, int var3, int var4);

    public void multiDrawIndexed(PointerBuffer var1, IntBuffer var2, IntBuffer var3, int var4);

    public void drawIndexedIndirect(GpuBufferSlice var1, int var2);

    public void draw(int var1, int var2, int var3, int var4);

    public void multiDraw(IntBuffer var1, int var2, int var3, int var4);

    public void multiDraw(IntBuffer var1, IntBuffer var2, int var3);

    public void drawIndirect(GpuBufferSlice var1, int var2);

    public void writeTimestamp(GpuQueryPool var1, int var2);
}

