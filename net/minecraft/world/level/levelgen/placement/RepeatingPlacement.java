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

public interface RepeatingPlacement
extends PlacementModifier {
    public int count(RandomSource var1, BlockPos var2);

    @Override
    default public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        int count = this.count(random, origin);
        for (int i = 0; i < count; ++i) {
            output.accept(origin);
        }
    }

    public MapCodec<? extends RepeatingPlacement> codec();
}

