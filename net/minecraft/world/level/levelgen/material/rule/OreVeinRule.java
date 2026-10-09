/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.material.rule;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;

public record OreVeinRule(BlockState oreBlock, BlockState rawOreBlock, BlockState fillerBlock, float rawOreChance, DensityFunction density, DensityFunction richness, DensityFunction fillerGap) implements MaterialRule
{
    public static final float VEININESS_THRESHOLD = 0.4f;
    public static final int EDGE_ROUNDOFF_BEGIN = 20;
    public static final float MAX_EDGE_ROUNDOFF = 0.2f;
    public static final float VEIN_SOLIDNESS = 0.7f;
    public static final float MIN_RICHNESS = 0.1f;
    public static final float MAX_RICHNESS = 0.3f;
    public static final float MAX_RICHNESS_THRESHOLD = 0.6f;
    private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.02f;
    public static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3f;
    public static final MapCodec<OreVeinRule> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockState.CODEC.fieldOf("ore_block").forGetter(OreVeinRule::oreBlock), (App)BlockState.CODEC.fieldOf("raw_ore_block").forGetter(OreVeinRule::rawOreBlock), (App)BlockState.CODEC.fieldOf("filler_block").forGetter(OreVeinRule::fillerBlock), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("raw_ore_chance").forGetter(OreVeinRule::rawOreChance), (App)DensityFunction.CODEC.fieldOf("density").forGetter(OreVeinRule::density), (App)DensityFunction.CODEC.fieldOf("richness").forGetter(OreVeinRule::richness), (App)DensityFunction.CODEC.fieldOf("filler_gap").forGetter(OreVeinRule::fillerGap)).apply((Applicative)i, OreVeinRule::new));

    @Override
    public RuleEvaluator compile(MaterialRuleContext context) {
        if (SharedConstants.DEBUG_DISABLE_ORE_VEINS) {
            return (n, n2, n3) -> null;
        }
        BlockState defaultState = SharedConstants.DEBUG_ORE_VEINS ? Blocks.AIR.defaultBlockState() : null;
        MaterialRules.DensityGetter densitySampler = context.getDensitiesInChunk(this.density, true);
        MaterialRules.DensityGetter richnessSampler = context.getDensitiesInChunk(this.richness, true);
        MaterialRules.DensityGetter fillerGapSampler = context.getDensitiesInChunk(this.fillerGap, false);
        PositionalRandomFactory randomFactory = context.getOrCreateRandomFactory(Identifier.withDefaultNamespace("ore"));
        BlockState fillerBlock = SharedConstants.DEBUG_ORE_VEINS ? Blocks.OAK_BUTTON.defaultBlockState() : this.fillerBlock;
        return (blockX, blockY, blockZ) -> {
            float density = densitySampler.get();
            if (density <= 0.0f) {
                return defaultState;
            }
            RandomSource random = randomFactory.at(blockX, blockY, blockZ);
            if (random.nextFloat() > density) {
                return defaultState;
            }
            float richness = richnessSampler.get();
            if (random.nextFloat() < richness && fillerGapSampler.get() < 0.0f) {
                return random.nextFloat() < this.rawOreChance ? this.rawOreBlock : this.oreBlock;
            }
            return fillerBlock;
        };
    }

    public MapCodec<OreVeinRule> codec() {
        return CODEC;
    }

    public static enum VeinType {
        COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.RAW_COPPER_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
        IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

        private final BlockState oreBlock;
        private final BlockState rawOreBlock;
        private final BlockState fillerBlock;
        public final int minY;
        public final int maxY;

        private VeinType(BlockState oreBlock, BlockState rawOreBlock, BlockState fillerBlock, int minY, int maxY) {
            this.oreBlock = oreBlock;
            this.rawOreBlock = rawOreBlock;
            this.fillerBlock = fillerBlock;
            this.minY = minY;
            this.maxY = maxY;
        }

        public OreVeinRule create(DensityFunction density, DensityFunction richness, DensityFunction fillerGap) {
            return new OreVeinRule(this.oreBlock, this.rawOreBlock, this.fillerBlock, 0.02f, density, richness, fillerGap);
        }
    }
}

