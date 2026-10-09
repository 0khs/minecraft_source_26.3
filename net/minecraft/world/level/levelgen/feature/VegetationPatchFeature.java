/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Function10
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Function10;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VegetationPatchFeature
implements Feature {
    public static final MapCodec<VegetationPatchFeature> CODEC = VegetationPatchFeature.makeCodec(VegetationPatchFeature::new);
    protected final HolderSet<Block> replaceable;
    protected final Holder<BlockStateProvider> groundState;
    protected final Holder<PlacedFeature> vegetationFeature;
    protected final CaveSurface surface;
    protected final IntProvider depth;
    protected final float extraBottomBlockChance;
    protected final int verticalRange;
    protected final float vegetationChance;
    protected final IntProvider xzRadius;
    protected final float extraEdgeColumnChance;

    protected static <T extends VegetationPatchFeature> MapCodec<T> makeCodec(Function10<HolderSet<Block>, Holder<BlockStateProvider>, Holder<PlacedFeature>, CaveSurface, IntProvider, Float, Integer, Float, IntProvider, Float, T> constructor) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("replaceable").forGetter(f -> f.replaceable), (App)BlockStateProvider.CODEC.fieldOf("ground_state").forGetter(f -> f.groundState), (App)PlacedFeature.CODEC.fieldOf("vegetation_feature").forGetter(f -> f.vegetationFeature), (App)CaveSurface.CODEC.fieldOf("surface").forGetter(f -> f.surface), (App)IntProviders.codec(1, 128).fieldOf("depth").forGetter(f -> f.depth), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("extra_bottom_block_chance").forGetter(f -> Float.valueOf(f.extraBottomBlockChance)), (App)Codec.intRange((int)1, (int)256).fieldOf("vertical_range").forGetter(f -> f.verticalRange), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("vegetation_chance").forGetter(f -> Float.valueOf(f.vegetationChance)), (App)IntProviders.CODEC.fieldOf("xz_radius").forGetter(f -> f.xzRadius), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("extra_edge_column_chance").forGetter(f -> Float.valueOf(f.extraEdgeColumnChance))).apply((Applicative)i, constructor));
    }

    public VegetationPatchFeature(HolderSet<Block> replaceable, Holder<BlockStateProvider> groundState, Holder<PlacedFeature> vegetationFeature, CaveSurface surface, IntProvider depth, float extraBottomBlockChance, int verticalRange, float vegetationChance, IntProvider xzRadius, float extraEdgeColumnChance) {
        this.replaceable = replaceable;
        this.groundState = groundState;
        this.vegetationFeature = vegetationFeature;
        this.surface = surface;
        this.depth = depth;
        this.extraBottomBlockChance = extraBottomBlockChance;
        this.verticalRange = verticalRange;
        this.vegetationChance = vegetationChance;
        this.xzRadius = xzRadius;
        this.extraEdgeColumnChance = extraEdgeColumnChance;
    }

    public MapCodec<? extends VegetationPatchFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int xRadius = this.xzRadius.sample(random) + 1;
        int zRadius = this.xzRadius.sample(random) + 1;
        Set<BlockPos> surface = this.placeGroundPatch(level, random, origin, s -> s.is(this.replaceable), xRadius, zRadius);
        this.distributeVegetation(level, chunkGenerator, random, surface);
        return !surface.isEmpty();
    }

    protected Set<BlockPos> placeGroundPatch(WorldGenLevel level, RandomSource random, BlockPos origin, Predicate<BlockState> replaceable, int xRadius, int zRadius) {
        BlockPos.MutableBlockPos pos = origin.mutable();
        BlockPos.MutableBlockPos belowPos = pos.mutable();
        Direction inwards = this.surface.getDirection();
        Direction outwards = inwards.getOpposite();
        HashSet<BlockPos> surface = new HashSet<BlockPos>();
        for (int dx = -xRadius; dx <= xRadius; ++dx) {
            boolean isXEdge = dx == -xRadius || dx == xRadius;
            for (int dz = -zRadius; dz <= zRadius; ++dz) {
                int offset;
                boolean isEdgeButNotCorner;
                boolean isZEdge = dz == -zRadius || dz == zRadius;
                boolean isEdge = isXEdge || isZEdge;
                boolean isCorner = isXEdge && isZEdge;
                boolean bl = isEdgeButNotCorner = isEdge && !isCorner;
                if (isCorner || isEdgeButNotCorner && (this.extraEdgeColumnChance == 0.0f || random.nextFloat() > this.extraEdgeColumnChance)) continue;
                pos.setWithOffset(origin, dx, 0, dz);
                for (offset = 0; level.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::isAir) && offset < this.verticalRange; ++offset) {
                    pos.move(inwards);
                }
                for (offset = 0; level.isStateAtPosition(pos, s -> !s.isAir()) && offset < this.verticalRange; ++offset) {
                    pos.move(outwards);
                }
                belowPos.setWithOffset((Vec3i)pos, this.surface.getDirection());
                BlockState belowState = level.getBlockState(belowPos);
                if (!level.isEmptyBlock(pos) || !belowState.isFaceSturdy(level, belowPos, this.surface.getDirection().getOpposite())) continue;
                int depth = this.depth.sample(random) + (this.extraBottomBlockChance > 0.0f && random.nextFloat() < this.extraBottomBlockChance ? 1 : 0);
                BlockPos groundPos = belowPos.immutable();
                boolean groundPlaced = this.placeGround(level, replaceable, random, belowPos, depth);
                if (!groundPlaced) continue;
                surface.add(groundPos);
            }
        }
        return surface;
    }

    private void distributeVegetation(WorldGenLevel level, ChunkGenerator generator, RandomSource random, Set<BlockPos> surface) {
        for (BlockPos surfacePos : surface) {
            if (!(this.vegetationChance > 0.0f) || !(random.nextFloat() < this.vegetationChance)) continue;
            this.placeVegetation(level, generator, random, surfacePos);
        }
    }

    protected boolean placeVegetation(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos vegetationPos) {
        return this.vegetationFeature.value().place(level, generator, random, vegetationPos.relative(this.surface.getDirection().getOpposite()));
    }

    private boolean placeGround(WorldGenLevel level, Predicate<BlockState> replaceable, RandomSource random, BlockPos.MutableBlockPos belowPos, int depth) {
        for (int i = 0; i < depth; ++i) {
            BlockState belowState;
            BlockState stateToPlace = this.groundState.value().getState(level, random, belowPos);
            if (stateToPlace.is((belowState = level.getBlockState(belowPos)).getBlock())) continue;
            if (!replaceable.test(belowState)) {
                return i != 0;
            }
            level.setBlock(belowPos, stateToPlace, 2);
            belowPos.move(this.surface.getDirection());
        }
        return true;
    }
}

