/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FallingParticlesLeavesBlock
extends LeavesBlock {
    protected final float leafParticleChance;

    public FallingParticlesLeavesBlock(float leafParticleChance, AmbientLeavesBlockSoundPlayer ambientLeavesBlockSoundPlayer, BlockBehaviour.Properties properties) {
        super(ambientLeavesBlockSoundPlayer, properties);
        this.leafParticleChance = leafParticleChance;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        this.makeFallingLeavesParticles(level, pos, random);
    }

    private void makeFallingLeavesParticles(Level level, BlockPos pos, RandomSource random) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (random.nextFloat() >= this.leafParticleChance) {
            return;
        }
        if (FallingParticlesLeavesBlock.isFaceFull(belowState.getCollisionShape(level, below), Direction.UP)) {
            return;
        }
        this.spawnFallingLeavesParticle(level, pos, random);
    }

    protected abstract void spawnFallingLeavesParticle(Level var1, BlockPos var2, RandomSource var3);
}

