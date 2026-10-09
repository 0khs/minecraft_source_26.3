/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.world.level.levelgen.placement;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureCountTracker;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class FeaturePlacer {
    private final WorldGenLevel level;
    private final ChunkGenerator generator;
    private final List<BlockPos> positions = new ArrayList<BlockPos>();
    private final IntList modifierIndices = new IntArrayList();
    private final List<BlockPos> modifiedPositions = new ArrayList<BlockPos>();

    public FeaturePlacer(WorldGenLevel level, ChunkGenerator generator) {
        this.level = level;
        this.generator = generator;
    }

    public boolean place(PlacedFeature placedFeature, RandomSource random, BlockPos origin) {
        return this.place(placedFeature, random, origin, false);
    }

    public boolean placeWithBiomeCheck(PlacedFeature placedFeature, RandomSource random, BlockPos origin) {
        return this.place(placedFeature, random, origin, true);
    }

    private boolean place(PlacedFeature placedFeature, RandomSource random, BlockPos origin, boolean biomeCheck) {
        Optional<PlacedFeature> topFeature = biomeCheck ? Optional.of(placedFeature) : Optional.empty();
        PlacementContext context = new PlacementContext(this.level, this.generator, topFeature);
        Feature feature = placedFeature.feature().value();
        List<PlacementModifier> placement = placedFeature.placement();
        if (placement.isEmpty()) {
            if (SharedConstants.DEBUG_FEATURE_COUNT) {
                FeatureCountTracker.featurePlaced(this.level.getLevel(), feature, topFeature);
            }
            return feature.place(this.level, this.generator, random, origin);
        }
        boolean placedAny = false;
        this.positions.add(origin);
        this.modifierIndices.add(0);
        while (!this.positions.isEmpty()) {
            BlockPos pos = this.positions.removeLast();
            int modifierIndex = this.modifierIndices.removeInt(this.modifierIndices.size() - 1);
            PlacementModifier modifier = placement.get(modifierIndex);
            modifier.modify(context, random, pos, this.modifiedPositions::add);
            int nextModifierIndex = modifierIndex + 1;
            if (nextModifierIndex < placement.size()) {
                for (int i = this.modifiedPositions.size() - 1; i >= 0; --i) {
                    this.positions.add(this.modifiedPositions.get(i));
                    this.modifierIndices.add(nextModifierIndex);
                }
            } else {
                for (BlockPos nextPos : this.modifiedPositions) {
                    placedAny |= feature.place(this.level, this.generator, random, nextPos);
                    if (!SharedConstants.DEBUG_FEATURE_COUNT) continue;
                    FeatureCountTracker.featurePlaced(this.level.getLevel(), feature, topFeature);
                }
            }
            this.modifiedPositions.clear();
        }
        return placedAny;
    }
}

