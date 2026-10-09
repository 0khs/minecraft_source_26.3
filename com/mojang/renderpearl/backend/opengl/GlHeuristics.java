/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.lwjgl.opengl.GL33C
 *  org.lwjgl.opengl.GLCapabilities
 *  org.slf4j.Logger
 */
package com.mojang.renderpearl.backend.opengl;

import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.device.DeviceFeatures;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.DeviceLimits;
import com.mojang.renderpearl.api.device.DeviceType;
import com.mojang.renderpearl.api.device.HintsAndWorkarounds;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;

public class GlHeuristics {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<String> DEVICE_NAMES_THAT_IMPLY_CPU = List.of("mesa offscreen", "llvmpipe");
    private static final List<String> DEVICE_NAMES_THAT_IMPLY_VIRTUAL = List.of("virtgl");
    private final boolean isGlOnDx12;
    private final boolean isAmd;
    private final boolean isNvidia;
    private final boolean couldBeIntelGen7;

    GlHeuristics(String deviceName, String vendor) {
        this.isGlOnDx12 = GlHeuristics.isGlOnDx12(deviceName);
        this.isAmd = GlHeuristics.isAmd(deviceName);
        this.isNvidia = GlHeuristics.isNvidia(deviceName);
        this.couldBeIntelGen7 = GlHeuristics.couldBeIntelGen7(deviceName.toLowerCase(Locale.ROOT), vendor.toLowerCase(Locale.ROOT));
    }

    public boolean isGlOnDx12() {
        return this.isGlOnDx12;
    }

    public boolean isAmd() {
        return this.isAmd;
    }

    public boolean isNvidia() {
        return this.isNvidia;
    }

    public boolean couldBeIntelGen7() {
        return this.couldBeIntelGen7;
    }

    private static boolean isGlOnDx12(String deviceName) {
        boolean isWindowsArm64 = Util.getPlatform() == Util.OS.WINDOWS && Util.isAarch64();
        return isWindowsArm64 || deviceName.startsWith("D3D12");
    }

    private static boolean isAmd(String deviceName) {
        return deviceName.contains("AMD");
    }

    private static boolean isNvidia(String deviceName) {
        return deviceName.toLowerCase(Locale.ROOT).contains("nvidia");
    }

    private static int getMaxSupportedTextureSize() {
        int maxReported = GlStateManager._getInteger(3379);
        for (int texSize = Math.max(32768, maxReported); texSize >= 1024; texSize >>= 1) {
            GlStateManager._texImage2D(32868, 0, 6408, texSize, texSize, 0, 6408, 5121, null);
            int width = GlStateManager._getTexLevelParameter(32868, 0, 4096);
            if (width == 0) continue;
            return texSize;
        }
        int maxSupportedTextureSize = Math.max(maxReported, 1024);
        LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", (Object)maxSupportedTextureSize);
        return maxSupportedTextureSize;
    }

    public DeviceInfo createDeviceInfo(GLCapabilities capabilities, int maxSupportedAnisotropy, Set<String> enabledExtensions) {
        String renderer = GlStateManager._getString(7937);
        String vendor = GlStateManager._getString(7936);
        String rendererLowerCase = renderer.toLowerCase(Locale.ROOT);
        String vendorLowerCase = vendor.toLowerCase(Locale.ROOT);
        int drawIndirectCount = enabledExtensions.contains("GL_ARB_multi_draw_indirect") ? Integer.MAX_VALUE : (enabledExtensions.contains("GL_ARB_draw_indirect") ? 1 : 0);
        return new DeviceInfo(renderer, vendor, GlStateManager._getString(7938), capabilities.GL_ARB_clip_control, "OpenGL", 1.0f, new DeviceLimits(maxSupportedAnisotropy, GL33C.glGetInteger((int)35380), GlHeuristics.getMaxSupportedTextureSize(), Long.MAX_VALUE, 0, GL33C.glGetInteger((int)34852), drawIndirectCount), new DeviceFeatures(true, enabledExtensions.contains("GL_ARB_shader_draw_parameters"), false, true, enabledExtensions.contains("GL_ARB_multi_draw_indirect"), enabledExtensions.contains("GL_ARB_draw_indirect"), enabledExtensions.contains("GL_ARB_base_instance"), enabledExtensions.contains("GL_ARB_buffer_storage")), Collections.unmodifiableSet(enabledExtensions), new HintsAndWorkarounds(this.isGlOnDx12(), this.isAmd(), Util.isAppleSiliconMac(renderer), vendorLowerCase.contains("intel") && !rendererLowerCase.contains("arc")), this.guessDeviceType(rendererLowerCase, vendorLowerCase));
    }

    private static boolean couldBeIntelGen7(String renderer, String vendor) {
        if (!vendor.contains("intel")) {
            return false;
        }
        if (renderer.contains("2500")) {
            return true;
        }
        if (renderer.contains("4000")) {
            return true;
        }
        if (renderer.contains("hd graphics (byt)")) {
            return true;
        }
        return renderer.endsWith("hd graphics");
    }

    private DeviceType guessDeviceType(String renderer, String vendor) {
        if (vendor.contains("intel")) {
            if (renderer.contains("arc")) {
                return DeviceType.DISCRETE;
            }
            return DeviceType.INTEGRATED;
        }
        for (String string : DEVICE_NAMES_THAT_IMPLY_CPU) {
            if (!renderer.contains(string)) continue;
            return DeviceType.CPU;
        }
        for (String string : DEVICE_NAMES_THAT_IMPLY_VIRTUAL) {
            if (!renderer.contains(string)) continue;
            return DeviceType.VIRTUAL;
        }
        return DeviceType.OTHER;
    }
}

