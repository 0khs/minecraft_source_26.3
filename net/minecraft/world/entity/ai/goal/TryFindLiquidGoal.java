/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.material.Fluid;

public class TryFindLiquidGoal
extends Goal {
    private final PathfinderMob mob;
    private final TagKey<Fluid> fluidTag;

    public TryFindLiquidGoal(PathfinderMob mob, TagKey<Fluid> fluidTag) {
        this.mob = mob;
        this.fluidTag = fluidTag;
    }

    @Override
    public boolean canUse() {
        return this.mob.onGround() && !this.mob.level().getFluidState(this.mob.blockPosition()).is(this.fluidTag);
    }

    @Override
    public void start() {
        Vec3i fluidPos = null;
        Iterable<BlockPos> between = BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 2.0), Mth.floor(this.mob.getY() - 2.0), Mth.floor(this.mob.getZ() - 2.0), Mth.floor(this.mob.getX() + 2.0), this.mob.getBlockY(), Mth.floor(this.mob.getZ() + 2.0));
        for (BlockPos pos : between) {
            if (!this.mob.level().getFluidState(pos).is(this.fluidTag)) continue;
            fluidPos = pos;
            break;
        }
        if (fluidPos != null) {
            this.mob.getMoveControl().setWantedPosition(fluidPos.getX(), fluidPos.getY(), fluidPos.getZ(), 1.0);
        }
    }
}

