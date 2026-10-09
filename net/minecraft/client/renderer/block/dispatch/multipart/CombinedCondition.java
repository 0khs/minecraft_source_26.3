/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 */
package net.minecraft.client.renderer.block.dispatch.multipart;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.renderer.block.dispatch.multipart.Condition;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;

public record CombinedCondition(Operation operation, List<Condition> terms) implements Condition
{
    @Override
    public <O, S extends StateHolder<O, S>> Predicate<S> instantiate(StateDefinition<O, S> definition) {
        return this.operation.apply(Lists.transform(this.terms, c -> c.instantiate(definition)));
    }

    public static enum Operation implements StringRepresentable
    {
        AND("AND"){

            @Override
            public <V> Predicate<V> apply(List<Predicate<V>> terms) {
                return Util.allOf(terms);
            }
        }
        ,
        OR("OR"){

            @Override
            public <V> Predicate<V> apply(List<Predicate<V>> terms) {
                return Util.anyOf(terms);
            }
        };

        public static final Codec<Operation> CODEC;
        private final String name;

        private Operation(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public abstract <V> Predicate<V> apply(List<Predicate<V>> var1);

        static {
            CODEC = StringRepresentable.fromEnum(Operation::values);
        }
    }
}

