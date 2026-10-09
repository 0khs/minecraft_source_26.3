/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.material.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;

public record BiomeCondition(HolderSet<Biome> biomes) implements MaterialCondition
{
    public static final MapCodec<BiomeCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biome_is").forGetter(BiomeCondition::biomes)).apply((Applicative)i, BiomeCondition::new));

    public MapCodec<BiomeCondition> codec() {
        return CODEC;
    }

    @Override
    public ConditionEvaluator compile(MaterialRuleContext ruleContext) {
        Set<Holder<Biome>> possibleBiomes = ruleContext.possibleBiomes();
        if (possibleBiomes != null) {
            if (this.canNeverMatch(possibleBiomes)) {
                return () -> false;
            }
            if (this.willAlwaysMatch(possibleBiomes)) {
                return () -> true;
            }
        }
        return new MaterialRuleContext.LazyYCondition(this, ruleContext){
            final /* synthetic */ BiomeCondition this$0;
            {
                BiomeCondition biomeCondition = this$0;
                Objects.requireNonNull(biomeCondition);
                this.this$0 = biomeCondition;
                super(context);
            }

            @Override
            protected boolean compute() {
                return this.this$0.biomes.contains(this.context.getBiome());
            }
        };
    }

    private boolean canNeverMatch(Set<Holder<Biome>> possibleBiomes) {
        for (Holder holder : this.biomes) {
            if (!possibleBiomes.contains(holder)) continue;
            return false;
        }
        return true;
    }

    private boolean willAlwaysMatch(Set<Holder<Biome>> possibleBiomes) {
        for (Holder<Biome> possibleBiome : possibleBiomes) {
            if (this.biomes.contains(possibleBiome)) continue;
            return false;
        }
        return true;
    }
}

