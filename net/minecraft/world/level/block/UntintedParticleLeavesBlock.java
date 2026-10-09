/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingParticlesLeavesBlock;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class UntintedParticleLeavesBlock
extends FallingParticlesLeavesBlock {
    protected final ParticleOptions leafParticle;

    public UntintedParticleLeavesBlock(float leafParticleChance, ParticleOptions leafParticle, AmbientLeavesBlockSoundPlayer ambientLeavesBlockSoundPlayer, BlockBehaviour.Properties properties) {
        super(leafParticleChance, ambientLeavesBlockSoundPlayer, properties);
        this.leafParticle = leafParticle;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        ParticleUtils.spawnParticleBelow(level, pos, random, this.leafParticle);
    }
}

