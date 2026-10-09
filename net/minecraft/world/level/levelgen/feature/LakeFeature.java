/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

@Deprecated
public record LakeFeature(Holder<BlockStateProvider> fluid, Holder<BlockStateProvider> barrier, BlockPredicate canPlaceFeature, BlockPredicate canReplaceWithAirOrFluid, BlockPredicate canReplaceWithBarrier) implements Feature
{
    public static final MapCodec<LakeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("fluid").forGetter(LakeFeature::fluid), (App)BlockStateProvider.CODEC.fieldOf("barrier").forGetter(LakeFeature::barrier), (App)BlockPredicate.CODEC.fieldOf("can_place_feature").forGetter(LakeFeature::canPlaceFeature), (App)BlockPredicate.CODEC.fieldOf("can_replace_with_air_or_fluid").forGetter(LakeFeature::canReplaceWithAirOrFluid), (App)BlockPredicate.CODEC.fieldOf("can_replace_with_barrier").forGetter(LakeFeature::canReplaceWithBarrier)).apply((Applicative)i, LakeFeature::new));
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public MapCodec<LakeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int xx;
        int zz;
        int xx2;
        if (origin.getY() <= level.getMinY() + 4) {
            return false;
        }
        origin = origin.offset(-8, -4, -8);
        boolean[] grid = new boolean[2048];
        int spots = random.nextInt(4) + 4;
        for (int i = 0; i < spots; ++i) {
            double xr = random.nextDouble() * 6.0 + 3.0;
            double yr = random.nextDouble() * 4.0 + 2.0;
            double zr = random.nextDouble() * 6.0 + 3.0;
            double xp = random.nextDouble() * (16.0 - xr - 2.0) + 1.0 + xr / 2.0;
            double yp = random.nextDouble() * (8.0 - yr - 4.0) + 2.0 + yr / 2.0;
            double zp = random.nextDouble() * (16.0 - zr - 2.0) + 1.0 + zr / 2.0;
            for (int xx3 = 1; xx3 < 15; ++xx3) {
                for (int zz2 = 1; zz2 < 15; ++zz2) {
                    for (int yy = 1; yy < 7; ++yy) {
                        double xd = ((double)xx3 - xp) / (xr / 2.0);
                        double yd = ((double)yy - yp) / (yr / 2.0);
                        double zd = ((double)zz2 - zp) / (zr / 2.0);
                        double d = xd * xd + yd * yd + zd * zd;
                        if (!(d < 1.0)) continue;
                        grid[(xx3 * 16 + zz2) * 8 + yy] = true;
                    }
                }
            }
        }
        BlockState fluid = this.fluid.value().getState(level, random, origin);
        for (xx2 = 0; xx2 < 16; ++xx2) {
            for (zz = 0; zz < 16; ++zz) {
                for (int yy = 0; yy < 8; ++yy) {
                    boolean check;
                    boolean bl = check = !grid[(xx2 * 16 + zz) * 8 + yy] && (xx2 < 15 && grid[((xx2 + 1) * 16 + zz) * 8 + yy] || xx2 > 0 && grid[((xx2 - 1) * 16 + zz) * 8 + yy] || zz < 15 && grid[(xx2 * 16 + zz + 1) * 8 + yy] || zz > 0 && grid[(xx2 * 16 + (zz - 1)) * 8 + yy] || yy < 7 && grid[(xx2 * 16 + zz) * 8 + yy + 1] || yy > 0 && grid[(xx2 * 16 + zz) * 8 + (yy - 1)]);
                    if (!check) continue;
                    BlockPos offsetPos = origin.offset(xx2, yy, zz);
                    BlockState blockState = level.getBlockState(offsetPos);
                    if (yy >= 4 && blockState.liquid()) {
                        return false;
                    }
                    if (yy < 4 && !blockState.isSolid() && blockState != fluid) {
                        return false;
                    }
                    if (this.canPlaceFeature.test(level, offsetPos)) continue;
                    return false;
                }
            }
        }
        for (xx2 = 0; xx2 < 16; ++xx2) {
            for (zz = 0; zz < 16; ++zz) {
                for (int yy = 0; yy < 8; ++yy) {
                    BlockPos placePos;
                    if (!grid[(xx2 * 16 + zz) * 8 + yy] || !this.canReplaceWithAirOrFluid.test(level, placePos = origin.offset(xx2, yy, zz))) continue;
                    boolean placeAir = yy >= 4;
                    level.setBlock(placePos, placeAir ? AIR : fluid, 2);
                    if (!placeAir) continue;
                    level.scheduleTick(placePos, AIR.getBlock(), 0);
                    this.markAboveForPostProcessing(level, placePos);
                }
            }
        }
        BlockState barrier = this.barrier.value().getState(level, random, origin);
        if (!barrier.isAir()) {
            for (xx = 0; xx < 16; ++xx) {
                for (int zz3 = 0; zz3 < 16; ++zz3) {
                    for (int yy = 0; yy < 8; ++yy) {
                        BlockPos offset;
                        BlockState blockState;
                        boolean check;
                        boolean bl = check = !grid[(xx * 16 + zz3) * 8 + yy] && (xx < 15 && grid[((xx + 1) * 16 + zz3) * 8 + yy] || xx > 0 && grid[((xx - 1) * 16 + zz3) * 8 + yy] || zz3 < 15 && grid[(xx * 16 + zz3 + 1) * 8 + yy] || zz3 > 0 && grid[(xx * 16 + (zz3 - 1)) * 8 + yy] || yy < 7 && grid[(xx * 16 + zz3) * 8 + yy + 1] || yy > 0 && grid[(xx * 16 + zz3) * 8 + (yy - 1)]);
                        if (!check || yy >= 4 && random.nextInt(2) == 0 || !(blockState = level.getBlockState(offset = origin.offset(xx, yy, zz3))).isSolid() || !this.canReplaceWithBarrier.test(level, offset)) continue;
                        BlockPos barrierPos = origin.offset(xx, yy, zz3);
                        level.setBlock(barrierPos, barrier, 2);
                        this.markAboveForPostProcessing(level, barrierPos);
                    }
                }
            }
        }
        if (fluid.getFluidState().is(FluidTags.WATER)) {
            for (xx = 0; xx < 16; ++xx) {
                for (int zz4 = 0; zz4 < 16; ++zz4) {
                    int yy = 4;
                    BlockPos offset = origin.offset(xx, 4, zz4);
                    if (!level.getBiome(offset).value().shouldFreeze(level, offset, false) || !this.canReplaceWithAirOrFluid.test(level, offset)) continue;
                    level.setBlock(offset, Blocks.ICE.defaultBlockState(), 2);
                }
            }
        }
        return true;
    }
}

