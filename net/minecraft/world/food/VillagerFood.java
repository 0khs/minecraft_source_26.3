/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.food;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record VillagerFood(int nutrition) {
    public static final Codec<VillagerFood> CODEC = RecordCodecBuilder.create(i -> i.group((App)ExtraCodecs.POSITIVE_INT.fieldOf("nutrition").forGetter(VillagerFood::nutrition)).apply((Applicative)i, VillagerFood::new));
    public static final StreamCodec<ByteBuf, VillagerFood> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, VillagerFood::nutrition, VillagerFood::new);
}

