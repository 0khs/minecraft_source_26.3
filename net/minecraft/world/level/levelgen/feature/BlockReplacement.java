/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public record BlockReplacement(RuleTest target, BlockState state) {
    public static final Codec<BlockReplacement> CODEC = RecordCodecBuilder.create(i -> i.group((App)RuleTest.CODEC.fieldOf("target").forGetter(c -> c.target), (App)BlockState.CODEC.fieldOf("state").forGetter(c -> c.state)).apply((Applicative)i, BlockReplacement::new));

    public static BlockReplacement replace(RuleTest rule, BlockState state) {
        return new BlockReplacement(rule, state);
    }
}

