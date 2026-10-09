/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface StructureTags {
    public static final TagKey<Structure> EYE_OF_ENDER_LOCATED = StructureTags.create("eye_of_ender_located");
    public static final TagKey<Structure> DOLPHIN_LOCATED = StructureTags.create("dolphin_located");
    public static final TagKey<Structure> ON_WOODLAND_MANSION_MAPS = StructureTags.create("on_woodland_mansion_maps");
    public static final TagKey<Structure> ON_OCEAN_MONUMENT_MAPS = StructureTags.create("on_ocean_monument_maps");
    public static final TagKey<Structure> ON_SAVANNA_VILLAGE_MAPS = StructureTags.create("on_savanna_village_maps");
    public static final TagKey<Structure> ON_DESERT_VILLAGE_MAPS = StructureTags.create("on_desert_village_maps");
    public static final TagKey<Structure> ON_PLAINS_VILLAGE_MAPS = StructureTags.create("on_plains_village_maps");
    public static final TagKey<Structure> ON_TAIGA_VILLAGE_MAPS = StructureTags.create("on_taiga_village_maps");
    public static final TagKey<Structure> ON_SNOWY_VILLAGE_MAPS = StructureTags.create("on_snowy_village_maps");
    public static final TagKey<Structure> ON_JUNGLE_PYRAMID_MAPS = StructureTags.create("on_jungle_pyramid_maps");
    public static final TagKey<Structure> ON_SWAMP_HUT_MAPS = StructureTags.create("on_swamp_hut_maps");
    public static final TagKey<Structure> ON_TREASURE_MAPS = StructureTags.create("on_treasure_maps");
    public static final TagKey<Structure> ON_BURIED_TRIAL_CHAMBERS_MAPS = StructureTags.create("on_buried_trial_chambers_maps");
    public static final TagKey<Structure> ON_ANCIENT_CITY_MAPS = StructureTags.create("on_ancient_city_maps");
    public static final TagKey<Structure> ON_MINESHAFT_MAPS = StructureTags.create("on_mineshaft_maps");
    public static final TagKey<Structure> ON_DESERT_PYRAMID_MAPS = StructureTags.create("on_desert_pyramid_maps");
    public static final TagKey<Structure> ON_OCEAN_RUIN_WARM_MAPS = StructureTags.create("on_ocean_ruin_warm_maps");
    public static final TagKey<Structure> CATS_SPAWN_IN = StructureTags.create("cats_spawn_in");
    public static final TagKey<Structure> CATS_SPAWN_AS_BLACK = StructureTags.create("cats_spawn_as_black");
    public static final TagKey<Structure> VILLAGE = StructureTags.create("village");
    public static final TagKey<Structure> MINESHAFT = StructureTags.create("mineshaft");
    public static final TagKey<Structure> SHIPWRECK = StructureTags.create("shipwreck");
    public static final TagKey<Structure> RUINED_PORTAL = StructureTags.create("ruined_portal");
    public static final TagKey<Structure> OCEAN_RUIN = StructureTags.create("ocean_ruin");
    public static final TagKey<Structure> ABANDONED_CAMP = StructureTags.create("abandoned_camp");
    public static final TagKey<Structure> ON_ABANDONED_CAMP_BAMBOO_JUNGLE_MAPS = StructureTags.create("on_abandoned_camp_bamboo_jungle");
    public static final TagKey<Structure> ON_ABANDONED_CAMP_CHERRY_GROVE_MAPS = StructureTags.create("on_abandoned_camp_cherry_grove");
    public static final TagKey<Structure> ON_ABANDONED_CAMP_BIRCH_FOREST_MAPS = StructureTags.create("on_abandoned_camp_birch_forest");
    public static final TagKey<Structure> ON_ABANDONED_CAMP_DAPPLED_FOREST_MAPS = StructureTags.create("on_abandoned_camp_dappled_forest");
    public static final TagKey<Structure> ON_ABANDONED_CAMP_FLOWER_FOREST_MAPS = StructureTags.create("on_abandoned_camp_flower_forest");
    public static final TagKey<Structure> ON_ABANDONED_CAMP_PALE_GARDEN_MAPS = StructureTags.create("on_abandoned_camp_pale_garden");
    public static final TagKey<Structure> ON_ABANDONED_CAMP_SWAMP_MAPS = StructureTags.create("on_abandoned_camp_swamp");
    public static final TagKey<Structure> ON_ABANDONED_CAMP_WINDSWEPT_FOREST_MAPS = StructureTags.create("on_abandoned_camp_windswept_forest");

    private static TagKey<Structure> create(String name) {
        return TagKey.create(Registries.STRUCTURE, Identifier.withDefaultNamespace(name));
    }
}

