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
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

public record LocationCheck(Optional<LocationPredicate> predicate, Vec3i offset) implements LootItemCondition
{
    private static final MapCodec<Vec3i> OFFSET_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.INT.optionalFieldOf("offsetX", (Object)0).forGetter(Vec3i::getX), (App)Codec.INT.optionalFieldOf("offsetY", (Object)0).forGetter(Vec3i::getY), (App)Codec.INT.optionalFieldOf("offsetZ", (Object)0).forGetter(Vec3i::getZ)).apply((Applicative)i, Vec3i::new));
    public static final MapCodec<LocationCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)LocationPredicate.CODEC.optionalFieldOf("predicate").forGetter(LocationCheck::predicate), (App)OFFSET_CODEC.forGetter(LocationCheck::offset)).apply((Applicative)i, LocationCheck::new));

    public MapCodec<LocationCheck> codec() {
        return MAP_CODEC;
    }

    @Override
    public boolean test(LootContext context) {
        Vec3 pos = context.getOptional(LootContextParams.ORIGIN);
        return pos != null && (this.predicate.isEmpty() || this.predicate.get().matches(context.getLevel(), pos.x() + (double)this.offset.getX(), pos.y() + (double)this.offset.getY(), pos.z() + (double)this.offset.getZ()));
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder predicate) {
        return () -> new LocationCheck(Optional.of(predicate.build()), Vec3i.ZERO);
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder predicate, Vec3i offset) {
        return () -> new LocationCheck(Optional.of(predicate.build()), offset);
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder predicate, Direction direction) {
        return LocationCheck.checkLocation(predicate, direction.getUnitVec3i());
    }
}

