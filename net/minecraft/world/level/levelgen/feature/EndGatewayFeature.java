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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record EndGatewayFeature(Optional<BlockPos> exit, boolean exact) implements Feature
{
    public static final MapCodec<EndGatewayFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockPos.CODEC.optionalFieldOf("exit").forGetter(EndGatewayFeature::exit), (App)Codec.BOOL.fieldOf("exact").forGetter(EndGatewayFeature::exact)).apply((Applicative)i, EndGatewayFeature::new));

    public MapCodec<EndGatewayFeature> codec() {
        return CODEC;
    }

    public static EndGatewayFeature knownExit(BlockPos exit, boolean exact) {
        return new EndGatewayFeature(Optional.of(exit), exact);
    }

    public static EndGatewayFeature delayedExitSearch() {
        return new EndGatewayFeature(Optional.empty(), false);
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -2, -1), origin.offset(1, 2, 1))) {
            boolean end;
            boolean sameX = pos.getX() == origin.getX();
            boolean sameY = pos.getY() == origin.getY();
            boolean sameZ = pos.getZ() == origin.getZ();
            boolean bl = end = Math.abs(pos.getY() - origin.getY()) == 2;
            if (sameX && sameY && sameZ) {
                BlockPos immutable = pos.immutable();
                this.setBlock(level, immutable, Blocks.END_GATEWAY.defaultBlockState());
                this.exit.ifPresent(targetPos -> {
                    BlockEntity exitEntity = level.getBlockEntity(immutable);
                    if (exitEntity instanceof TheEndGatewayBlockEntity) {
                        TheEndGatewayBlockEntity exitGateway = (TheEndGatewayBlockEntity)exitEntity;
                        exitGateway.setExitPosition((BlockPos)targetPos, this.exact);
                    }
                });
                continue;
            }
            if (sameY) {
                this.setBlock(level, pos, Blocks.AIR.defaultBlockState());
                continue;
            }
            if (end && sameX && sameZ) {
                this.setBlock(level, pos, Blocks.BEDROCK.defaultBlockState());
                continue;
            }
            if (!sameX && !sameZ || end) {
                this.setBlock(level, pos, Blocks.AIR.defaultBlockState());
                continue;
            }
            this.setBlock(level, pos, Blocks.BEDROCK.defaultBlockState());
        }
        return true;
    }
}

