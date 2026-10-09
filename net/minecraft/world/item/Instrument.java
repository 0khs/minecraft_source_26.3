/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record Instrument(Holder<SoundEvent> soundEvent, float useDuration, float range, int durabilityDamage, Component description) {
    public static final Codec<Instrument> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group((App)SoundEvent.CODEC.fieldOf("sound_event").forGetter(Instrument::soundEvent), (App)ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("use_duration").forGetter(Instrument::useDuration), (App)ExtraCodecs.POSITIVE_FLOAT.fieldOf("range").forGetter(Instrument::range), (App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("durability_damage", (Object)0).forGetter(Instrument::durabilityDamage), (App)ComponentSerialization.CODEC.fieldOf("description").forGetter(Instrument::description)).apply((Applicative)i, Instrument::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Instrument> DIRECT_STREAM_CODEC = StreamCodec.composite(SoundEvent.STREAM_CODEC, Instrument::soundEvent, ByteBufCodecs.FLOAT, Instrument::useDuration, ByteBufCodecs.FLOAT, Instrument::range, ByteBufCodecs.VAR_INT, Instrument::durabilityDamage, ComponentSerialization.STREAM_CODEC, Instrument::description, Instrument::new);
    public static final Codec<Holder<Instrument>> CODEC = RegistryCodecs.holder(Registries.INSTRUMENT, DIRECT_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Instrument>> STREAM_CODEC = ByteBufCodecs.holder(Registries.INSTRUMENT, DIRECT_STREAM_CODEC);
}

