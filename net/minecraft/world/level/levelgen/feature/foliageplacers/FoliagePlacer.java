/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P2
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.material.Fluids;

public abstract class FoliagePlacer {
    public static final Codec<FoliagePlacer> CODEC = BuiltInRegistries.FOLIAGE_PLACER_TYPE.byNameCodec().dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
    protected final IntProvider radius;
    protected final IntProvider offset;

    protected static <P extends FoliagePlacer> Products.P2<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider> foliagePlacerParts(RecordCodecBuilder.Instance<P> instance) {
        return instance.group((App)IntProviders.codec(0, 16).fieldOf("radius").forGetter(p -> p.radius), (App)IntProviders.codec(0, 16).fieldOf("offset").forGetter(p -> p.offset));
    }

    public FoliagePlacer(IntProvider radius, IntProvider offset) {
        this.radius = radius;
        this.offset = offset;
    }

    protected abstract FoliagePlacerType<?> type();

    public void createFoliage(WorldGenLevel level, FoliageSetter foliageSetter, RandomSource random, TreeFeature tree, int treeHeight, FoliageAttachment foliageAttachment, int foliageHeight, int leafRadius) {
        this.createFoliage(level, foliageSetter, random, tree, treeHeight, foliageAttachment, foliageHeight, leafRadius, this.offset(random));
    }

    protected abstract void createFoliage(WorldGenLevel var1, FoliageSetter var2, RandomSource var3, TreeFeature var4, int var5, FoliageAttachment var6, int var7, int var8, int var9);

    public abstract int foliageHeight(RandomSource var1, int var2, TreeFeature var3);

    public int foliageRadius(RandomSource random, int trunkHeight) {
        return this.radius.sample(random);
    }

    private int offset(RandomSource random) {
        return this.offset.sample(random);
    }

    protected abstract boolean shouldSkipLocation(RandomSource var1, int var2, int var3, int var4, int var5, boolean var6);

    protected boolean shouldSkipLocationSigned(RandomSource random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk) {
        int minDz;
        int minDx;
        if (doubleTrunk) {
            minDx = Math.min(Math.abs(dx), Math.abs(dx - 1));
            minDz = Math.min(Math.abs(dz), Math.abs(dz - 1));
        } else {
            minDx = Math.abs(dx);
            minDz = Math.abs(dz);
        }
        return this.shouldSkipLocation(random, minDx, y, minDz, currentRadius, doubleTrunk);
    }

