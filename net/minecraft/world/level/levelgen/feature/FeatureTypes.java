/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.BambooFeature;
import net.minecraft.world.level.levelgen.feature.BlockBlobFeature;
import net.minecraft.world.level.levelgen.feature.BlockColumnFeature;
import net.minecraft.world.level.levelgen.feature.BlockPileFeature;
import net.minecraft.world.level.levelgen.feature.BlueIceFeature;
import net.minecraft.world.level.levelgen.feature.BonusChestFeature;
import net.minecraft.world.level.levelgen.feature.ChorusPlantFeature;
import net.minecraft.world.level.levelgen.feature.CoralClawFeature;
import net.minecraft.world.level.levelgen.feature.CoralTreeFeature;
import net.minecraft.world.level.levelgen.feature.DeltaFeature;
import net.minecraft.world.level.levelgen.feature.DiskFeature;
import net.minecraft.world.level.levelgen.feature.EndGatewayFeature;
import net.minecraft.world.level.levelgen.feature.EndIslandFeature;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import net.minecraft.world.level.levelgen.feature.FallenTreeFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FillLayerFeature;
import net.minecraft.world.level.levelgen.feature.FossilFeature;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.HugeBrownMushroomFeature;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;
import net.minecraft.world.level.levelgen.feature.HugeRedMushroomFeature;
import net.minecraft.world.level.levelgen.feature.IcebergFeature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.LargeDripstoneFeature;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.MultifaceGrowthFeature;
import net.minecraft.world.level.levelgen.feature.NoOpFeature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.OverlayFeature;
import net.minecraft.world.level.levelgen.feature.ProjectedRandomPatchySquare;
import net.minecraft.world.level.levelgen.feature.RandomBooleanSelectorFeature;
import net.minecraft.world.level.levelgen.feature.RandomNeighborSpreadFeature;
import net.minecraft.world.level.levelgen.feature.RandomSelectorFeature;
import net.minecraft.world.level.levelgen.feature.ReplaceBlobsFeature;
import net.minecraft.world.level.levelgen.feature.ReplaceBlockFeature;
import net.minecraft.world.level.levelgen.feature.RootSystemFeature;
import net.minecraft.world.level.levelgen.feature.ScatteredOreFeature;
import net.minecraft.world.level.levelgen.feature.SculkPatchFeature;
import net.minecraft.world.level.levelgen.feature.SequenceFeature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.SimpleRandomSelectorFeature;
import net.minecraft.world.level.levelgen.feature.SingleBlockPillarFeature;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import net.minecraft.world.level.levelgen.feature.SpeleothemClusterFeature;
import net.minecraft.world.level.levelgen.feature.SpeleothemFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.SpringFeature;
import net.minecraft.world.level.levelgen.feature.SteppedColumnClusterFeature;
import net.minecraft.world.level.levelgen.feature.TemplateFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.UnderwaterMagmaFeature;
import net.minecraft.world.level.levelgen.feature.VegetationPatchFeature;
import net.minecraft.world.level.levelgen.feature.VinesFeature;
import net.minecraft.world.level.levelgen.feature.VoidStartPlatformFeature;
import net.minecraft.world.level.levelgen.feature.WaterloggedVegetationPatchFeature;
import net.minecraft.world.level.levelgen.feature.WeightedRandomSelectorFeature;

