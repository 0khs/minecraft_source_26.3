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
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

public record Compostable(ResolvableInt layers) {
    public static final Codec<Compostable> CODEC = RecordCodecBuilder.create(i -> i.group((App)ResolvableInt.CODEC.fieldOf("layers").forGetter(Compostable::layers)).apply((Applicative)i, Compostable::new));
    public static final StreamCodec<ByteBuf, Compostable> STREAM_CODEC = StreamCodec.composite(ResolvableInt.STREAM_CODEC, Compostable::layers, Compostable::new);

    public Compostable(ResourceKey<ContextIntProvider> layers) {
        this(ResolvableInt.fromKey(layers));
    }
}

