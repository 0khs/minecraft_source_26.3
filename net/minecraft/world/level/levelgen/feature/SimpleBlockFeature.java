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
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.MossyCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record SimpleBlockFeature(Holder<BlockStateProvider> toPlace, boolean scheduleTick) implements Feature
{
    public static final MapCodec<SimpleBlockFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("to_place").forGetter(SimpleBlockFeature::toPlace), (App)Codec.BOOL.optionalFieldOf("schedule_tick", (Object)false).forGetter(SimpleBlockFeature::scheduleTick)).apply((Applicative)i, SimpleBlockFeature::new));

    public SimpleBlockFeature(Holder<BlockStateProvider> toPlace) {
        this(toPlace, false);
    }

    public SimpleBlockFeature(BlockStateProvider toPlace) {
        this(Holder.direct(toPlace));
    }

    public MapCodec<SimpleBlockFeature> codec() {
        return CODEC;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockState stateToPlace = this.toPlace.value().getOptionalState(level, random, origin);
        if (stateToPlace == null) {
            return false;
        }
        if (!stateToPlace.canSurvive(level, origin)) return false;
        if (stateToPlace.getBlock() instanceof DoublePlantBlock) {
            BlockState aboveState = level.getBlockState(origin.above());
            if (!aboveState.isAir() && (!Objects.equals(stateToPlace.getFluidState(), aboveState.getFluidState()) || !aboveState.canBeReplaced())) return false;
            DoublePlantBlock.placeAt(level, stateToPlace, origin, 2);
        } else if (stateToPlace.getBlock() instanceof MossyCarpetBlock) {
            MossyCarpetBlock.placeAt(level, origin, level.getRandom(), 2);
        } else {
            level.setBlock(origin, stateToPlace, 2);
        }
        if (!this.scheduleTick) return true;
        level.scheduleTick(origin, level.getBlockState(origin).getBlock(), 1);
        return true;
    }
}

