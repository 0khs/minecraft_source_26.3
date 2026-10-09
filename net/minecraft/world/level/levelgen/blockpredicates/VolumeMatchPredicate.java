/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

public record VolumeMatchPredicate(Vec3i min, Vec3i max, BlockPredicate match) implements BlockPredicate
{
    private static final MapCodec<VolumeMatchPredicate> UNVALIDATED_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Vec3i.offsetCodec(16).fieldOf("min").forGetter(VolumeMatchPredicate::min), (App)Vec3i.offsetCodec(16).fieldOf("max").forGetter(VolumeMatchPredicate::max), (App)BlockPredicate.CODEC.fieldOf("match").forGetter(VolumeMatchPredicate::match)).apply((Applicative)i, VolumeMatchPredicate::new));
    public static final MapCodec<VolumeMatchPredicate> CODEC = UNVALIDATED_CODEC.validate(p -> p.min.getX() > p.max.getX() || p.min.getY() > p.max.getY() || p.min.getZ() > p.max.getZ() ? DataResult.error(() -> "min bound cannot be larger than max bound") : DataResult.success((Object)p));

    public BlockPredicateType<VolumeMatchPredicate> type() {
        return BlockPredicateType.VOLUME_MATCH;
    }

    @Override
    public boolean test(LevelAccessor level, BlockPos blockPos) {
        BlockPos.MutableBlockPos pos = blockPos.mutable();
        for (int ox = this.min.getX(); ox <= this.max.getX(); ++ox) {
            for (int oz = this.min.getZ(); oz <= this.max.getZ(); ++oz) {
                for (int oy = this.min.getY(); oy <= this.max.getY(); ++oy) {
                    if (this.match.test(level, pos.setWithOffset(blockPos, ox, oy, oz))) continue;
                    return false;
                }
            }
        }
        return true;
    }
}

