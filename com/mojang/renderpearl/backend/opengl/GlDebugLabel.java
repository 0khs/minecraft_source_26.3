/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.EXTDebugLabel
 *  org.lwjgl.opengl.GL33C
 *  org.lwjgl.opengl.GLCapabilities
 *  org.lwjgl.opengl.KHRDebug
 *  org.slf4j.Logger
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlShaderModule;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.util.StringUtil;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.EXTDebugLabel;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.KHRDebug;
import org.slf4j.Logger;

public abstract class GlDebugLabel {
    private static final Logger LOGGER = LogUtils.getLogger();

    public void applyLabel(GlBuffer buffer, @Nullable Supplier<String> label) {
    }

    public void applyLabel(GlTexture texture) {
    }

    public void applyLabel(GlShaderModule shaderModule) {
    }

    public void applyLabel(GlProgram program) {
    }

    public void pushDebugGroup(Supplier<String> label) {
    }

    public void popDebugGroup() {
    }

    public static GlDebugLabel create(GLCapabilities caps, boolean wantsLabels, Set<String> enabledExtensions) {
        if (wantsLabels) {
            if (caps.GL_KHR_debug && GlDevice.USE_GL_KHR_debug) {
                enabledExtensions.add("GL_KHR_debug");
                return new Core();
            }
            if (caps.GL_EXT_debug_label && GlDevice.USE_GL_EXT_debug_label) {
                enabledExtensions.add("GL_EXT_debug_label");
                return new Ext();
            }
            LOGGER.warn("Debug labels unavailable: neither KHR_debug nor EXT_debug_label are supported");
        }
        return new Empty();
    }

    public boolean exists() {
        return false;
    }

    private static class Core
    extends GlDebugLabel {
        private final int maxLabelLength = GL33C.glGetInteger((int)33512);

        private Core() {
        }

        @Override
        public void applyLabel(GlBuffer buffer, @Nullable Supplier<String> label) {
            if (label != null) {
                KHRDebug.glObjectLabel((int)33504, (int)buffer.handle(), (CharSequence)StringUtil.truncateStringIfNecessary(label.get(), this.maxLabelLength, true));
            }
        }

        @Override
        public void applyLabel(GlTexture texture) {
            KHRDebug.glObjectLabel((int)5890, (int)texture.id, (CharSequence)StringUtil.truncateStringIfNecessary(texture.getLabel(), this.maxLabelLength, true));
        }

        @Override
        public void applyLabel(GlShaderModule shaderModule) {
            KHRDebug.glObjectLabel((int)33505, (int)shaderModule.getShaderId(), (CharSequence)StringUtil.truncateStringIfNecessary(shaderModule.getLabel(), this.maxLabelLength, true));
        }

        @Override
        public void applyLabel(GlProgram program) {
            KHRDebug.glObjectLabel((int)33506, (int)program.getProgramId(), (CharSequence)StringUtil.truncateStringIfNecessary(program.getDebugLabel(), this.maxLabelLength, true));
        }

        @Override
        public void pushDebugGroup(Supplier<String> label) {
            KHRDebug.glPushDebugGroup((int)33354, (int)0, (CharSequence)label.get());
        }

        @Override
        public void popDebugGroup() {
            KHRDebug.glPopDebugGroup();
        }

        @Override
        public boolean exists() {
            return true;
        }
    }

    private static class Ext
    extends GlDebugLabel {
        private Ext() {
        }

        @Override
        public void applyLabel(GlBuffer buffer, @Nullable Supplier<String> label) {
            if (label != null) {
                EXTDebugLabel.glLabelObjectEXT((int)37201, (int)buffer.handle(), (CharSequence)StringUtil.truncateStringIfNecessary(label.get(), 256, true));
            }
        }

        @Override
        public void applyLabel(GlTexture texture) {
            EXTDebugLabel.glLabelObjectEXT((int)5890, (int)texture.id, (CharSequence)StringUtil.truncateStringIfNecessary(texture.getLabel(), 256, true));
        }

        @Override
        public void applyLabel(GlShaderModule shaderModule) {
            EXTDebugLabel.glLabelObjectEXT((int)35656, (int)shaderModule.getShaderId(), (CharSequence)StringUtil.truncateStringIfNecessary(shaderModule.getLabel(), 256, true));
        }

        @Override
        public void applyLabel(GlProgram program) {
            EXTDebugLabel.glLabelObjectEXT((int)35648, (int)program.getProgramId(), (CharSequence)StringUtil.truncateStringIfNecessary(program.getDebugLabel(), 256, true));
        }

        @Override
        public boolean exists() {
            return true;
        }
    }

    private static class Empty
    extends GlDebugLabel {
        private Empty() {
        }
    }
}

