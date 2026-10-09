/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public record FixedPlacement(List<BlockPos> positions) implements PlacementModifier
{
    public static final MapCodec<FixedPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockPos.CODEC.listOf().fieldOf("positions").forGetter(c -> c.positions)).apply((Applicative)i, FixedPlacement::new));

    public static FixedPlacement of(BlockPos ... pos) {
        return new FixedPlacement(List.of(pos));
    }

    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        for (BlockPos position : this.positions) {
            if (!FixedPlacement.isSameChunk(chunkX, chunkZ, position)) continue;
            output.accept(position);
        }
    }

    private static boolean isSameChunk(int chunkX, int chunkZ, BlockPos position) {
        return chunkX == SectionPos.blockToSectionCoord(position.getX()) && chunkZ == SectionPos.blockToSectionCoord(position.getZ());
    }

    public MapCodec<FixedPlacement> codec() {
        return CODEC;
    }
}

