/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.alchemy.Potion;

public class PotionTags {
    public static final TagKey<Potion> TRADEABLE = PotionTags.create("tradeable");
    public static final TagKey<Potion> DOUSES_FIRE = PotionTags.create("douses_fire");
    public static final TagKey<Potion> HURTS_WATER_SENSITIVE_ENTITIES = PotionTags.create("hurts_water_sensitive_entities");
    public static final TagKey<Potion> EXTINGUISHES_ENTITIES = PotionTags.create("extinguishes_entities");
    public static final TagKey<Potion> REHYDRATES_AXOLOTLS = PotionTags.create("rehydrates_axolotls");

    private PotionTags() {
    }

    private static TagKey<Potion> create(String name) {
        return TagKey.create(Registries.POTION, Identifier.withDefaultNamespace(name));
    }
}

