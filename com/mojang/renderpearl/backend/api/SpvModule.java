/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.renderpearl.backend.api;

import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.util.ShaderCompileException;
import java.nio.ByteBuffer;
import java.util.List;
import org.jspecify.annotations.Nullable;

public interface SpvModule
extends AutoCloseable {
    @Override
    public void close();

    public ByteBuffer spv();

    public ShaderType type();

    public Reflection reflect() throws ShaderCompileException;

    public @Nullable Reflection getReflectionInfoIfAvailable();

    public static interface Reflection {
        public List<InterfaceVariable> inputs();

        public List<InterfaceVariable> outputs();

        public List<Descriptor> descriptors(int var1);

        public List<Descriptor> descriptors();

        public List<PushConstant> pushConstants();

        public static interface Type {
            public int baseType();

            public int dimensions();

            public int vectorSize();

            public int arrayDimensions();

            public int arrayLength(int var1);
        }

        public static interface PushConstant {
            public int size();
        }

        public static interface Descriptor {
            public String name();

            public Type type();

            public int resourceType();

            public int descriptorSetIndex();

            public void descriptorSetIndex(int var1);

            public int binding();

            public void binding(int var1);
        }

        public static interface InterfaceVariable {
            public String name();

            public Type type();

            public int location();

            public void location(int var1);

            default public boolean hasDecoration(int decoration) {
                return this.decoration(decoration) != 0;
            }

            public int decoration(int var1);
        }
    }
}

