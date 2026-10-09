/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.object.cushion;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.CushionRenderState;

public class CushionModel
extends EntityModel<CushionRenderState> {
    public CushionModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();
        root.addOrReplaceChild("cushion", CubeListBuilder.create().texOffs(0, 0).addBox(-31.0f, -4.0f, -1.0f, 16.0f, 4.0f, 16.0f, new CubeDeformation(-0.005f)), PartPose.offset(23.0f, 4.0f, -7.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}

