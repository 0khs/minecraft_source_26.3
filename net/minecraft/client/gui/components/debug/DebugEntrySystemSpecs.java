/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  oshi.SystemInfo
 *  oshi.hardware.CentralProcessor
 */
package net.minecraft.client.gui.components.debug;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.DeviceType;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class DebugEntrySystemSpecs
implements DebugScreenEntry {
    private static final Identifier GROUP = Identifier.withDefaultNamespace("system");
    private static @Nullable String cpuInfo;

    public static String getCpuInfo() {
        if (cpuInfo == null) {
            cpuInfo = "<unknown>";
            try {
                CentralProcessor processor = new SystemInfo().getHardware().getProcessor();
                cpuInfo = String.format(Locale.ROOT, "%dx %s", processor.getLogicalProcessorCount(), processor.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return cpuInfo;
    }

    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        DeviceInfo deviceInfo = RenderSystem.getDevice().getDeviceInfo();
        Window window = Minecraft.getInstance().getWindow();
        displayer.addToGroup(GROUP, List.of(String.format(Locale.ROOT, "Java: %s", System.getProperty("java.version")), String.format(Locale.ROOT, "CPU: %s", DebugEntrySystemSpecs.getCpuInfo()), String.format(Locale.ROOT, "Display: %dx%d (%s)", window.getWidth(), window.getHeight(), deviceInfo.vendorName()), String.format(Locale.ROOT, "Window: %dx%d (%.2fx pixel density)", window.getScreenWidth(), window.getScreenHeight(), Float.valueOf(window.getPixelDensity())), String.format(Locale.ROOT, "%s%s", deviceInfo.name(), this.typeName(deviceInfo.type())), String.format(Locale.ROOT, "%s %s", deviceInfo.backendName(), this.firstLine(deviceInfo.driverInfo()))));
    }

    private String firstLine(String value) {
        return value.lines().findFirst().orElse(value);
    }

    private String typeName(DeviceType type) {
        return switch (type) {
            default -> throw new MatchException(null, null);
            case DeviceType.OTHER -> "";
            case DeviceType.INTEGRATED -> " (iGPU)";
            case DeviceType.DISCRETE -> " (dGPU)";
            case DeviceType.VIRTUAL -> " (vGPU)";
            case DeviceType.CPU -> " (software)";
        };
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }
}

