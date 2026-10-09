/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.geom.builders;

import net.minecraft.client.model.geom.builders.MeshDefinition;

@FunctionalInterface
public interface MeshTransformer {
    public static final MeshTransformer IDENTITY = mesh -> mesh;

    public static MeshTransformer scaling(float factor) {
        float yOffset = 24.016f * (1.0f - factor);
        return mesh -> mesh.transformed(pose -> pose.scaled(factor).translated(0.0f, yOffset, 0.0f));
    }

    public MeshDefinition apply(MeshDefinition var1);
}

