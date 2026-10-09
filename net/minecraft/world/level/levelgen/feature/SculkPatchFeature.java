/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record SculkPatchFeature(int chargeCount, int amountPerCharge, int spreadAttempts, int growthRounds, int spreadRounds) implements Feature
{
    public static final MapCodec<SculkPatchFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.intRange((int)1, (int)32).fieldOf("charge_count").forGetter(SculkPatchFeature::chargeCount), (App)Codec.intRange((int)1, (int)500).fieldOf("amount_per_charge").forGetter(SculkPatchFeature::amountPerCharge), (App)Codec.intRange((int)1, (int)64).fieldOf("spread_attempts").forGetter(SculkPatchFeature::spreadAttempts), (App)Codec.intRange((int)0, (int)8).fieldOf("growth_rounds").forGetter(SculkPatchFeature::growthRounds), (App)Codec.intRange((int)0, (int)8).fieldOf("spread_rounds").forGetter(SculkPatchFeature::spreadRounds)).apply((Applicative)i, SculkPatchFeature::new));

    public MapCodec<SculkPatchFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!this.canSpreadFrom(level, origin)) {
            return false;
        }
        SculkSpreader spreader = SculkSpreader.createWorldGenSpreader();
        int totalRounds = this.spreadRounds + this.growthRounds;
        for (int round = 0; round < totalRounds; ++round) {
            for (int i = 0; i < this.chargeCount; ++i) {
                spreader.addCursors(origin, this.amountPerCharge);
            }
            boolean spreadVeins = round < this.spreadRounds;
            for (int i = 0; i < this.spreadAttempts; ++i) {
                spreader.updateCursors(level, origin, random, spreadVeins);
            }
            spreader.clear();
        }
        return true;
    }

    private boolean canSpreadFrom(LevelAccessor level, BlockPos origin) {
        block5: {
            block4: {
                BlockState start = level.getBlockState(origin);
                if (start.getBlock() instanceof SculkBehaviour) {
                    return true;
                }
                if (start.isAir()) break block4;
                if (!start.is(Blocks.WATER) || !start.getFluidState().isSource()) break block5;
            }
            return Direction.stream().map(origin::relative).anyMatch(pos -> level.getBlockState((BlockPos)pos).isCollisionShapeFullBlock(level, (BlockPos)pos));
        }
        return false;
    }
}

