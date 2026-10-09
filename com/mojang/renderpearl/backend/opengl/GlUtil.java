/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.renderpearl.api.buffers.GpuBuffer;

public class GlUtil {
    public static int selectBufferBindTarget(@GpuBuffer.Usage int usage) {
        if ((usage & 0x20) != 0) {
            return 34962;
        }
        if ((usage & 0x40) != 0) {
            return 34963;
        }
        if ((usage & 0x80) != 0) {
            return 35345;
        }
        if ((usage & 0x200) != 0) {
            return 36671;
        }
        return 36663;
    }
}

