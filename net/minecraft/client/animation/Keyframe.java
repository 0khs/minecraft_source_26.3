/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3fc
 */
package net.minecraft.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import org.joml.Vector3fc;

public record Keyframe(float timestamp, Vector3fc preTarget, Vector3fc postTarget, AnimationChannel.Interpolation interpolation) {
    public Keyframe(float timestamp, Vector3fc postTarget, AnimationChannel.Interpolation interpolation) {
        this(timestamp, postTarget, postTarget, interpolation);
    }
}

