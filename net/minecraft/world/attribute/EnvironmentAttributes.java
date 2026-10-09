/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  org.joml.Vector3fc
 *  org.joml.Vector4fc
 */
package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.TriState;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.AttributeRange;
import net.minecraft.world.attribute.AttributeTypes;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public interface EnvironmentAttributes {
    public static final EnvironmentAttribute<Vector3fc> FOG_COLOR = EnvironmentAttributes.register("visual/fog_color", EnvironmentAttribute.builder(AttributeTypes.RGB_COLOR).defaultValue((Vector3fc)ARGB.vector3fFromRGB24(0)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> FOG_START_DISTANCE = EnvironmentAttributes.register("visual/fog_start_distance", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(0.0f)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> FOG_END_DISTANCE = EnvironmentAttributes.register("visual/fog_end_distance", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(1024.0f)).valueRange(AttributeRange.NON_NEGATIVE_FLOAT).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> SKY_FOG_END_DISTANCE = EnvironmentAttributes.register("visual/sky_fog_end_distance", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(512.0f)).valueRange(AttributeRange.NON_NEGATIVE_FLOAT).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> CLOUD_FOG_END_DISTANCE = EnvironmentAttributes.register("visual/cloud_fog_end_distance", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(2048.0f)).valueRange(AttributeRange.NON_NEGATIVE_FLOAT).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Vector3fc> WATER_FOG_COLOR = EnvironmentAttributes.register("visual/water_fog_color", EnvironmentAttribute.builder(AttributeTypes.RGB_COLOR).defaultValue((Vector3fc)ARGB.vector3fFromRGB24(-16448205)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> WATER_FOG_START_DISTANCE = EnvironmentAttributes.register("visual/water_fog_start_distance", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(-8.0f)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> WATER_FOG_END_DISTANCE = EnvironmentAttributes.register("visual/water_fog_end_distance", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(96.0f)).valueRange(AttributeRange.NON_NEGATIVE_FLOAT).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Vector3fc> SKY_COLOR = EnvironmentAttributes.register("visual/sky_color", EnvironmentAttribute.builder(AttributeTypes.RGB_COLOR).defaultValue((Vector3fc)ARGB.vector3fFromRGB24(0)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Vector4fc> SUNRISE_SUNSET_COLOR = EnvironmentAttributes.register("visual/sunrise_sunset_color", EnvironmentAttribute.builder(AttributeTypes.ARGB_COLOR).defaultValue((Vector4fc)ARGB.vector4fFromARGB32(0)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Vector4fc> CLOUD_COLOR = EnvironmentAttributes.register("visual/cloud_color", EnvironmentAttribute.builder(AttributeTypes.ARGB_COLOR).defaultValue((Vector4fc)ARGB.vector4fFromARGB32(0)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> CLOUD_HEIGHT = EnvironmentAttributes.register("visual/cloud_height", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(192.33f)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> SUN_ANGLE = EnvironmentAttributes.register("visual/sun_angle", EnvironmentAttribute.builder(AttributeTypes.ANGLE_DEGREES).defaultValue(Float.valueOf(0.0f)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> MOON_ANGLE = EnvironmentAttributes.register("visual/moon_angle", EnvironmentAttribute.builder(AttributeTypes.ANGLE_DEGREES).defaultValue(Float.valueOf(0.0f)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> STAR_ANGLE = EnvironmentAttributes.register("visual/star_angle", EnvironmentAttribute.builder(AttributeTypes.ANGLE_DEGREES).defaultValue(Float.valueOf(0.0f)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<MoonPhase> MOON_PHASE = EnvironmentAttributes.register("visual/moon_phase", EnvironmentAttribute.builder(AttributeTypes.MOON_PHASE).defaultValue(MoonPhase.FULL_MOON).syncable());
    public static final EnvironmentAttribute<Float> STAR_BRIGHTNESS = EnvironmentAttributes.register("visual/star_brightness", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(0.0f)).valueRange(AttributeRange.UNIT_FLOAT).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Vector3fc> BLOCK_LIGHT_TINT = EnvironmentAttributes.register("visual/block_light_tint", EnvironmentAttribute.builder(AttributeTypes.RGB_COLOR).defaultValue((Vector3fc)ARGB.vector3fFromRGB24(-10100)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Vector3fc> SKY_LIGHT_COLOR = EnvironmentAttributes.register("visual/sky_light_color", EnvironmentAttribute.builder(AttributeTypes.RGB_COLOR).defaultValue((Vector3fc)ARGB.vector3fFromRGB24(-1)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Float> SKY_LIGHT_FACTOR = EnvironmentAttributes.register("visual/sky_light_factor", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(1.0f)).valueRange(AttributeRange.UNIT_FLOAT).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Vector3fc> NIGHT_VISION_COLOR = EnvironmentAttributes.register("visual/night_vision_color", EnvironmentAttribute.builder(AttributeTypes.RGB_COLOR).defaultValue((Vector3fc)ARGB.vector3fFromRGB24(-6710887)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<Vector3fc> AMBIENT_LIGHT_COLOR = EnvironmentAttributes.register("visual/ambient_light_color", EnvironmentAttribute.builder(AttributeTypes.RGB_COLOR).defaultValue((Vector3fc)ARGB.vector3fFromRGB24(-16777216)).spatiallyInterpolated().syncable());
    public static final EnvironmentAttribute<ParticleOptions> DEFAULT_DRIPSTONE_PARTICLE = EnvironmentAttributes.register("visual/default_dripstone_particle", EnvironmentAttribute.builder(AttributeTypes.PARTICLE).defaultValue(ParticleTypes.DRIPPING_DRIPSTONE_WATER).syncable());
    public static final EnvironmentAttribute<List<AmbientParticle>> AMBIENT_PARTICLES = EnvironmentAttributes.register("visual/ambient_particles", EnvironmentAttribute.builder(AttributeTypes.AMBIENT_PARTICLES).defaultValue(List.of()).syncable());
    public static final EnvironmentAttribute<BackgroundMusic> BACKGROUND_MUSIC = EnvironmentAttributes.register("audio/background_music", EnvironmentAttribute.builder(AttributeTypes.BACKGROUND_MUSIC).defaultValue(BackgroundMusic.EMPTY).syncable());
    public static final EnvironmentAttribute<Float> MUSIC_VOLUME = EnvironmentAttributes.register("audio/music_volume", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(1.0f)).valueRange(AttributeRange.UNIT_FLOAT).syncable());
    public static final EnvironmentAttribute<AmbientSounds> AMBIENT_SOUNDS = EnvironmentAttributes.register("audio/ambient_sounds", EnvironmentAttribute.builder(AttributeTypes.AMBIENT_SOUNDS).defaultValue(AmbientSounds.EMPTY).syncable());
    public static final EnvironmentAttribute<Boolean> FIREFLY_BUSH_SOUNDS = EnvironmentAttributes.register("audio/firefly_bush_sounds", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(false).syncable());
    public static final EnvironmentAttribute<Float> SKY_LIGHT_LEVEL = EnvironmentAttributes.register("gameplay/sky_light_level", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(15.0f)).valueRange(AttributeRange.ofFloat(0.0f, 15.0f)).notPositional().syncable());
    public static final EnvironmentAttribute<Boolean> CAN_START_RAID = EnvironmentAttributes.register("gameplay/can_start_raid", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(true));
    public static final EnvironmentAttribute<Boolean> WATER_EVAPORATES = EnvironmentAttributes.register("gameplay/water_evaporates", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(false).syncable());
    public static final EnvironmentAttribute<BedRule> BED_RULE = EnvironmentAttributes.register("gameplay/bed_rule", EnvironmentAttribute.builder(AttributeTypes.BED_RULE).defaultValue(BedRule.CAN_SLEEP_WHEN_DARK));
    public static final EnvironmentAttribute<BedRule> STRAW_BED_RULE = EnvironmentAttributes.register("gameplay/straw_bed_rule", EnvironmentAttribute.builder(AttributeTypes.BED_RULE).defaultValue(BedRule.DESTROY_ON_LEAVE));
    public static final EnvironmentAttribute<Boolean> RESPAWN_ANCHOR_WORKS = EnvironmentAttributes.register("gameplay/respawn_anchor_works", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<Boolean> NETHER_PORTAL_SPAWNS_PIGLINS = EnvironmentAttributes.register("gameplay/nether_portal_spawns_piglin", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<Boolean> FAST_LAVA = EnvironmentAttributes.register("gameplay/fast_lava", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(false).notPositional().syncable());
    public static final EnvironmentAttribute<Boolean> INCREASED_FIRE_BURNOUT = EnvironmentAttributes.register("gameplay/increased_fire_burnout", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<TriState> EYEBLOSSOM_OPEN = EnvironmentAttributes.register("gameplay/eyeblossom_open", EnvironmentAttribute.builder(AttributeTypes.TRI_STATE).defaultValue(TriState.DEFAULT));
    public static final EnvironmentAttribute<Float> TURTLE_EGG_HATCH_CHANCE = EnvironmentAttributes.register("gameplay/turtle_egg_hatch_chance", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(0.002f)).valueRange(AttributeRange.UNIT_FLOAT));
    public static final EnvironmentAttribute<Boolean> PIGLINS_ZOMBIFY = EnvironmentAttributes.register("gameplay/piglins_zombify", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(true).syncable());
    public static final EnvironmentAttribute<Boolean> SNOW_GOLEM_MELTS = EnvironmentAttributes.register("gameplay/snow_golem_melts", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<Boolean> CREAKING_ACTIVE = EnvironmentAttributes.register("gameplay/creaking_active", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(false).syncable());
    public static final EnvironmentAttribute<Float> SURFACE_SLIME_SPAWN_CHANCE = EnvironmentAttributes.register("gameplay/surface_slime_spawn_chance", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(0.0f)).valueRange(AttributeRange.UNIT_FLOAT));
    public static final EnvironmentAttribute<Float> CAT_WAKING_UP_GIFT_CHANCE = EnvironmentAttributes.register("gameplay/cat_waking_up_gift_chance", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(0.0f)).valueRange(AttributeRange.UNIT_FLOAT));
    public static final EnvironmentAttribute<Boolean> BEES_STAY_IN_HIVE = EnvironmentAttributes.register("gameplay/bees_stay_in_hive", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<Boolean> MONSTERS_BURN = EnvironmentAttributes.register("gameplay/monsters_burn", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(false));
    public static final EnvironmentAttribute<Boolean> CAN_PILLAGER_PATROL_SPAWN = EnvironmentAttributes.register("gameplay/can_pillager_patrol_spawn", EnvironmentAttribute.builder(AttributeTypes.BOOLEAN).defaultValue(true));
    public static final EnvironmentAttribute<MobSpawnSettings> NATURAL_MOB_SPAWNS = EnvironmentAttributes.register("gameplay/natural_mob_spawns", EnvironmentAttribute.builder(AttributeTypes.MOB_SPAWN_SETTINGS).defaultValue(MobSpawnSettings.EMPTY).fullResolutionBiomes());
    public static final EnvironmentAttribute<Float> CREATURE_WORLD_GEN_SPAWN_PROBABILITY = EnvironmentAttributes.register("gameplay/creature_world_gen_spawn_probability", EnvironmentAttribute.builder(AttributeTypes.FLOAT).defaultValue(Float.valueOf(0.1f)).valueRange(AttributeRange.UNIT_FLOAT_EPSILON));
    public static final EnvironmentAttribute<Activity> VILLAGER_ACTIVITY = EnvironmentAttributes.register("gameplay/villager_activity", EnvironmentAttribute.builder(AttributeTypes.ACTIVITY).defaultValue(Activity.IDLE));
    public static final EnvironmentAttribute<Activity> BABY_VILLAGER_ACTIVITY = EnvironmentAttributes.register("gameplay/baby_villager_activity", EnvironmentAttribute.builder(AttributeTypes.ACTIVITY).defaultValue(Activity.IDLE));
    public static final Codec<EnvironmentAttribute<?>> CODEC = BuiltInRegistries.ENVIRONMENT_ATTRIBUTE.byNameCodec();

    public static EnvironmentAttribute<?> bootstrap(Registry<EnvironmentAttribute<?>> registry) {
        return RESPAWN_ANCHOR_WORKS;
    }

    private static <Value> EnvironmentAttribute<Value> register(String id, EnvironmentAttribute.Builder<Value> attributeBuilder) {
        EnvironmentAttribute<Value> attribute = attributeBuilder.build();
        Registry.register(BuiltInRegistries.ENVIRONMENT_ATTRIBUTE, Identifier.withDefaultNamespace(id), attribute);
        return attribute;
    }
}

