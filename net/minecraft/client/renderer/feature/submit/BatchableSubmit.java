/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.feature.submit;

import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

public interface BatchableSubmit
extends SubmitNode {
    public Object batchKey();

    public FeatureRendererType<? extends BatchableSubmit> featureType();
}