    protected void placeLeavesRow(WorldGenLevel level, FoliageSetter foliageSetter, RandomSource random, TreeFeature tree, BlockPos origin, int currentRadius, int y, boolean doubleTrunk) {
        int offset = doubleTrunk ? 1 : 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -currentRadius; dx <= currentRadius + offset; ++dx) {
            for (int dz = -currentRadius; dz <= currentRadius + offset; ++dz) {
                if (this.shouldSkipLocationSigned(random, dx, y, dz, currentRadius, doubleTrunk)) continue;
                pos.setWithOffset(origin, dx, y, dz);
                FoliagePlacer.tryPlaceLeaf(level, foliageSetter, random, tree, pos);
            }
        }
    }

    protected final void placeLeavesRowWithHangingLeavesBelow(WorldGenLevel level, FoliageSetter foliageSetter, RandomSource random, TreeFeature tree, BlockPos origin, int currentRadius, int y, boolean doubleTrunk, float hangingLeavesChance, float hangingLeavesExtensionChance) {
        this.placeLeavesRow(level, foliageSetter, random, tree, origin, currentRadius, y, doubleTrunk);
        int offset = doubleTrunk ? 1 : 0;
        BlockPos logPos = origin.below();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Direction alongEdge : Direction.Plane.HORIZONTAL) {
            Direction toEdge = alongEdge.getClockWise();
            int offsetToEdge = toEdge.getAxisDirection() == Direction.AxisDirection.POSITIVE ? currentRadius + offset : currentRadius;
            pos.setWithOffset(origin, 0, y - 1, 0).move(toEdge, offsetToEdge).move(alongEdge, -currentRadius);
            for (int offsetAlongEdge = -currentRadius; offsetAlongEdge < currentRadius + offset; ++offsetAlongEdge) {
                boolean leavesAbove = foliageSetter.isSet(pos.move(Direction.UP));
                pos.move(Direction.DOWN);
                if (leavesAbove && FoliagePlacer.tryPlaceExtension(level, foliageSetter, random, tree, hangingLeavesChance, logPos, pos)) {
                    pos.move(Direction.DOWN);
                    FoliagePlacer.tryPlaceExtension(level, foliageSetter, random, tree, hangingLeavesExtensionChance, logPos, pos);
                    pos.move(Direction.UP);
                }
                pos.move(alongEdge);
            }
        }
    }

    private static boolean tryPlaceExtension(WorldGenLevel level, FoliageSetter foliageSetter, RandomSource random, TreeFeature tree, float chance, BlockPos logPos, BlockPos.MutableBlockPos pos) {
        if (pos.distManhattan(logPos) >= 7) {
            return false;
        }
        if (random.nextFloat() > chance) {
            return false;
        }
        return FoliagePlacer.tryPlaceLeaf(level, foliageSetter, random, tree, pos);
    }

    protected static boolean tryPlaceLeaf(WorldGenLevel level, FoliageSetter foliageSetter, RandomSource random, TreeFeature tree, BlockPos pos) {
        boolean isPersistent = level.isStateAtPosition(pos, state -> state.getValueOrElse(BlockStateProperties.PERSISTENT, false));
        if (isPersistent || !TreeFeature.validTreePos(level, pos)) {
            return false;
        }
        BlockState foliageState = tree.foliageProvider().value().getState(level, random, pos);
        if (foliageState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            foliageState = (BlockState)foliageState.setValue(BlockStateProperties.WATERLOGGED, level.isFluidAtPosition(pos, fluidState -> fluidState.isSourceOfType(Fluids.WATER)));
        }
        foliageSetter.set(pos, foliageState);
        return true;
    }

    public static interface FoliageSetter {
        public void set(BlockPos var1, BlockState var2);

        public boolean isSet(BlockPos var1);
    }

    public static final class FoliageAttachment
    extends Record {
        private final BlockPos pos;
        private final int radiusOffsetXZ;
        private final int foliageHeightOffset;
        private final int sizeX;
        private final int sizeZ;

        public FoliageAttachment(BlockPos pos, int radiusOffsetXZ, boolean doubleTrunk) {
            int sizeXZ = doubleTrunk ? 2 : 1;
            this(pos, radiusOffsetXZ, sizeXZ, sizeXZ);
        }

        public FoliageAttachment(BlockPos pos, int radiusOffsetXZ, int sizeX, int sizeZ) {
            this(pos, radiusOffsetXZ, 0, sizeX, sizeZ);
        }

        public FoliageAttachment(BlockPos pos, int radiusOffsetXZ, int foliageHeightOffset, int sizeX, int sizeZ) {
            this.pos = pos;
            this.radiusOffsetXZ = radiusOffsetXZ;
            this.foliageHeightOffset = foliageHeightOffset;
            this.sizeX = sizeX;
            this.sizeZ = sizeZ;
        }

        public boolean doubleTrunk() {
            return this.sizeX == 2 && this.sizeZ == 2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{FoliageAttachment.class, "pos;radiusOffsetXZ;foliageHeightOffset;sizeX;sizeZ", "pos", "radiusOffsetXZ", "foliageHeightOffset", "sizeX", "sizeZ"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FoliageAttachment.class, "pos;radiusOffsetXZ;foliageHeightOffset;sizeX;sizeZ", "pos", "radiusOffsetXZ", "foliageHeightOffset", "sizeX", "sizeZ"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FoliageAttachment.class, "pos;radiusOffsetXZ;foliageHeightOffset;sizeX;sizeZ", "pos", "radiusOffsetXZ", "foliageHeightOffset", "sizeX", "sizeZ"}, this, o);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public int radiusOffsetXZ() {
            return this.radiusOffsetXZ;
        }

        public int foliageHeightOffset() {
            return this.foliageHeightOffset;
        }

        public int sizeX() {
            return this.sizeX;
        }

        public int sizeZ() {
            return this.sizeZ;
        }
    }
}

