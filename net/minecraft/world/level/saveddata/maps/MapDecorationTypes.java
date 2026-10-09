/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.saveddata.maps;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public class MapDecorationTypes {
    public static final Holder<MapDecorationType> PLAYER = MapDecorationTypes.register("player", "player", false, true);
    public static final Holder<MapDecorationType> FRAME = MapDecorationTypes.register("frame", "frame", true, true);
    public static final Holder<MapDecorationType> RED_MARKER = MapDecorationTypes.register("red_marker", "red_marker", false, true);
    public static final Holder<MapDecorationType> BLUE_MARKER = MapDecorationTypes.register("blue_marker", "blue_marker", false, true);
    public static final Holder<MapDecorationType> TARGET_X = MapDecorationTypes.register("target_x", "target_x", true, false);
    public static final Holder<MapDecorationType> TARGET_POINT = MapDecorationTypes.register("target_point", "target_point", true, false);
    public static final Holder<MapDecorationType> PLAYER_OFF_MAP = MapDecorationTypes.register("player_off_map", "player_off_map", false, true);
    public static final Holder<MapDecorationType> PLAYER_OFF_LIMITS = MapDecorationTypes.register("player_off_limits", "player_off_limits", false, true);
    public static final Holder<MapDecorationType> WOODLAND_MANSION = MapDecorationTypes.register("mansion", "woodland_mansion", true, false);
    public static final Holder<MapDecorationType> OCEAN_MONUMENT = MapDecorationTypes.register("monument", "ocean_monument", true, false);
    public static final Holder<MapDecorationType> WHITE_BANNER = MapDecorationTypes.register("banner_white", "white_banner", true, true);
    public static final Holder<MapDecorationType> ORANGE_BANNER = MapDecorationTypes.register("banner_orange", "orange_banner", true, true);
    public static final Holder<MapDecorationType> MAGENTA_BANNER = MapDecorationTypes.register("banner_magenta", "magenta_banner", true, true);
    public static final Holder<MapDecorationType> LIGHT_BLUE_BANNER = MapDecorationTypes.register("banner_light_blue", "light_blue_banner", true, true);
    public static final Holder<MapDecorationType> YELLOW_BANNER = MapDecorationTypes.register("banner_yellow", "yellow_banner", true, true);
    public static final Holder<MapDecorationType> LIME_BANNER = MapDecorationTypes.register("banner_lime", "lime_banner", true, true);
    public static final Holder<MapDecorationType> PINK_BANNER = MapDecorationTypes.register("banner_pink", "pink_banner", true, true);
    public static final Holder<MapDecorationType> GRAY_BANNER = MapDecorationTypes.register("banner_gray", "gray_banner", true, true);
    public static final Holder<MapDecorationType> LIGHT_GRAY_BANNER = MapDecorationTypes.register("banner_light_gray", "light_gray_banner", true, true);
    public static final Holder<MapDecorationType> CYAN_BANNER = MapDecorationTypes.register("banner_cyan", "cyan_banner", true, true);
    public static final Holder<MapDecorationType> PURPLE_BANNER = MapDecorationTypes.register("banner_purple", "purple_banner", true, true);
    public static final Holder<MapDecorationType> BLUE_BANNER = MapDecorationTypes.register("banner_blue", "blue_banner", true, true);
    public static final Holder<MapDecorationType> BROWN_BANNER = MapDecorationTypes.register("banner_brown", "brown_banner", true, true);
    public static final Holder<MapDecorationType> GREEN_BANNER = MapDecorationTypes.register("banner_green", "green_banner", true, true);
    public static final Holder<MapDecorationType> RED_BANNER = MapDecorationTypes.register("banner_red", "red_banner", true, true);
    public static final Holder<MapDecorationType> BLACK_BANNER = MapDecorationTypes.register("banner_black", "black_banner", true, true);
    public static final Holder<MapDecorationType> RED_X = MapDecorationTypes.register("red_x", "red_x", true, false);
    public static final Holder<MapDecorationType> DESERT_VILLAGE = MapDecorationTypes.register("village_desert", "desert_village", true, false);
    public static final Holder<MapDecorationType> PLAINS_VILLAGE = MapDecorationTypes.register("village_plains", "plains_village", true, false);
    public static final Holder<MapDecorationType> SAVANNA_VILLAGE = MapDecorationTypes.register("village_savanna", "savanna_village", true, false);
    public static final Holder<MapDecorationType> SNOWY_VILLAGE = MapDecorationTypes.register("village_snowy", "snowy_village", true, false);
    public static final Holder<MapDecorationType> TAIGA_VILLAGE = MapDecorationTypes.register("village_taiga", "taiga_village", true, false);
    public static final Holder<MapDecorationType> JUNGLE_TEMPLE = MapDecorationTypes.register("jungle_temple", "jungle_temple", true, false);
    public static final Holder<MapDecorationType> SWAMP_HUT = MapDecorationTypes.register("swamp_hut", "swamp_hut", true, false);
    public static final Holder<MapDecorationType> TRIAL_CHAMBERS = MapDecorationTypes.register("trial_chambers", "trial_chambers", true, false);
    public static final Holder<MapDecorationType> ABANDONED_CAMP = MapDecorationTypes.register("abandoned_camp", "abandoned_camp", true, false);
    public static final Holder<MapDecorationType> ANCIENT_CITY = MapDecorationTypes.register("ancient_city", "ancient_city", true, false);
    public static final Holder<MapDecorationType> DESERT_PYRAMID = MapDecorationTypes.register("desert_pyramid", "desert_pyramid", true, false);
    public static final Holder<MapDecorationType> MINESHAFT = MapDecorationTypes.register("mineshaft", "mineshaft", true, false);
    public static final Holder<MapDecorationType> OCEAN_RUIN_WARM = MapDecorationTypes.register("ocean_ruin_warm", "warm_ocean_ruins", true, false);

    public static Holder<MapDecorationType> bootstrap(Registry<MapDecorationType> registry) {
        return PLAYER;
    }

    private static Holder<MapDecorationType> register(String name, String assetName, boolean showOnItemFrame, boolean trackCount) {
        ResourceKey<MapDecorationType> key = ResourceKey.create(Registries.MAP_DECORATION_TYPE, Identifier.withDefaultNamespace(name));
        MapDecorationType type = new MapDecorationType(Identifier.withDefaultNamespace(assetName), showOnItemFrame, trackCount);
        return Registry.registerForHolder(BuiltInRegistries.MAP_DECORATION_TYPE, key, type);
    }
}

