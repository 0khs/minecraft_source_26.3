/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.monster.piglin;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.piglin.AbstractPiglinModel;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;

public abstract class ZombifiedPiglinModel
extends AbstractPiglinModel<ZombifiedPiglinRenderState> {
    public ZombifiedPiglinModel(ModelPart root) {
        super(root);
    }

    @Override
    protected void setupAttackAnimation(ZombifiedPiglinRenderState state) {
        super.setupAttackAnimation(state);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, state.isAggressive, state);
    }
}

