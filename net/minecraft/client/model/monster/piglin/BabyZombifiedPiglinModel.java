/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.monster.piglin;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.monster.piglin.BabyPiglinModel;
import net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel;

public class BabyZombifiedPiglinModel
extends ZombifiedPiglinModel {
    public BabyZombifiedPiglinModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return BabyPiglinModel.createBodyLayer();
    }

    @Override
    protected float getDefaultEarAngleInDegrees() {
        return 5.0f;
    }
}

