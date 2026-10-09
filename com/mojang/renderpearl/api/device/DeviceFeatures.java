/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.device;

public record DeviceFeatures(boolean wireframeFillMode, boolean shaderDrawParameters, boolean multiDrawDirectInterleaved, boolean multiDrawDirectSeparate, boolean multiDrawIndirect, boolean drawIndirect, boolean nonZeroFirstInstance, boolean persistentMapping) {
}

