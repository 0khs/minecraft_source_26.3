/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.block.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record DecoratedPotPattern(Identifier assetId) {
    public static final Codec<DecoratedPotPattern> CODEC = RecordCodecBuilder.create(i -> i.group((App)Identifier.CODEC.fieldOf("asset_id").forGetter(DecoratedPotPattern::assetId)).apply((Applicative)i, DecoratedPotPattern::new));
}

