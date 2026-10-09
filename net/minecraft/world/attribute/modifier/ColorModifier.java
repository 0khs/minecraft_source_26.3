/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.joml.Vector3fc
 *  org.joml.Vector4fc
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public interface ColorModifier<Subject, Argument>
extends AttributeModifier<Subject, Argument> {
    public static final ArgbModifier<Vector3fc> ALPHA_BLEND_RGB = ARGB::alphaBlend;
    public static final ArgbModifier<Vector4fc> ALPHA_BLEND_ARGB = ARGB::alphaBlend;
    public static final RgbModifier<Vector3fc> ADD_RGB = ARGB::addRgb;
    public static final RgbModifier<Vector3fc> SUBTRACT_RGB = ARGB::subtractRgb;
    public static final RgbModifier<Vector3fc> MULTIPLY_RGB = ARGB::multiply;
    public static final RgbModifier<Vector4fc> ADD_ARGB = ARGB::addRgb;
    public static final RgbModifier<Vector4fc> SUBTRACT_ARGB = ARGB::subtractRgb;
    public static final ArgbModifier<Vector4fc> MULTIPLY_ARGB = ARGB::multiply;
    public static final ColorModifier<Vector3fc, BlendToGray> BLEND_TO_GRAY_RGB = new ColorModifier<Vector3fc, BlendToGray>(){

        @Override
        public Vector3fc apply(Vector3fc subject, BlendToGray argument) {
            Vector3fc multipliedGreyscale = ARGB.scaleRGB(ARGB.greyscale(subject), argument.brightness);
            return ARGB.srgbLerp(argument.factor, subject, multipliedGreyscale);
        }

        @Override
        public Codec<BlendToGray> argumentCodec(EnvironmentAttribute<Vector3fc> type) {
            return BlendToGray.CODEC;
        }

        @Override
        public LerpFunction<BlendToGray> argumentKeyframeLerp(EnvironmentAttribute<Vector3fc> type) {
            return BlendToGray::lerp;
        }
    };
    public static final ColorModifier<Vector4fc, BlendToGray> BLEND_TO_GRAY_ARGB = new ColorModifier<Vector4fc, BlendToGray>(){

        @Override
        public Vector4fc apply(Vector4fc subject, BlendToGray argument) {
            Vector4fc multipliedGreyscale = ARGB.scaleRGB(ARGB.greyscale(subject), argument.brightness);
            return ARGB.srgbLerp(argument.factor, subject, multipliedGreyscale);
        }

        @Override
        public Codec<BlendToGray> argumentCodec(EnvironmentAttribute<Vector4fc> type) {
            return BlendToGray.CODEC;
        }

        @Override
        public LerpFunction<BlendToGray> argumentKeyframeLerp(EnvironmentAttribute<Vector4fc> type) {
            return BlendToGray::lerp;
        }
    };

    @FunctionalInterface
    public static interface ArgbModifier<Subject>
    extends ColorModifier<Subject, Vector4fc> {
        @Override
        default public Codec<Vector4fc> argumentCodec(EnvironmentAttribute<Subject> type) {
            return ExtraCodecs.STRING_ARGB_VEC4_COLOR;
        }

        @Override
        default public LerpFunction<Vector4fc> argumentKeyframeLerp(EnvironmentAttribute<Subject> type) {
            return LerpFunction.ofColorVec4();
        }
    }

    public static interface RgbModifier<Subject>
    extends ColorModifier<Subject, Vector3fc> {
        @Override
        default public Codec<Vector3fc> argumentCodec(EnvironmentAttribute<Subject> type) {
            return ExtraCodecs.STRING_RGB_VEC3_COLOR;
        }

        @Override
        default public LerpFunction<Vector3fc> argumentKeyframeLerp(EnvironmentAttribute<Subject> type) {
            return LerpFunction.ofColorVec3();
        }
    }

    public record BlendToGray(float brightness, float factor) {
        public static final Codec<BlendToGray> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("brightness").forGetter(BlendToGray::brightness), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("factor").forGetter(BlendToGray::factor)).apply((Applicative)i, BlendToGray::new));

        public static BlendToGray lerp(float alpha, BlendToGray from, BlendToGray to) {
            return new BlendToGray(Mth.lerp(alpha, from.brightness, to.brightness), Mth.lerp(alpha, from.factor, to.factor));
        }
    }
}