public interface FeatureTypes {
    public static MapCodec<? extends Feature> bootstrap(Registry<MapCodec<? extends Feature>> registry) {
        Registry.register(registry, "bamboo", BambooFeature.CODEC);
        Registry.register(registry, "block_blob", BlockBlobFeature.CODEC);
        Registry.register(registry, "block_column", BlockColumnFeature.CODEC);
        Registry.register(registry, "block_pile", BlockPileFeature.CODEC);
        Registry.register(registry, "blue_ice", BlueIceFeature.CODEC);
        Registry.register(registry, "bonus_chest", BonusChestFeature.CODEC);
        Registry.register(registry, "chorus_plant", ChorusPlantFeature.CODEC);
        Registry.register(registry, "coral_claw", CoralClawFeature.CODEC);
        Registry.register(registry, "coral_tree", CoralTreeFeature.CODEC);
        Registry.register(registry, "delta_feature", DeltaFeature.CODEC);
        Registry.register(registry, "disk", DiskFeature.CODEC);
        Registry.register(registry, "end_gateway", EndGatewayFeature.CODEC);
        Registry.register(registry, "end_island", EndIslandFeature.CODEC);
        Registry.register(registry, "end_platform", EndPlatformFeature.CODEC);
        Registry.register(registry, "end_podium", EndPodiumFeature.CODEC);
        Registry.register(registry, "end_spike", EndSpikeFeature.CODEC);
        Registry.register(registry, "fallen_tree", FallenTreeFeature.CODEC);
        Registry.register(registry, "fill_layer", FillLayerFeature.CODEC);
        Registry.register(registry, "fossil", FossilFeature.CODEC);
        Registry.register(registry, "freeze_top_layer", SnowAndFreezeFeature.CODEC);
        Registry.register(registry, "geode", GeodeFeature.CODEC);
        Registry.register(registry, "huge_brown_mushroom", HugeBrownMushroomFeature.CODEC);
        Registry.register(registry, "huge_fungus", HugeFungusFeature.CODEC);
        Registry.register(registry, "huge_red_mushroom", HugeRedMushroomFeature.CODEC);
        Registry.register(registry, "iceberg", IcebergFeature.CODEC);
        Registry.register(registry, "lake", LakeFeature.CODEC);
        Registry.register(registry, "large_dripstone", LargeDripstoneFeature.CODEC);
        Registry.register(registry, "monster_room", MonsterRoomFeature.CODEC);
        Registry.register(registry, "multiface_growth", MultifaceGrowthFeature.CODEC);
        Registry.register(registry, "netherrack_replace_blobs", ReplaceBlobsFeature.CODEC);
        Registry.register(registry, "no_op", NoOpFeature.CODEC);
        Registry.register(registry, "ore", OreFeature.CODEC);
        Registry.register(registry, "overlay", OverlayFeature.CODEC);
        Registry.register(registry, "projected_random_patchy_square", ProjectedRandomPatchySquare.CODEC);
        Registry.register(registry, "random_boolean_selector", RandomBooleanSelectorFeature.CODEC);
        Registry.register(registry, "random_neighbor_spread", RandomNeighborSpreadFeature.CODEC);
        Registry.register(registry, "random_selector", RandomSelectorFeature.CODEC);
        Registry.register(registry, "replace_single_block", ReplaceBlockFeature.CODEC);
        Registry.register(registry, "root_system", RootSystemFeature.CODEC);
        Registry.register(registry, "scattered_ore", ScatteredOreFeature.CODEC);
        Registry.register(registry, "sculk_patch", SculkPatchFeature.CODEC);
        Registry.register(registry, "sequence", SequenceFeature.CODEC);
        Registry.register(registry, "simple_block", SimpleBlockFeature.CODEC);
        Registry.register(registry, "simple_random_selector", SimpleRandomSelectorFeature.CODEC);
        Registry.register(registry, "single_block_pillar", SingleBlockPillarFeature.CODEC);
        Registry.register(registry, "speleothem", SpeleothemFeature.CODEC);
        Registry.register(registry, "speleothem_cluster", SpeleothemClusterFeature.CODEC);
        Registry.register(registry, "spike", SpikeFeature.CODEC);
        Registry.register(registry, "spring_feature", SpringFeature.CODEC);
        Registry.register(registry, "stepped_column_cluster", SteppedColumnClusterFeature.CODEC);
        Registry.register(registry, "template", TemplateFeature.CODEC);
        Registry.register(registry, "tree", TreeFeature.CODEC);
        Registry.register(registry, "underwater_magma", UnderwaterMagmaFeature.CODEC);
        Registry.register(registry, "vegetation_patch", VegetationPatchFeature.CODEC);
        Registry.register(registry, "vines", VinesFeature.CODEC);
        Registry.register(registry, "void_start_platform", VoidStartPlatformFeature.CODEC);
        Registry.register(registry, "waterlogged_vegetation_patch", WaterloggedVegetationPatchFeature.CODEC);
        return Registry.register(registry, "weighted_random_selector", WeightedRandomSelectorFeature.CODEC);
    }
}

