/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public interface ListModifier<Element>
extends AttributeModifier<List<Element>, List<Element>> {
    public static <Element> ListModifier<Element> append() {
        return Append.INSTANCE;
    }

    @Override
    default public Codec<List<Element>> argumentCodec(EnvironmentAttribute<List<Element>> attribute) {
        return attribute.valueCodec();
    }

    @Override
    default public LerpFunction<List<Element>> argumentKeyframeLerp(EnvironmentAttribute<List<Element>> attribute) {
        return attribute.type().keyframeLerp();
    }

    public record Append<Element>() implements ListModifier<Element>
    {
        private static final Append<?> INSTANCE = new Append();

        @Override
        public List<Element> apply(List<Element> subject, List<Element> argument) {
            if (argument.isEmpty()) {
                return subject;
            }
            if (subject.isEmpty()) {
                return argument;
            }
            return Util.join(subject, argument);
        }
    }
}

