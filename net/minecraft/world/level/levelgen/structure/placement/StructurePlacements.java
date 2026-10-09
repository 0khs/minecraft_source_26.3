/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.DimensionOriginStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public interface StructurePlacements {
    public static MapCodec<? extends StructurePlacement> bootstrap(Registry<MapCodec<? extends StructurePlacement>> registry) {
        Registry.register(registry, "concentric_rings", ConcentricRingsStructurePlacement.CODEC);
        Registry.register(registry, "dimension_origin", DimensionOriginStructurePlacement.CODEC);
        return Registry.register(registry, "random_spread", RandomSpreadStructurePlacement.CODEC);
    }
}

