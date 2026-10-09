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
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

public record CookingFuel(ResolvableInt burnTime, ResolvableFloat speedMultiplier) {
    public static final Codec<CookingFuel> CODEC = RecordCodecBuilder.create(i -> i.group((App)ResolvableInt.CODEC.fieldOf("burn_time").forGetter(CookingFuel::burnTime), (App)ResolvableFloat.CODEC.fieldOf("speed_multiplier").forGetter(CookingFuel::speedMultiplier)).apply((Applicative)i, CookingFuel::new));
    public static final StreamCodec<ByteBuf, CookingFuel> STREAM_CODEC = StreamCodec.composite(ResolvableInt.STREAM_CODEC, CookingFuel::burnTime, ResolvableFloat.STREAM_CODEC, CookingFuel::speedMultiplier, CookingFuel::new);

    public CookingFuel(ResourceKey<ContextIntProvider> burnTime, ResourceKey<ContextFloatProvider> speedMultiplier) {
        this(ResolvableInt.fromKey(burnTime), ResolvableFloat.fromKey(speedMultiplier));
    }
}

