/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model;

import net.minecraft.resources.Identifier;

public interface ResolvableModel {
    public void resolveDependencies(Resolver var1);

    public static interface Resolver {
        public void markDependency(Identifier var1);
    }
}

