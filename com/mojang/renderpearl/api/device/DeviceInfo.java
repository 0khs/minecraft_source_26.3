/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.device;

import com.mojang.renderpearl.api.device.DeviceFeatures;
import com.mojang.renderpearl.api.device.DeviceLimits;
import com.mojang.renderpearl.api.device.DeviceType;
import com.mojang.renderpearl.api.device.HintsAndWorkarounds;
import java.util.Set;

public record DeviceInfo(String name, String vendorName, String driverInfo, boolean isZZeroToOne, String backendName, float timestampPeriod, DeviceLimits limits, DeviceFeatures features, Set<String> underlyingExtensions, HintsAndWorkarounds hintsAndWorkarounds, DeviceType type) {
}

