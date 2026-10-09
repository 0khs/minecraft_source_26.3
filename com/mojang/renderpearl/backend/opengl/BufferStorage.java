/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GLCapabilities
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.opengl.GlHeuristics;
import java.nio.ByteBuffer;
import java.util.Set;
import org.lwjgl.opengl.GLCapabilities;

public abstract class BufferStorage {
    public static BufferStorage create(GLCapabilities capabilities, Set<String> enabledExtensions, boolean forceMutable) {
        if (!forceMutable && capabilities.GL_ARB_buffer_storage && GlDevice.USE_GL_ARB_buffer_storage) {
            enabledExtensions.add("GL_ARB_buffer_storage");
            return new Immutable();
        }
        return new Mutable();
    }

    public abstract GlBuffer createBuffer(GlHeuristics var1, DirectStateAccess var2, @GpuBuffer.Usage int var3, long var4);

    public abstract GlBuffer createBuffer(GlHeuristics var1, DirectStateAccess var2, @GpuBuffer.Usage int var3, ByteBuffer var4);

    private static class Immutable
    extends BufferStorage {
        private Immutable() {
        }

        @Override
        public GlBuffer createBuffer(GlHeuristics heuristics, DirectStateAccess dsa, @GpuBuffer.Usage int usage, long size) {
            int buffer = dsa.createBuffer();
            dsa.bufferStorage(buffer, size, usage);
            return new GlBuffer.Direct(heuristics, dsa, usage, size, buffer, true);
        }

        @Override
        public GlBuffer createBuffer(GlHeuristics heuristics, DirectStateAccess dsa, @GpuBuffer.Usage int usage, ByteBuffer data) {
            int buffer = dsa.createBuffer();
            int size = data.remaining();
            dsa.bufferStorage(buffer, data, usage);
            return new GlBuffer.Direct(heuristics, dsa, usage, size, buffer, true);
        }
    }

    private static class Mutable
    extends BufferStorage {
        private Mutable() {
        }

        @Override
        public GlBuffer createBuffer(GlHeuristics heuristics, DirectStateAccess dsa, @GpuBuffer.Usage int usage, long size) {
            int buffer = dsa.createBuffer();
            dsa.bufferData(buffer, size, usage);
            return new GlBuffer.Direct(heuristics, dsa, usage, size, buffer, false);
        }

        @Override
        public GlBuffer createBuffer(GlHeuristics heuristics, DirectStateAccess dsa, @GpuBuffer.Usage int usage, ByteBuffer data) {
            int buffer = dsa.createBuffer();
            int size = data.remaining();
            dsa.bufferData(buffer, data, usage);
            return new GlBuffer.Direct(heuristics, dsa, usage, size, buffer, false);
        }
    }
}

