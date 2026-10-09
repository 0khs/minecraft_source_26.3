/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;

public record MobVisibility(HolderSet<EntityType<?>> targetingEntityTypes, float visibility) {
    public static final float MIN_VISIBILITY = 0.0f;
    public static final float MAX_VISIBILITY = 10.0f;
    public static final Codec<MobVisibility> CODEC = RecordCodecBuilder.create(i -> i.group((App)RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("targeting_entity_types").forGetter(MobVisibility::targetingEntityTypes), (App)ExtraCodecs.floatRange(0.0f, 10.0f).fieldOf("visibility").forGetter(MobVisibility::visibility)).apply((Applicative)i, MobVisibility::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MobVisibility> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderSet(Registries.ENTITY_TYPE), MobVisibility::targetingEntityTypes, ByteBufCodecs.FLOAT, MobVisibility::visibility, MobVisibility::new);
}

