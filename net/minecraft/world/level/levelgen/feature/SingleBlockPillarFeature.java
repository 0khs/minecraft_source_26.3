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
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record SingleBlockPillarFeature(Holder<BlockStateProvider> block, BlockPredicate canReplace, Direction direction, float chanceToContinue, Optional<Holder<PlacedFeature>> capFeature) implements Feature
{
    public static final MapCodec<SingleBlockPillarFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("block").forGetter(SingleBlockPillarFeature::block), (App)BlockPredicate.CODEC.optionalFieldOf("can_replace", (Object)BlockPredicate.alwaysTrue()).forGetter(SingleBlockPillarFeature::canReplace), (App)Direction.VERTICAL_CODEC.fieldOf("direction").forGetter(SingleBlockPillarFeature::direction), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("chance_to_continue", (Object)Float.valueOf(1.0f)).forGetter(SingleBlockPillarFeature::chanceToContinue), (App)PlacedFeature.CODEC.optionalFieldOf("cap_feature").forGetter(SingleBlockPillarFeature::capFeature)).apply((Applicative)i, SingleBlockPillarFeature::new));

    public SingleBlockPillarFeature(Holder<BlockStateProvider> block, BlockPredicate mayReplace, Direction direction, float chanceToContinue) {
        this(block, mayReplace, direction, chanceToContinue, Optional.empty());
    }

    public MapCodec<SingleBlockPillarFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockPos.MutableBlockPos pos = origin.mutable();
        while (this.canReplace.test(level, pos) && random.nextFloat() < this.chanceToContinue && !level.isOutsideBuildHeight(pos)) {
            level.setBlock(pos, this.block.value().getState(level, random, pos), 2);
            pos.move(this.direction);
        }
        pos.move(this.direction.getOpposite());
        this.capFeature.ifPresent(feature -> ((PlacedFeature)feature.value()).place(level, chunkGenerator, random, pos));
        return true;
    }
}

