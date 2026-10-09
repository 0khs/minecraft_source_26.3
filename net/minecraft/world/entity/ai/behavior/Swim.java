/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.material.Fluid;

public class Swim<T extends Mob>
extends Behavior<T> {
    private final float chance;
    private final TagKey<Fluid> fluid;

    public Swim(float chance) {
        this(chance, FluidTags.ENTITY_FLOATABLE);
    }

    public Swim(float chance, TagKey<Fluid> fluid) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of());
        this.chance = chance;
        this.fluid = fluid;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob body) {
        return body.isInFluidDeeperThan(body.getFluidJumpThreshold(), this.fluid) || body.isInLava();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob body, long timestamp) {
        return this.checkExtraStartConditions(level, body);
    }

    @Override
    protected void tick(ServerLevel level, Mob body, long timestamp) {
        if (body.getRandom().nextFloat() < this.chance) {
            body.getJumpControl().jump();
        }
    }
}

