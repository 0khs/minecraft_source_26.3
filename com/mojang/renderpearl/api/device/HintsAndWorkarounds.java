/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.device;

public record HintsAndWorkarounds(boolean writeToBufferIsSlow, boolean anisotropyHasKnownIssues, boolean isExplicitDepthRequired, boolean multiDrawIndirectHasKnownIssues) {
}

