/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class DimensionOriginStructurePlacement
implements StructurePlacement {
    public static final DimensionOriginStructurePlacement INSTANCE = new DimensionOriginStructurePlacement();
    public static final MapCodec<DimensionOriginStructurePlacement> CODEC = MapCodec.unit((Object)INSTANCE);

    private DimensionOriginStructurePlacement() {
    }

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int sourceX, int sourceZ) {
        ChunkPos origin = state.getDimensionOrigin();
        return origin.x() == sourceX && origin.z() == sourceZ;
    }

    public MapCodec<DimensionOriginStructurePlacement> codec() {
        return CODEC;
    }
}

