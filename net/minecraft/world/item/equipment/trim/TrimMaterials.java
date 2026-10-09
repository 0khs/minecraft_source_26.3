/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.equipment.trim;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public class TrimMaterials {
    public static final ResourceKey<TrimMaterial> QUARTZ = TrimMaterials.registryKey("quartz");
    public static final ResourceKey<TrimMaterial> IRON = TrimMaterials.registryKey("iron");
    public static final ResourceKey<TrimMaterial> NETHERITE = TrimMaterials.registryKey("netherite");
    public static final ResourceKey<TrimMaterial> REDSTONE = TrimMaterials.registryKey("redstone");
    public static final ResourceKey<TrimMaterial> COPPER = TrimMaterials.registryKey("copper");
    public static final ResourceKey<TrimMaterial> GOLD = TrimMaterials.registryKey("gold");
    public static final ResourceKey<TrimMaterial> EMERALD = TrimMaterials.registryKey("emerald");
    public static final ResourceKey<TrimMaterial> DIAMOND = TrimMaterials.registryKey("diamond");
    public static final ResourceKey<TrimMaterial> LAPIS = TrimMaterials.registryKey("lapis");
    public static final ResourceKey<TrimMaterial> AMETHYST = TrimMaterials.registryKey("amethyst");
    public static final ResourceKey<TrimMaterial> RESIN = TrimMaterials.registryKey("resin");

    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        TrimMaterials.register(context, QUARTZ, Style.EMPTY.withColor(14931140), Palette.QUARTZ);
        TrimMaterials.register(context, IRON, Style.EMPTY.withColor(0xECECEC), Palette.IRON);
        TrimMaterials.register(context, NETHERITE, Style.EMPTY.withColor(6445145), Palette.NETHERITE);
        TrimMaterials.register(context, REDSTONE, Style.EMPTY.withColor(9901575), Palette.REDSTONE);
        TrimMaterials.register(context, COPPER, Style.EMPTY.withColor(11823181), Palette.COPPER);
        TrimMaterials.register(context, GOLD, Style.EMPTY.withColor(14594349), Palette.GOLD);
        TrimMaterials.register(context, EMERALD, Style.EMPTY.withColor(1155126), Palette.EMERALD);
        TrimMaterials.register(context, DIAMOND, Style.EMPTY.withColor(7269586), Palette.DIAMOND);
        TrimMaterials.register(context, LAPIS, Style.EMPTY.withColor(4288151), Palette.LAPIS);
        TrimMaterials.register(context, AMETHYST, Style.EMPTY.withColor(10116294), Palette.AMETHYST);
        TrimMaterials.register(context, RESIN, Style.EMPTY.withColor(16545810), Palette.RESIN);
    }

    private static void register(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> registryKey, Style hoverTextStyle, Palette palette) {
        MutableComponent description = Component.translatable(Util.makeDescriptionId("trim_material", registryKey.identifier())).withStyle(hoverTextStyle);
        context.register(registryKey, new TrimMaterial(palette.id(), description));
    }

    private static ResourceKey<TrimMaterial> registryKey(String id) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, Identifier.withDefaultNamespace(id));
    }

    public static enum Palette {
        QUARTZ("quartz"),
        IRON("iron"),
        IRON_DARKER("iron_darker"),
        NETHERITE("netherite"),
        NETHERITE_DARKER("netherite_darker"),
        REDSTONE("redstone"),
        COPPER("copper"),
        COPPER_DARKER("copper_darker"),
        GOLD("gold"),
        GOLD_DARKER("gold_darker"),
        EMERALD("emerald"),
        DIAMOND("diamond"),
        DIAMOND_DARKER("diamond_darker"),
        LAPIS("lapis"),
        AMETHYST("amethyst"),
        RESIN("resin");

        private final String suffix;
        private final Identifier id;

        private Palette(String name) {
            this.suffix = name;
            this.id = Identifier.withDefaultNamespace("trim/" + name);
        }

        public String suffix() {
            return this.suffix;
        }

        public Identifier id() {
            return this.id;
        }
    }
}

