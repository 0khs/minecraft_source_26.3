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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.TriState;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.AttributeType;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public interface AttributeTypes {
    public static final AttributeType<Boolean> BOOLEAN = AttributeTypes.register("boolean", AttributeType.ofNotInterpolated(Codec.BOOL, AttributeModifier.BOOLEAN_LIBRARY));
    public static final AttributeType<TriState> TRI_STATE = AttributeTypes.register("tri_state", AttributeType.ofNotInterpolated(TriState.CODEC));
    public static final AttributeType<Float> FLOAT = AttributeTypes.register("float", AttributeType.ofInterpolated(Codec.FLOAT, AttributeModifier.FLOAT_LIBRARY, LerpFunction.ofFloat(), LerpFunction.ofFloat(), value -> value.floatValue(), null));
    public static final AttributeType<Float> ANGLE_DEGREES = AttributeTypes.register("angle_degrees", AttributeType.ofInterpolated(Codec.FLOAT, AttributeModifier.FLOAT_LIBRARY, LerpFunction.ofFloat(), LerpFunction.ofDegrees(90.0f), value -> value.floatValue(), null));
    public static final AttributeType<Vector3fc> RGB_COLOR = AttributeTypes.register("rgb_color", AttributeType.ofInterpolated(ExtraCodecs.STRING_RGB_VEC3_COLOR, AttributeModifier.RGB_COLOR_LIBRARY, LerpFunction.ofColorVec3()));
    public static final AttributeType<Vector4fc> ARGB_COLOR = AttributeTypes.register("argb_color", AttributeType.ofInterpolated(ExtraCodecs.STRING_ARGB_VEC4_COLOR, AttributeModifier.ARGB_COLOR_LIBRARY, LerpFunction.ofColorVec4()));
    public static final AttributeType<Integer> INTEGER = AttributeTypes.register("integer", AttributeType.ofInterpolated(Codec.INT, AttributeModifier.INTEGER_LIBRARY, LerpFunction.ofInteger(), LerpFunction.ofInteger(), value -> value.intValue(), value -> value));
    public static final AttributeType<MoonPhase> MOON_PHASE = AttributeTypes.register("moon_phase", AttributeType.ofNotInterpolated(MoonPhase.CODEC));
    public static final AttributeType<Activity> ACTIVITY = AttributeTypes.register("activity", AttributeType.ofNotInterpolated(BuiltInRegistries.ACTIVITY.byNameCodec()));
    public static final AttributeType<BedRule> BED_RULE = AttributeTypes.register("bed_rule", AttributeType.ofNotInterpolated(BedRule.CODEC));
    public static final AttributeType<ParticleOptions> PARTICLE = AttributeTypes.register("particle", AttributeType.ofNotInterpolated(ParticleTypes.CODEC));
    public static final AttributeType<List<AmbientParticle>> AMBIENT_PARTICLES = AttributeTypes.register("ambient_particles", AttributeType.ofInterpolated(AmbientParticle.CODEC.listOf(), AttributeModifier.listLibrary(), LerpFunction.ofListCrossFade((p, alpha) -> new AmbientParticle(p.particle(), p.probability() * alpha))));
    public static final AttributeType<BackgroundMusic> BACKGROUND_MUSIC = AttributeTypes.register("background_music", AttributeType.ofNotInterpolated(BackgroundMusic.CODEC));
    public static final AttributeType<AmbientSounds> AMBIENT_SOUNDS = AttributeTypes.register("ambient_sounds", AttributeType.ofNotInterpolated(AmbientSounds.CODEC));
    public static final AttributeType<MobSpawnSettings> MOB_SPAWN_SETTINGS = AttributeTypes.register("mob_spawn_settings", AttributeType.ofNotInterpolated(MobSpawnSettings.CODEC, AttributeModifier.MOB_SPAWN_SETTINGS_LIBRARY));
    public static final Codec<AttributeType<?>> CODEC = BuiltInRegistries.ATTRIBUTE_TYPE.byNameCodec();

    public static AttributeType<?> bootstrap(Registry<AttributeType<?>> registry) {
        return BOOLEAN;
    }

    public static <Value> AttributeType<Value> register(String name, AttributeType<Value> type) {
        Registry.register(BuiltInRegistries.ATTRIBUTE_TYPE, Identifier.withDefaultNamespace(name), type);
        return type;
    }
}

