/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface BuiltinStructures {
    public static final ResourceKey<Structure> PILLAGER_OUTPOST = BuiltinStructures.createKey("pillager_outpost");
    public static final ResourceKey<Structure> MINESHAFT = BuiltinStructures.createKey("mineshaft");
    public static final ResourceKey<Structure> MINESHAFT_MESA = BuiltinStructures.createKey("mineshaft_mesa");
    public static final ResourceKey<Structure> WOODLAND_MANSION = BuiltinStructures.createKey("mansion");
    public static final ResourceKey<Structure> JUNGLE_TEMPLE = BuiltinStructures.createKey("jungle_pyramid");
    public static final ResourceKey<Structure> DESERT_PYRAMID = BuiltinStructures.createKey("desert_pyramid");
    public static final ResourceKey<Structure> IGLOO = BuiltinStructures.createKey("igloo");
    public static final ResourceKey<Structure> SHIPWRECK = BuiltinStructures.createKey("shipwreck");
    public static final ResourceKey<Structure> SHIPWRECK_BEACHED = BuiltinStructures.createKey("shipwreck_beached");
    public static final ResourceKey<Structure> SWAMP_HUT = BuiltinStructures.createKey("swamp_hut");
    public static final ResourceKey<Structure> STRONGHOLD = BuiltinStructures.createKey("stronghold");
    public static final ResourceKey<Structure> OCEAN_MONUMENT = BuiltinStructures.createKey("monument");
    public static final ResourceKey<Structure> OCEAN_RUIN_COLD = BuiltinStructures.createKey("ocean_ruin_cold");
    public static final ResourceKey<Structure> OCEAN_RUIN_WARM = BuiltinStructures.createKey("ocean_ruin_warm");
    public static final ResourceKey<Structure> FORTRESS = BuiltinStructures.createKey("fortress");
    public static final ResourceKey<Structure> NETHER_FOSSIL = BuiltinStructures.createKey("nether_fossil");
    public static final ResourceKey<Structure> END_CITY = BuiltinStructures.createKey("end_city");
    public static final ResourceKey<Structure> BURIED_TREASURE = BuiltinStructures.createKey("buried_treasure");
    public static final ResourceKey<Structure> BASTION_REMNANT = BuiltinStructures.createKey("bastion_remnant");
    public static final ResourceKey<Structure> VILLAGE_PLAINS = BuiltinStructures.createKey("village_plains");
    public static final ResourceKey<Structure> VILLAGE_DESERT = BuiltinStructures.createKey("village_desert");
    public static final ResourceKey<Structure> VILLAGE_SAVANNA = BuiltinStructures.createKey("village_savanna");
    public static final ResourceKey<Structure> VILLAGE_SNOWY = BuiltinStructures.createKey("village_snowy");
    public static final ResourceKey<Structure> VILLAGE_TAIGA = BuiltinStructures.createKey("village_taiga");
    public static final ResourceKey<Structure> RUINED_PORTAL_STANDARD = BuiltinStructures.createKey("ruined_portal");
    public static final ResourceKey<Structure> RUINED_PORTAL_DESERT = BuiltinStructures.createKey("ruined_portal_desert");
    public static final ResourceKey<Structure> RUINED_PORTAL_JUNGLE = BuiltinStructures.createKey("ruined_portal_jungle");
    public static final ResourceKey<Structure> RUINED_PORTAL_SWAMP = BuiltinStructures.createKey("ruined_portal_swamp");
    public static final ResourceKey<Structure> RUINED_PORTAL_MOUNTAIN = BuiltinStructures.createKey("ruined_portal_mountain");
    public static final ResourceKey<Structure> RUINED_PORTAL_OCEAN = BuiltinStructures.createKey("ruined_portal_ocean");
    public static final ResourceKey<Structure> RUINED_PORTAL_NETHER = BuiltinStructures.createKey("ruined_portal_nether");
    public static final ResourceKey<Structure> ANCIENT_CITY = BuiltinStructures.createKey("ancient_city");
    public static final ResourceKey<Structure> TRAIL_RUINS = BuiltinStructures.createKey("trail_ruins");
    public static final ResourceKey<Structure> TRIAL_CHAMBERS = BuiltinStructures.createKey("trial_chambers");
    public static final ResourceKey<Structure> ABANDONED_CAMP_BAMBOO_JUNGLE = BuiltinStructures.createKey("abandoned_camp_bamboo_jungle");
    public static final ResourceKey<Structure> ABANDONED_CAMP_BIRCH_FOREST = BuiltinStructures.createKey("abandoned_camp_birch_forest");
    public static final ResourceKey<Structure> ABANDONED_CAMP_CHERRY_GROVE = BuiltinStructures.createKey("abandoned_camp_cherry_grove");
    public static final ResourceKey<Structure> ABANDONED_CAMP_DAPPLED_FOREST = BuiltinStructures.createKey("abandoned_camp_dappled_forest");
    public static final ResourceKey<Structure> ABANDONED_CAMP_FLOWER_FOREST = BuiltinStructures.createKey("abandoned_camp_flower_forest");
    public static final ResourceKey<Structure> ABANDONED_CAMP_FOREST = BuiltinStructures.createKey("abandoned_camp_forest");
    public static final ResourceKey<Structure> ABANDONED_CAMP_MEADOW = BuiltinStructures.createKey("abandoned_camp_meadow");
    public static final ResourceKey<Structure> ABANDONED_CAMP_OLD_GROWTH_BIRCH_FOREST = BuiltinStructures.createKey("abandoned_camp_old_growth_birch_forest");
    public static final ResourceKey<Structure> ABANDONED_CAMP_OLD_GROWTH_PINE_TAIGA = BuiltinStructures.createKey("abandoned_camp_old_growth_pine_taiga");
    public static final ResourceKey<Structure> ABANDONED_CAMP_OLD_GROWTH_SPRUCE_TAIGA = BuiltinStructures.createKey("abandoned_camp_old_growth_spruce_taiga");
    public static final ResourceKey<Structure> ABANDONED_CAMP_PALE_GARDEN = BuiltinStructures.createKey("abandoned_camp_pale_garden");
    public static final ResourceKey<Structure> ABANDONED_CAMP_SAVANNA = BuiltinStructures.createKey("abandoned_camp_savanna");
    public static final ResourceKey<Structure> ABANDONED_CAMP_SNOWY_TAIGA = BuiltinStructures.createKey("abandoned_camp_snowy_taiga");
    public static final ResourceKey<Structure> ABANDONED_CAMP_SPARSE_JUNGLE = BuiltinStructures.createKey("abandoned_camp_sparse_jungle");
    public static final ResourceKey<Structure> ABANDONED_CAMP_SWAMP = BuiltinStructures.createKey("abandoned_camp_swamp");
    public static final ResourceKey<Structure> ABANDONED_CAMP_TAIGA = BuiltinStructures.createKey("abandoned_camp_taiga");
    public static final ResourceKey<Structure> ABANDONED_CAMP_WINDSWEPT_FOREST = BuiltinStructures.createKey("abandoned_camp_windswept_forest");
    public static final ResourceKey<Structure> ABANDONED_CAMP_WOODED_BADLANDS = BuiltinStructures.createKey("abandoned_camp_wooded_badlands");

    private static ResourceKey<Structure> createKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, Identifier.withDefaultNamespace(name));
    }
}

