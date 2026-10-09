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
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class PoplarFoliagePlacer
extends FoliagePlacer {
    public static final MapCodec<PoplarFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(i -> PoplarFoliagePlacer.foliagePlacerParts(i).and(i.group((App)IntProviders.codec(5, 16).fieldOf("height").forGetter(p -> p.height), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("side_hole_chance").forGetter(p -> Float.valueOf(p.sideHoleChance)))).apply((Applicative)i, PoplarFoliagePlacer::new));
    private final IntProvider height;
    private final float sideHoleChance;

    public PoplarFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height, float sideHoleChance) {
        super(radius, offset);
        this.height = height;
        this.sideHoleChance = sideHoleChance;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.POPLAR_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(WorldGenLevel level, FoliagePlacer.FoliageSetter foliageSetter, RandomSource random, TreeFeature tree, int treeHeight, FoliagePlacer.FoliageAttachment foliageAttachment, int foliageHeight, int leafRadius, int offset) {
        boolean doubleTrunk = foliageAttachment.doubleTrunk();
        BlockPos foliagePos = foliageAttachment.pos().above(offset);
        int currentRadius = leafRadius + foliageAttachment.radiusOffsetXZ() - 1;
        boolean flipRhombusShape = random.nextBoolean();
        int foliageHeightWithOffset = foliageHeight + foliageAttachment.foliageHeightOffset();
        this.placeLeavesRow(level, foliageSetter, random, tree, foliagePos, currentRadius - 2, foliageHeightWithOffset - 1, doubleTrunk, foliageHeightWithOffset, flipRhombusShape);
        this.placeLeavesRow(level, foliageSetter, random, tree, foliagePos, currentRadius - 1, foliageHeightWithOffset - 2, doubleTrunk, foliageHeightWithOffset, flipRhombusShape);
        this.placeLeavesRow(level, foliageSetter, random, tree, foliagePos, currentRadius - 1, foliageHeightWithOffset - 3, doubleTrunk, foliageHeightWithOffset, flipRhombusShape);
        for (int y = foliageHeightWithOffset - 4; y >= 1; --y) {
            this.placeLeavesRow(level, foliageSetter, random, tree, foliagePos, currentRadius, y, doubleTrunk, foliageHeightWithOffset, flipRhombusShape);
        }
        this.replaceLeavesWithLog(level, foliageSetter, tree, random, foliagePos, currentRadius, foliageHeightWithOffset - 4, doubleTrunk, foliageHeightWithOffset, flipRhombusShape);
        this.placeLeavesRow(level, foliageSetter, random, tree, foliagePos, currentRadius - 1, 0, doubleTrunk, foliageHeightWithOffset, flipRhombusShape);
        this.placeLeavesRow(level, foliageSetter, random, tree, foliagePos, Mth.clamp(currentRadius - 2, 1, 2), -1, doubleTrunk, foliageHeightWithOffset, flipRhombusShape);
    }

    private void replaceLeavesWithLog(WorldGenLevel level, FoliagePlacer.FoliageSetter foliageSetter, TreeFeature tree, RandomSource random, BlockPos origin, int currentRadius, int y, boolean doubleTrunk, int foliageHeight, boolean flipRhombusShape) {
        int offset = doubleTrunk ? 1 : 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -currentRadius; dx <= currentRadius + offset; ++dx) {
            for (int dz = -currentRadius; dz <= currentRadius + offset; ++dz) {
                int absDz = Mth.abs(dz);
                int absDx = Mth.abs(dx);
                if (!PoplarFoliagePlacer.isWithinRhombusShape(currentRadius, absDx, absDz, this.getCornerBlocksToCutForRhombusShape(dx, dz, currentRadius, this.shouldRowBePartialRhombusShape(foliageHeight, y), flipRhombusShape), 2) || (absDz != 0 || currentRadius - absDx < 4) && (absDx != 0 || currentRadius - absDz < 4)) continue;
                pos.setWithOffset(origin, dx, y, dz);
                PoplarFoliagePlacer.tryPlaceLog(level, foliageSetter, random, tree, pos, PoplarFoliagePlacer.getSidewaysStateModifier(Direction.fromAxisAndDirection(absDz == 0 ? Direction.Axis.X : Direction.Axis.Z, Direction.AxisDirection.POSITIVE)));
            }
        }
    }

    private static void tryPlaceLog(WorldGenLevel level, FoliagePlacer.FoliageSetter foliageSetter, RandomSource random, TreeFeature tree, BlockPos pos, Function<BlockState, BlockState> stateModifier) {
        if (level.isStateAtPosition(pos, state -> state.equals(tree.foliageProvider().value().getState(level, random, pos)))) {
            foliageSetter.set(pos, stateModifier.apply(tree.trunkProvider().value().getState(level, random, pos)));
        }
    }

    private static Function<BlockState, BlockState> getSidewaysStateModifier(Direction branchDirection) {
        return state -> (BlockState)state.trySetValue(RotatedPillarBlock.AXIS, branchDirection.getAxis());
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeFeature tree) {
        return this.height.sample(random);
    }

    private void placeLeavesRow(WorldGenLevel level, FoliagePlacer.FoliageSetter foliageSetter, RandomSource random, TreeFeature tree, BlockPos origin, int currentRadius, int y, boolean doubleTrunk, int foliageHeight, boolean flipRhombusShape) {
        int offset = doubleTrunk ? 1 : 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -currentRadius; dx <= currentRadius + offset; ++dx) {
            for (int dz = -currentRadius; dz <= currentRadius + offset; ++dz) {
                if (this.shouldSkipLocation(random, dx, y, dz, currentRadius, doubleTrunk, foliageHeight, flipRhombusShape)) continue;
                pos.setWithOffset(origin, dx, y, dz);
                PoplarFoliagePlacer.tryPlaceLeaf(level, foliageSetter, random, tree, pos);
            }
        }
    }

    private boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk, int foliageHeight, boolean flipRhombusShape) {
        boolean isRhombusEdgeBlock;
        boolean shouldRowBePartialRhombusShape = this.shouldRowBePartialRhombusShape(foliageHeight, y);
        int cornerBlocksToCutForRhombusShape = this.getCornerBlocksToCutForRhombusShape(dx, dz, currentRadius, shouldRowBePartialRhombusShape, flipRhombusShape);
        int absDx = Mth.abs(dx);
        int absDz = Mth.abs(dz);
        boolean bl = isRhombusEdgeBlock = absDx == currentRadius || absDz == currentRadius;
        if (shouldRowBePartialRhombusShape && isRhombusEdgeBlock) {
            return true;
        }
        int additionalSideRemoval = random.nextFloat() <= this.sideHoleChance ? 1 : 0;
        return !PoplarFoliagePlacer.isWithinRhombusShape(currentRadius, absDx, absDz, cornerBlocksToCutForRhombusShape, additionalSideRemoval);
    }

    @Override
    protected boolean shouldSkipLocationSigned(RandomSource random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk) {
        throw new IllegalStateException("Overridden method needs more context");
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk) {
        throw new IllegalStateException("Overridden method needs more context");
    }

    private int getCornerBlocksToCutForRhombusShape(int dx, int dz, int currentRadius, boolean shouldRowBePartialRhombusShape, boolean flipRhombusShape) {
        boolean isSmallCornerOfShape;
        boolean bl = isSmallCornerOfShape = flipRhombusShape ? PoplarFoliagePlacer.isLeftTopCornerOrRightLowerCorner(dx, dz) : PoplarFoliagePlacer.isLeftLowerCornerOrRightTopCorner(dx, dz);
        return isSmallCornerOfShape ? currentRadius - 1 : (shouldRowBePartialRhombusShape ? currentRadius + 1 : currentRadius);
    }

    private static boolean isWithinRhombusShape(int currentRadius, int absDx, int absDz, int cornerBlocksToCutForRhombusShape, int additionalSideRemoval) {
        return absDx + absDz <= currentRadius * 2 - (cornerBlocksToCutForRhombusShape + additionalSideRemoval);
    }

    private static boolean isLeftLowerCornerOrRightTopCorner(int dx, int dz) {
        return dx > 0 && dz < 0 || dz > 0 && dx < 0;
    }

    private static boolean isLeftTopCornerOrRightLowerCorner(int dx, int dz) {
        return dx > 0 && dz > 0 || dz < 0 && dx < 0;
    }

    private boolean shouldRowBePartialRhombusShape(int foliageHeight, int y) {
        return foliageHeight - 1 == y || foliageHeight - 2 == y;
    }
}

