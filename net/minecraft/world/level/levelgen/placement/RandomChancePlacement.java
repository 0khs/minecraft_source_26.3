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
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;

public record RandomChancePlacement(float chance) implements PlacementFilter
{
    public static final MapCodec<RandomChancePlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance").forGetter(RandomChancePlacement::chance)).apply((Applicative)i, RandomChancePlacement::new));

    public MapCodec<RandomChancePlacement> codec() {
        return CODEC;
    }

    @Override
    public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
        return random.nextFloat() < this.chance;
    }
}

