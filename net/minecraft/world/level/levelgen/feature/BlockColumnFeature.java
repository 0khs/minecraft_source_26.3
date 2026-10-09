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
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record BlockColumnFeature(List<Layer> layers, Direction direction, BlockPredicate allowedPlacement, boolean prioritizeTip) implements Feature
{
    public static final MapCodec<BlockColumnFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Layer.CODEC.listOf().fieldOf("layers").forGetter(BlockColumnFeature::layers), (App)Direction.CODEC.fieldOf("direction").forGetter(BlockColumnFeature::direction), (App)BlockPredicate.CODEC.fieldOf("allowed_placement").forGetter(BlockColumnFeature::allowedPlacement), (App)Codec.BOOL.fieldOf("prioritize_tip").forGetter(BlockColumnFeature::prioritizeTip)).apply((Applicative)i, BlockColumnFeature::new));

    public static Layer layer(IntProvider height, Holder<BlockStateProvider> state) {
        return new Layer(height, state);
    }

    public static Layer layer(IntProvider height, BlockStateProvider state) {
        return BlockColumnFeature.layer(height, Holder.direct(state));
    }

    public static BlockColumnFeature simple(IntProvider height, BlockStateProvider state) {
        return new BlockColumnFeature(List.of(BlockColumnFeature.layer(height, Holder.direct(state))), Direction.UP, BlockPredicate.ONLY_IN_AIR_PREDICATE, false);
    }

    public MapCodec<BlockColumnFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int layerCount = this.layers.size();
        int[] layerHeights = new int[layerCount];
        int totalHeight = 0;
        for (int i = 0; i < layerCount; ++i) {
            layerHeights[i] = this.layers.get(i).height().sample(random);
            totalHeight += layerHeights[i];
        }
        if (totalHeight == 0) {
            return false;
        }
        BlockPos.MutableBlockPos placePos = origin.mutable();
        BlockPos.MutableBlockPos nextPos = placePos.mutable().move(this.direction);
        for (int y = 0; y < totalHeight; ++y) {
            if (!this.allowedPlacement.test(level, nextPos)) {
                BlockColumnFeature.truncate(layerHeights, totalHeight, y, this.prioritizeTip);
                break;
            }
            nextPos.move(this.direction);
        }
        for (int i = 0; i < layerCount; ++i) {
            int count = layerHeights[i];
            if (count == 0) continue;
            Layer layer = this.layers.get(i);
            for (int y = 0; y < count; ++y) {
                level.setBlock(placePos, layer.state().value().getState(level, random, placePos), 2);
                placePos.move(this.direction);
            }
        }
        return true;
    }

    private static void truncate(int[] layerHeights, int totalHeight, int newHeight, boolean prioritizeTip) {
        int toRemoveFromLayer;
        int amountToRemove = totalHeight - newHeight;
        int direction = prioritizeTip ? 1 : -1;
        int start = prioritizeTip ? 0 : layerHeights.length - 1;
        int end = prioritizeTip ? layerHeights.length : -1;
        for (int i = start; i != end && amountToRemove > 0; amountToRemove -= toRemoveFromLayer, i += direction) {
            int thisLayer = layerHeights[i];
            toRemoveFromLayer = Math.min(thisLayer, amountToRemove);
            int n = i;
            layerHeights[n] = layerHeights[n] - toRemoveFromLayer;
        }
    }

    public record Layer(IntProvider height, Holder<BlockStateProvider> state) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(i -> i.group((App)IntProviders.NON_NEGATIVE_CODEC.fieldOf("height").forGetter(Layer::height), (App)BlockStateProvider.CODEC.fieldOf("provider").forGetter(Layer::state)).apply((Applicative)i, Layer::new));
    }
}

