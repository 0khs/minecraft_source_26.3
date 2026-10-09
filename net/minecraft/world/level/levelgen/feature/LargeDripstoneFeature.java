/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpeleothemUtils;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record LargeDripstoneFeature(HolderSet<Block> replaceableBlocks, int floorToCeilingSearchRange, IntProvider columnRadius, FloatProvider heightScale, float maxColumnRadiusToCaveHeightRatio, FloatProvider stalactiteBluntness, FloatProvider stalagmiteBluntness, FloatProvider windSpeed, int minRadiusForWind, float minBluntnessForWind) implements Feature
{
    public static final MapCodec<LargeDripstoneFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("replaceable_blocks").forGetter(LargeDripstoneFeature::replaceableBlocks), (App)Codec.intRange((int)1, (int)512).optionalFieldOf("floor_to_ceiling_search_range", (Object)30).forGetter(LargeDripstoneFeature::floorToCeilingSearchRange), (App)IntProviders.codec(1, 16).fieldOf("column_radius").forGetter(LargeDripstoneFeature::columnRadius), (App)FloatProviders.codec(0.0f, 20.0f).fieldOf("height_scale").forGetter(LargeDripstoneFeature::heightScale), (App)Codec.floatRange((float)0.1f, (float)1.0f).fieldOf("max_column_radius_to_cave_height_ratio").forGetter(LargeDripstoneFeature::maxColumnRadiusToCaveHeightRatio), (App)FloatProviders.codec(0.1f, 10.0f).fieldOf("stalactite_bluntness").forGetter(LargeDripstoneFeature::stalactiteBluntness), (App)FloatProviders.codec(0.1f, 10.0f).fieldOf("stalagmite_bluntness").forGetter(LargeDripstoneFeature::stalagmiteBluntness), (App)FloatProviders.codec(0.0f, 2.0f).fieldOf("wind_speed").forGetter(LargeDripstoneFeature::windSpeed), (App)Codec.intRange((int)0, (int)100).fieldOf("min_radius_for_wind").forGetter(LargeDripstoneFeature::minRadiusForWind), (App)Codec.floatRange((float)0.0f, (float)5.0f).fieldOf("min_bluntness_for_wind").forGetter(LargeDripstoneFeature::minBluntnessForWind)).apply((Applicative)i, LargeDripstoneFeature::new));

    public MapCodec<LargeDripstoneFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Column column;
        if (!SpeleothemUtils.isEmptyOrWater(level, origin)) {
            return false;
        }
        Optional<Column> column2 = Column.scan(level, origin, this.floorToCeilingSearchRange, SpeleothemUtils::isEmptyOrWater, state -> SpeleothemUtils.isBaseOrLava(state, Blocks.DRIPSTONE_BLOCK, this.replaceableBlocks));
        if (column2.isEmpty() || !((column = column2.get()) instanceof Column.Range)) {
            return false;
        }
        Column.Range columnRange = (Column.Range)column;
        if (columnRange.height() < 4) {
            return false;
        }
        int maxColumnRadiusBasedOnColumnHeight = (int)((float)columnRange.height() * this.maxColumnRadiusToCaveHeightRatio);
        int maxColumnRadius = Mth.clamp(maxColumnRadiusBasedOnColumnHeight, this.columnRadius.minInclusive(), this.columnRadius.maxInclusive());
        int radius = Mth.randomBetweenInclusive(random, this.columnRadius.minInclusive(), maxColumnRadius);
        LargeDripstone stalactite = LargeDripstoneFeature.makeDripstone(origin.atY(columnRange.ceiling() - 1), false, random, radius, this.stalactiteBluntness, this.heightScale);
        LargeDripstone stalagmite = LargeDripstoneFeature.makeDripstone(origin.atY(columnRange.floor() + 1), true, random, radius, this.stalagmiteBluntness, this.heightScale);
        WindOffsetter wind = stalactite.isSuitableForWind(this.minRadiusForWind, this.minBluntnessForWind) && stalagmite.isSuitableForWind(this.minRadiusForWind, this.minBluntnessForWind) ? new WindOffsetter(origin.getY(), random, this.windSpeed, 16 - radius) : WindOffsetter.noWind();
        boolean stalactiteBaseEmbeddedInStone = stalactite.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(level, wind);
        boolean stalagmiteBaseEmbeddedInStone = stalagmite.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(level, wind);
        if (stalactiteBaseEmbeddedInStone) {
            stalactite.placeBlocks(level, random, wind);
        }
        if (stalagmiteBaseEmbeddedInStone) {
            stalagmite.placeBlocks(level, random, wind);
        }
        if (SharedConstants.DEBUG_LARGE_DRIPSTONE) {
            this.placeDebugMarkers(level, origin, columnRange, wind);
        }
        return true;
    }

    private static LargeDripstone makeDripstone(BlockPos root, boolean pointingUp, RandomSource random, int radius, FloatProvider bluntness, FloatProvider heightScale) {
        return new LargeDripstone(root, pointingUp, radius, bluntness.sample(random), heightScale.sample(random));
    }

    private void placeDebugMarkers(WorldGenLevel level, BlockPos origin, Column.Range range, WindOffsetter wind) {
        level.setBlock(wind.offset(origin.atY(range.ceiling() - 1)), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
        level.setBlock(wind.offset(origin.atY(range.floor() + 1)), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        BlockPos.MutableBlockPos pos = origin.atY(range.floor() + 2).mutable();
        while (pos.getY() < range.ceiling() - 1) {
            BlockPos windAdjustedPos = wind.offset(pos);
            if (SpeleothemUtils.isEmptyOrWater(level, windAdjustedPos) || level.getBlockState(windAdjustedPos).is(Blocks.DRIPSTONE_BLOCK)) {
                level.setBlock(windAdjustedPos, Blocks.CREEPER_HEAD.defaultBlockState(), 2);
            }
            pos.move(Direction.UP);
        }
    }

    private static final class LargeDripstone {
        private BlockPos root;
        private final boolean pointingUp;
        private int radius;
        private final double bluntness;
        private final double scale;

        private LargeDripstone(BlockPos root, boolean pointingUp, int radius, double bluntness, double scale) {
            this.root = root;
            this.pointingUp = pointingUp;
            this.radius = radius;
            this.bluntness = bluntness;
            this.scale = scale;
        }

        private int getHeight() {
            return this.getHeightAtRadius(0.0f);
        }

        private boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel level, WindOffsetter wind) {
            while (this.radius > 1) {
                BlockPos.MutableBlockPos newRoot = this.root.mutable();
                int maxTries = Math.min(10, this.getHeight());
                for (int i = 0; i < maxTries; ++i) {
                    if (level.getBlockState(newRoot).is(Blocks.LAVA)) {
                        return false;
                    }
                    if (SpeleothemUtils.isCircleMostlyEmbeddedInStone(level, wind.offset(newRoot), this.radius)) {
                        this.root = newRoot;
                        return true;
                    }
                    newRoot.move(this.pointingUp ? Direction.DOWN : Direction.UP);
                }
                this.radius /= 2;
            }
            return false;
        }

        private int getHeightAtRadius(float checkRadius) {
            return (int)SpeleothemUtils.getSpeleothemHeight(checkRadius, this.radius, this.scale, this.bluntness);
        }

        private void placeBlocks(WorldGenLevel level, RandomSource random, WindOffsetter wind) {
            for (int dx = -this.radius; dx <= this.radius; ++dx) {
                block1: for (int dz = -this.radius; dz <= this.radius; ++dz) {
                    int height;
                    float currentRadius = Mth.sqrt(dx * dx + dz * dz);
                    if (currentRadius > (float)this.radius || (height = this.getHeightAtRadius(currentRadius)) <= 0) continue;
                    if ((double)random.nextFloat() < 0.2) {
                        height = (int)((float)height * Mth.randomBetween(random, 0.8f, 1.0f));
                    }
                    BlockPos.MutableBlockPos pos = this.root.offset(dx, 0, dz).mutable();
                    boolean hasBeenOutOfStone = false;
                    int maxY = this.pointingUp ? level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) : Integer.MAX_VALUE;
                    for (int i = 0; i < height && pos.getY() < maxY; ++i) {
                        BlockPos windAdjustedPos = wind.offset(pos);
                        if (SpeleothemUtils.isEmptyOrWaterOrLava(level, windAdjustedPos)) {
                            hasBeenOutOfStone = true;
                            Block block = SharedConstants.DEBUG_LARGE_DRIPSTONE ? Blocks.GLASS : Blocks.DRIPSTONE_BLOCK;
                            level.setBlock(windAdjustedPos, block.defaultBlockState(), 2);
                        } else if (hasBeenOutOfStone && level.getBlockState(windAdjustedPos).is(BlockTags.BASE_STONE_OVERWORLD)) continue block1;
                        pos.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                    }
                }
            }
        }

        private boolean isSuitableForWind(int minRadiusForWind, float minBluntnessForWind) {
            return this.radius >= minRadiusForWind && this.bluntness >= (double)minBluntnessForWind;
        }
    }

    private static final class WindOffsetter {
        private final int originY;
        private final @Nullable Vec3 windSpeed;
        private final int maxOffset;

        private WindOffsetter(int originY, RandomSource random, FloatProvider windSpeedRange, int maxOffset) {
            this.originY = originY;
            this.maxOffset = maxOffset;
            float speed = windSpeedRange.sample(random);
            float direction = Mth.randomBetween(random, 0.0f, (float)Math.PI);
            this.windSpeed = new Vec3(Mth.cos(direction) * speed, 0.0, Mth.sin(direction) * speed);
        }

        private WindOffsetter() {
            this.originY = 0;
            this.windSpeed = null;
            this.maxOffset = 0;
        }

        private static WindOffsetter noWind() {
            return new WindOffsetter();
        }

        private BlockPos offset(BlockPos pos) {
            if (this.windSpeed == null) {
                return pos;
            }
            int dy = this.originY - pos.getY();
            Vec3 totalWindAdjust = this.windSpeed.scale(dy);
            int dx = Mth.clamp(Mth.floor(totalWindAdjust.x), -this.maxOffset, this.maxOffset);
            int dz = Mth.clamp(Mth.floor(totalWindAdjust.z), -this.maxOffset, this.maxOffset);
            return pos.offset(dx, 0, dz);
        }
    }
}

