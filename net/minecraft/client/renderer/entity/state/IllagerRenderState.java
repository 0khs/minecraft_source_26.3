/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.UndeadRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.illager.AbstractIllager;

public class IllagerRenderState
extends UndeadRenderState {
    public boolean isRiding;
    public boolean isAggressive;
    public HumanoidArm mainArm = HumanoidArm.RIGHT;
    public AbstractIllager.IllagerArmPose armPose = AbstractIllager.IllagerArmPose.NEUTRAL;
    public int maxCrossbowChargeDuration;
    public float ticksUsingItem;
}

