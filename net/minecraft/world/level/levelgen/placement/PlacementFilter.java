/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public interface PlacementFilter
extends PlacementModifier {
    @Override
    default public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        if (this.shouldPlace(context, random, origin)) {
            output.accept(origin);
        }
    }

    public boolean shouldPlace(PlacementContext var1, RandomSource var2, BlockPos var3);

    public MapCodec<? extends PlacementFilter> codec();
}

