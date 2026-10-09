/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.animal.parrot.Parrot;

public class ParrotRenderState
extends LivingEntityRenderState {
    public Parrot.Variant variant = Parrot.Variant.RED_BLUE;
    public float flapAngle;
    public ParrotModel.Pose pose = ParrotModel.Pose.FLYING;
}

