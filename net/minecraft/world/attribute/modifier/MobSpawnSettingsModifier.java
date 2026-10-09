/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

public interface MobSpawnSettingsModifier
extends AttributeModifier<MobSpawnSettings, MobSpawnSettings> {
    public static MobSpawnSettingsModifier overlay() {
        return Overlay.INSTANCE;
    }

    @Override
    default public Codec<MobSpawnSettings> argumentCodec(EnvironmentAttribute<MobSpawnSettings> attribute) {
        return attribute.valueCodec();
    }

    @Override
    default public LerpFunction<MobSpawnSettings> argumentKeyframeLerp(EnvironmentAttribute<MobSpawnSettings> attribute) {
        return attribute.type().keyframeLerp();
    }

    public record Overlay() implements MobSpawnSettingsModifier
    {
        private static final Overlay INSTANCE = new Overlay();
        private static final MobCategory[] ALL_CATEGORIES = MobCategory.values();

        @Override
        public MobSpawnSettings apply(MobSpawnSettings first, MobSpawnSettings second) {
            Set<MobCategory> firstCategories = first.definedCategories();
            Set<MobCategory> secondCategories = second.definedCategories();
            Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> firstSpawnCosts = first.allSpawnCosts();
            Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> secondSpawnCosts = second.allSpawnCosts();
            if (firstCategories.isEmpty() && firstSpawnCosts.isEmpty()) {
                return second;
            }
            if (secondCategories.isEmpty() && secondSpawnCosts.isEmpty()) {
                return first;
            }
            if (secondCategories.containsAll(firstCategories) && secondSpawnCosts.keySet().containsAll(firstSpawnCosts.keySet())) {
                return second;
            }
            MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
            for (MobCategory category : ALL_CATEGORIES) {
                WeightedList<MobSpawnSettings.SpawnerData> secondSpawns = second.getMobsInCategory(category);
                if (secondSpawns != null) {
                    builder.addAllSpawns(category, secondSpawns);
                    continue;
                }
                WeightedList<MobSpawnSettings.SpawnerData> firstSpawns = first.getMobsInCategory(category);
                if (firstSpawns == null) continue;
                builder.addAllSpawns(category, firstSpawns);
            }
            return builder.addAllCosts(firstSpawnCosts).addAllCosts(secondSpawnCosts).build();
        }
    }
}

