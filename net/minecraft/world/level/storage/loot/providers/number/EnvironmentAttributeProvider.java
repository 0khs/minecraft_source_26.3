/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public interface EnvironmentAttributeProvider
extends LootContextUser {
    public static <Self extends EnvironmentAttributeProvider> MapCodec<Self> mapCodec(Codec<EnvironmentAttribute<?>> attributeCodec, Factory<Self> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)attributeCodec.fieldOf("attribute").forGetter(EnvironmentAttributeProvider::attribute)).apply((Applicative)i, factory::create));
    }

    public EnvironmentAttribute<?> attribute();

    @Override
    default public Set<ContextKey<?>> getReferencedContextParams() {
        return this.attribute().isPositional() ? Set.of(LootContextParams.ORIGIN) : Set.of();
    }

    @FunctionalInterface
    public static interface Factory<Self extends EnvironmentAttributeProvider> {
        public Self create(EnvironmentAttribute<?> var1);
    }
}

