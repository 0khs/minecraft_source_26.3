/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P5
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public abstract class AbstractSpreadingStructurePlacement
implements StructurePlacement {
    private static final int HIGHLY_ARBITRARY_RANDOM_SALT = 10387320;
    private final Vec3i locateOffset;
    private final FrequencyReductionMethod frequencyReductionMethod;
    private final float frequency;
    private final int salt;
    private final Optional<ExclusionZone> exclusionZone;

    protected static <S extends AbstractSpreadingStructurePlacement> Products.P5<RecordCodecBuilder.Mu<S>, Vec3i, FrequencyReductionMethod, Float, Integer, Optional<ExclusionZone>> placementCodec(RecordCodecBuilder.Instance<S> i) {
        return i.group((App)Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", (Object)Vec3i.ZERO).forGetter(AbstractSpreadingStructurePlacement::locateOffset), (App)FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", (Object)FrequencyReductionMethod.DEFAULT).forGetter(AbstractSpreadingStructurePlacement::frequencyReductionMethod), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("frequency", (Object)Float.valueOf(1.0f)).forGetter(AbstractSpreadingStructurePlacement::frequency), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(AbstractSpreadingStructurePlacement::salt), (App)ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(AbstractSpreadingStructurePlacement::exclusionZone));
    }

    protected AbstractSpreadingStructurePlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, Optional<ExclusionZone> exclusionZone) {
        this.locateOffset = locateOffset;
        this.frequencyReductionMethod = frequencyReductionMethod;
        this.frequency = frequency;
        this.salt = salt;
        this.exclusionZone = exclusionZone;
    }

    public abstract MapCodec<? extends AbstractSpreadingStructurePlacement> codec();

    @Override
    public Vec3i locateOffset() {
        return this.locateOffset;
    }

    protected FrequencyReductionMethod frequencyReductionMethod() {
        return this.frequencyReductionMethod;
    }

    protected float frequency() {
        return this.frequency;
    }

    protected int salt() {
        return this.salt;
    }

    protected Optional<ExclusionZone> exclusionZone() {
        return this.exclusionZone;
    }

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int sourceX, int sourceZ) {
        return this.isPlacementChunk(state, sourceX, sourceZ) && this.applyAdditionalChunkRestrictions(sourceX, sourceZ, state.getLevelSeed()) && this.applyInteractionsWithOtherStructures(state, sourceX, sourceZ);
    }

    @Override
    public boolean applyAdditionalChunkRestrictions(int sourceX, int sourceZ, long levelSeed) {
        return !(this.frequency < 1.0f) || this.frequencyReductionMethod.shouldGenerate(levelSeed, this.salt, sourceX, sourceZ, this.frequency);
    }

    public boolean applyInteractionsWithOtherStructures(ChunkGeneratorStructureState state, int sourceX, int sourceZ) {
        return !this.exclusionZone.isPresent() || !this.exclusionZone.get().isPlacementForbidden(state, sourceX, sourceZ);
    }

    protected abstract boolean isPlacementChunk(ChunkGeneratorStructureState var1, int var2, int var3);

    private static boolean probabilityReducer(long seed, int salt, int sourceX, int sourceZ, float probability) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, salt, sourceX, sourceZ);
        return random.nextFloat() < probability;
    }

    private static boolean legacyProbabilityReducerWithDouble(long seed, int salt, int sourceX, int sourceZ, float probability) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(seed, sourceX, sourceZ);
        return random.nextDouble() < (double)probability;
    }

    private static boolean legacyArbitrarySaltProbabilityReducer(long seed, int salt, int sourceX, int sourceZ, float probability) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, sourceX, sourceZ, 10387320);
        return random.nextFloat() < probability;
    }

    private static boolean legacyPillagerOutpostReducer(long seed, int salt, int sourceX, int sourceZ, float probability) {
        int cx = sourceX >> 4;
        int cz = sourceZ >> 4;
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setSeed((long)(cx ^ cz << 4) ^ seed);
        random.nextInt();
        return random.nextInt((int)(1.0f / probability)) == 0;
    }

    public static enum FrequencyReductionMethod implements StringRepresentable
    {
        DEFAULT("default", AbstractSpreadingStructurePlacement::probabilityReducer),
        LEGACY_TYPE_1("legacy_type_1", AbstractSpreadingStructurePlacement::legacyPillagerOutpostReducer),
        LEGACY_TYPE_2("legacy_type_2", AbstractSpreadingStructurePlacement::legacyArbitrarySaltProbabilityReducer),
        LEGACY_TYPE_3("legacy_type_3", AbstractSpreadingStructurePlacement::legacyProbabilityReducerWithDouble);

        public static final Codec<FrequencyReductionMethod> CODEC;
        private final String name;
        private final FrequencyReducer reducer;

        private FrequencyReductionMethod(String name, FrequencyReducer reducer) {
            this.name = name;
            this.reducer = reducer;
        }

        public boolean shouldGenerate(long seed, int salt, int sourceX, int sourceZ, float probability) {
            return this.reducer.shouldGenerate(seed, salt, sourceX, sourceZ, probability);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(FrequencyReductionMethod::values);
        }
    }

    @Deprecated
    public record ExclusionZone(Holder<StructureSet> otherSet, int chunkCount) {
        public static final Codec<ExclusionZone> CODEC = RecordCodecBuilder.create(i -> i.group((App)RegistryCodecs.holder(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC, false).fieldOf("other_set").forGetter(ExclusionZone::otherSet), (App)Codec.intRange((int)1, (int)16).fieldOf("chunk_count").forGetter(ExclusionZone::chunkCount)).apply((Applicative)i, ExclusionZone::new));

        private boolean isPlacementForbidden(ChunkGeneratorStructureState state, int sourceX, int sourceZ) {
            return state.hasStructureChunkInRange(this.otherSet, sourceX, sourceZ, this.chunkCount);
        }
    }

    @FunctionalInterface
    public static interface FrequencyReducer {
        public boolean shouldGenerate(long var1, int var3, int var4, int var5, float var6);
    }
}

