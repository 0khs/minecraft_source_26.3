/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;

public interface TestFunctionLoader {
    public static final List<TestFunctionLoader> ALL_LOADERS = new ArrayList<TestFunctionLoader>();

    public static void registerLoader(TestFunctionLoader loader) {
        ALL_LOADERS.add(loader);
    }

    public static void runLoaders(Registry<Consumer<GameTestHelper>> registry) {
        for (TestFunctionLoader loader : ALL_LOADERS) {
            loader.load((key, function) -> Registry.register(registry, key, function));
        }
    }

    public void load(BiConsumer<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> var1);
}

