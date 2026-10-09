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
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.util.valueproviders.TrapezoidInt;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public record OffsetPlacement(IntProvider x, IntProvider y, IntProvider z) implements PlacementModifier
{
    public static final MapCodec<OffsetPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)IntProviders.codec(-16, 16).fieldOf("x").forGetter(OffsetPlacement::x), (App)IntProviders.codec(-16, 16).fieldOf("y").forGetter(OffsetPlacement::y), (App)IntProviders.codec(-16, 16).fieldOf("z").forGetter(OffsetPlacement::z)).apply((Applicative)i, OffsetPlacement::new));

    public static OffsetPlacement of(IntProvider xzSpread, IntProvider ySpread) {
        return new OffsetPlacement(xzSpread, ySpread, xzSpread);
    }

    public static OffsetPlacement ofTriangle(int xzRange, int yRange) {
        return OffsetPlacement.of(TrapezoidInt.triangle(xzRange), TrapezoidInt.triangle(yRange));
    }

    public static OffsetPlacement vertical(IntProvider ySpread) {
        return OffsetPlacement.of(ConstantInt.of(0), ySpread);
    }

    public static OffsetPlacement horizontal(IntProvider xzSpread) {
        return OffsetPlacement.of(xzSpread, ConstantInt.of(0));
    }

    public static OffsetPlacement of(int x, int y, int z) {
        return new OffsetPlacement(ConstantInt.of(x), ConstantInt.of(y), ConstantInt.of(z));
    }

    public static OffsetPlacement of(Direction direction) {
        return OffsetPlacement.of(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public static OffsetPlacement above() {
        return OffsetPlacement.of(0, 1, 0);
    }

    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        output.accept(origin.offset(this.x.sample(random), this.y.sample(random), this.z.sample(random)));
    }

    public MapCodec<OffsetPlacement> codec() {
        return CODEC;
    }
}

