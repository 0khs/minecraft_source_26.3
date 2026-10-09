/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class AcaciaFoliagePlacer
extends FoliagePlacer {
    public static final MapCodec<AcaciaFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(i -> AcaciaFoliagePlacer.foliagePlacerParts(i).apply((Applicative)i, AcaciaFoliagePlacer::new));

    public AcaciaFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.ACACIA_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(WorldGenLevel level, FoliagePlacer.FoliageSetter foliageSetter, RandomSource random, TreeFeature tree, int treeHeight, FoliagePlacer.FoliageAttachment foliageAttachment, int foliageHeight, int leafRadius, int offset) {
        boolean doubleTrunk = foliageAttachment.doubleTrunk();
        BlockPos foliagePos = foliageAttachment.pos().above(offset);
        int foliageHeightWithOffset = foliageHeight + foliageAttachment.foliageHeightOffset();
        this.placeLeavesRow(level, foliageSetter, random, tree, foliagePos, leafRadius + foliageAttachment.radiusOffsetXZ(), -1 - foliageHeightWithOffset, doubleTrunk);
        this.placeLeavesRow(level, foliageSetter, random, tree, foliagePos, leafRadius - 1, -foliageHeightWithOffset, doubleTrunk);
        this.placeLeavesRow(level, foliageSetter, random, tree, foliagePos, leafRadius + foliageAttachment.radiusOffsetXZ() - 1, 0, doubleTrunk);
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeFeature tree) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk) {
        if (y == 0) {
            return (dx > 1 || dz > 1) && dx != 0 && dz != 0;
        }
        return dx == currentRadius && dz == currentRadius && currentRadius > 0;
    }
}

