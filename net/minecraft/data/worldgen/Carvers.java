/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.TrapezoidFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.VeryBiasedToBottomInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public interface Carvers {
    public static final ResourceKey<WorldCarver> CAVE = Carvers.createKey("cave");
    public static final ResourceKey<WorldCarver> CAVE_EXTRA_UNDERGROUND = Carvers.createKey("cave_extra_underground");
    public static final ResourceKey<WorldCarver> CANYON = Carvers.createKey("canyon");
    public static final ResourceKey<WorldCarver> NETHER_CAVE = Carvers.createKey("nether_cave");

    private static ResourceKey<WorldCarver> createKey(String name) {
        return ResourceKey.create(Registries.CARVER, Identifier.withDefaultNamespace(name));
    }

    public static void bootstrap(BootstrapContext<WorldCarver> context) {
        context.register(CAVE, new CaveWorldCarver(0.15f, UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(180)), VeryBiasedToBottomInt.of(0, 14), TrapezoidFloat.of(0.0f, 3.0f, 1.0f), true, UniformFloat.of(0.1f, 0.9f), UniformFloat.of(0.7f, 1.4f), UniformFloat.of(0.8f, 1.3f), ConstantFloat.of(1.0f), UniformFloat.of(-1.0f, -0.4f)));
        context.register(CAVE_EXTRA_UNDERGROUND, new CaveWorldCarver(0.07f, UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(47)), VeryBiasedToBottomInt.of(0, 14), TrapezoidFloat.of(0.0f, 3.0f, 1.0f), true, UniformFloat.of(0.1f, 0.9f), UniformFloat.of(0.7f, 1.4f), UniformFloat.of(0.8f, 1.3f), ConstantFloat.of(1.0f), UniformFloat.of(-1.0f, -0.4f)));
        context.register(CANYON, new CanyonWorldCarver(0.01f, UniformHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(67)), UniformFloat.of(-0.125f, 0.125f), new CanyonWorldCarver.Shape(UniformFloat.of(0.75f, 1.0f), TrapezoidFloat.of(0.0f, 6.0f, 2.0f), 3, UniformFloat.of(0.75f, 1.0f), 1.0f, 0.0f, ConstantFloat.of(3.0f))));
        context.register(NETHER_CAVE, new CaveWorldCarver(0.2f, UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.belowTop(1)), VeryBiasedToBottomInt.of(0, 9), TrapezoidFloat.of(0.0f, 6.0f, 2.0f), false, ConstantFloat.of(0.5f), ConstantFloat.of(1.0f), ConstantFloat.of(1.0f), ConstantFloat.of(5.0f), ConstantFloat.of(-0.7f)));
    }
}

