/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.monster.slime;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.slime.SulfurCubeModel;

public class SmallSulfurCubeModel
extends SulfurCubeModel {
    public SmallSulfurCubeModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createOuterBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, -5.0f, -5.0f, 10.0f, 10.0f, 10.0f), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition sulfur_cube = root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}

