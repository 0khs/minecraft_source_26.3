/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.animal.feline;

import net.minecraft.client.model.animal.feline.BabyFelineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.entity.state.CatRenderState;

public class BabyCatModel
extends BabyFelineModel<CatRenderState> {
    public static final MeshTransformer COLLAR_TRANSFORMER = MeshTransformer.scaling(1.01f);

    public BabyCatModel(ModelPart root) {
        super(root);
    }
}

