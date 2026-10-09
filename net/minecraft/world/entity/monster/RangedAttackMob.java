/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface RangedAttackMob {
    public void performRangedAttack(LivingEntity var1, float var2);

    default public float rangedAttackUncertainty(Level level) {
        return 14 - level.getDifficulty().getId() * 4;
    }
}

