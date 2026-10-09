/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Keyable
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MobSpawnSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final float DEFAULT_CREATURE_WORLD_GEN_SPAWN_PROBABILITY = 0.1f;
    public static final WeightedList<SpawnerData> EMPTY_MOB_LIST = WeightedList.of();
    public static final MobSpawnSettings EMPTY = new Builder().build();
    public static final MobSpawnSettings NO_SPAWNS = Util.make(() -> {
        Builder builder = new Builder();
        for (MobCategory category : MobCategory.values()) {
            builder.noSpawns(category);
        }
        return builder.build();
    });
    public static final Codec<MobSpawnSettings> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.simpleMap(MobCategory.CODEC, (Codec)WeightedList.codec(SpawnerData.CODEC).promotePartial(Util.prefix("Spawn data: ", arg_0 -> ((Logger)LOGGER).error(arg_0))), (Keyable)StringRepresentable.keys(MobCategory.values())).fieldOf("spawns_by_category").forGetter(b -> b.spawnsByCategory), (App)Codec.simpleMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), MobSpawnCost.CODEC, BuiltInRegistries.ENTITY_TYPE).fieldOf("spawn_costs").forGetter(b -> b.mobSpawnCosts)).apply((Applicative)i, MobSpawnSettings::new));
    private final Map<MobCategory, WeightedList<SpawnerData>> spawnsByCategory;
    private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts;

    private MobSpawnSettings(Map<MobCategory, WeightedList<SpawnerData>> spawnsByCategory, Map<EntityType<?>, MobSpawnCost> mobSpawnCosts) {
        this.spawnsByCategory = ImmutableMap.copyOf(spawnsByCategory);
        this.mobSpawnCosts = ImmutableMap.copyOf(mobSpawnCosts);
    }

    public WeightedList<SpawnerData> getMobsToSpawn(MobCategory category) {
        return this.spawnsByCategory.getOrDefault(category, EMPTY_MOB_LIST);
    }

    public @Nullable WeightedList<SpawnerData> getMobsInCategory(MobCategory category) {
        return this.spawnsByCategory.get(category);
    }

    public Set<MobCategory> definedCategories() {
        return this.spawnsByCategory.keySet();
    }

    public @Nullable MobSpawnCost getMobSpawnCost(EntityType<?> type) {
        return this.mobSpawnCosts.get(type);
    }

    public Map<EntityType<?>, MobSpawnCost> allSpawnCosts() {
        return this.mobSpawnCosts;
    }

    public record MobSpawnCost(double energyBudget, double charge) {
        public static final Codec<MobSpawnCost> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.DOUBLE.fieldOf("energy_budget").forGetter(e -> e.energyBudget), (App)Codec.DOUBLE.fieldOf("charge").forGetter(e -> e.charge)).apply((Applicative)i, MobSpawnCost::new));
    }

    public record SpawnerData(EntityType<?> type, IntProvider count) {
        public static final MapCodec<SpawnerData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(SpawnerData::type), (App)IntProviders.CODEC.fieldOf("count").forGetter(SpawnerData::count)).apply((Applicative)i, SpawnerData::new));

        public SpawnerData {
            type = type.getCategory() == MobCategory.MISC ? EntityTypes.PIG : type;
        }

        @Override
        public String toString() {
            return String.valueOf(EntityType.getKey(this.type)) + "*(" + this.count.minInclusive() + "-" + this.count.maxInclusive() + ") (" + String.valueOf(BuiltInRegistries.INT_PROVIDER_TYPE.getKey(this.count.codec())) + ")";
        }
    }

    public static class Builder {
        private final Map<MobCategory, WeightedList.Builder<SpawnerData>> spawnsByCategory = new EnumMap<MobCategory, WeightedList.Builder<SpawnerData>>(MobCategory.class);
        private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts = Maps.newLinkedHashMap();

        public Builder addSpawn(EntityType<?> type, int weight, int minCount, int maxCount) {
            Record count = minCount == maxCount ? new ConstantInt(minCount) : new UniformInt(minCount, maxCount);
            this.addSpawn(type, type.getCategory(), weight, (IntProvider)((Object)count));
            return this;
        }

        public Builder addSpawn(EntityType<?> type, int weight, IntProvider count) {
            this.addSpawn(type, type.getCategory(), weight, count);
            return this;
        }

        @Deprecated
        public Builder addSpawn(EntityType<?> type, MobCategory category, int weight, IntProvider count) {
            this.forCategory(category).add(new SpawnerData(type, count), weight);
            return this;
        }

        public Builder addAllSpawns(MobCategory category, WeightedList<SpawnerData> spawns) {
            this.forCategory(category).addAll(spawns);
            return this;
        }

        public Builder noSpawns(MobCategory category) {
            this.spawnsByCategory.put(category, WeightedList.builder());
            return this;
        }

        public Builder dontOverride(MobCategory category) {
            this.spawnsByCategory.remove(category);
            return this;
        }

        public Builder addMobSpawnCost(EntityType<?> type, double charge, double energyBudget) {
            this.mobSpawnCosts.put(type, new MobSpawnCost(energyBudget, charge));
            return this;
        }

        public Builder addAllCosts(Map<EntityType<?>, MobSpawnCost> costs) {
            this.mobSpawnCosts.putAll(costs);
            return this;
        }

        public MobSpawnSettings build() {
            return new MobSpawnSettings((Map)this.spawnsByCategory.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> ((WeightedList.Builder)e.getValue()).build())), (Map<EntityType<?>, MobSpawnCost>)ImmutableMap.copyOf(this.mobSpawnCosts));
        }

        private WeightedList.Builder<SpawnerData> forCategory(MobCategory category) {
            return this.spawnsByCategory.computeIfAbsent(category, mobCategory -> WeightedList.builder());
        }
    }
}

