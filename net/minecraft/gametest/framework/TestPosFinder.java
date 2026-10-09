/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import java.util.stream.Stream;
import net.minecraft.core.GlobalPos;

@FunctionalInterface
public interface TestPosFinder {
    public Stream<GlobalPos> findTestPos();
}

