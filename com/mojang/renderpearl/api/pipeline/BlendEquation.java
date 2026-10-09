/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.renderpearl.api.pipeline;

import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendOp;

public record BlendEquation(BlendFactor sourceFactor, BlendFactor destFactor, BlendOp op) {
}

