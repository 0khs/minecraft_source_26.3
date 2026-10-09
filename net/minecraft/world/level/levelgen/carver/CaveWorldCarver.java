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
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public record CaveWorldCarver(float probability, HeightProvider y, IntProvider count, FloatProvider thickness, boolean weirdThicknessBias, FloatProvider roomVerticalRadiusMultiplier, FloatProvider horizontalRadiusMultiplier, FloatProvider verticalRadiusMultiplier, FloatProvider startVerticalRadiusMultiplier, FloatProvider floorLevel) implements WorldCarver
{
    public static final MapCodec<CaveWorldCarver> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").forGetter(c -> Float.valueOf(c.probability)), (App)HeightProvider.CODEC.fieldOf("y").forGetter(c -> c.y), (App)IntProviders.NON_NEGATIVE_CODEC.fieldOf("count").forGetter(c -> c.count), (App)FloatProviders.codec(0.0f).fieldOf("thickness").forGetter(c -> c.thickness), (App)Codec.BOOL.optionalFieldOf("weird_thickness_bias", (Object)false).forGetter(c -> c.weirdThicknessBias), (App)FloatProviders.CODEC.fieldOf("room_vertical_radius_multiplier").forGetter(c -> c.roomVerticalRadiusMultiplier), (App)FloatProviders.CODEC.fieldOf("horizontal_radius_multiplier").forGetter(c -> c.horizontalRadiusMultiplier), (App)FloatProviders.CODEC.fieldOf("vertical_radius_multiplier").forGetter(c -> c.verticalRadiusMultiplier), (App)FloatProviders.CODEC.optionalFieldOf("start_vertical_radius_multiplier", (Object)ConstantFloat.of(1.0f)).forGetter(c -> c.startVerticalRadiusMultiplier), (App)FloatProviders.codec(-1.0f, 1.0f).fieldOf("floor_level").forGetter(c -> c.floorLevel)).apply((Applicative)i, CaveWorldCarver::new));

    @Override
    public boolean isStartChunk(RandomSource random) {
        return random.nextFloat() <= this.probability;
    }

    @Override
    public boolean carve(WorldGenerationContext context, RandomSource random, ChunkPos chunkPos, ChunkPos sourceChunkPos, CarverOutput output) {
        int maxDistance = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
        int caveCount = this.count.sample(random);
        for (int cave = 0; cave < caveCount; ++cave) {
            double x = sourceChunkPos.getBlockX(random.nextInt(16));
            double y = this.y.sample(random, context);
            double z = sourceChunkPos.getBlockZ(random.nextInt(16));
            double horizontalRadiusMultiplier = this.horizontalRadiusMultiplier.sample(random);
            double verticalRadiusMultiplier = this.verticalRadiusMultiplier.sample(random);
            double startVerticalRadiusMultiplier = this.startVerticalRadiusMultiplier.sample(random);
            double floorLevel = this.floorLevel.sample(random);
            WorldCarver.CarveSkipChecker skipChecker = (xd, yd, zd, worldY) -> CaveWorldCarver.shouldSkip(xd, yd, zd, floorLevel);
            int tunnels = 1;
            if (random.nextInt(4) == 0) {
                double yScale = this.roomVerticalRadiusMultiplier.sample(random);
                float thickness = 1.0f + random.nextFloat() * 6.0f;
                this.createRoom(chunkPos, x, y, z, thickness, yScale, output, skipChecker);
                tunnels += random.nextInt(4);
            }
            for (int i = 0; i < tunnels; ++i) {
                float horizontalRotation = random.nextFloat() * ((float)Math.PI * 2);
                float verticalRotation = (random.nextFloat() - 0.5f) / 4.0f;
                float thickness = this.getThickness(random);
                int distance = maxDistance - random.nextInt(maxDistance / 4);
                boolean initialStep = false;
                this.createTunnel(chunkPos, random.nextLong(), x, y, z, horizontalRadiusMultiplier, verticalRadiusMultiplier, thickness, horizontalRotation, verticalRotation, 0, distance, startVerticalRadiusMultiplier, output, skipChecker);
            }
        }
        return true;
    }

    private float getThickness(RandomSource random) {
        float thickness = this.thickness.sample(random);
        if (this.weirdThicknessBias && random.nextInt(10) == 0) {
            thickness *= random.nextFloat() * random.nextFloat() * 3.0f + 1.0f;
        }
        return thickness;
    }

    private void createRoom(ChunkPos chunkPos, double x, double y, double z, float thickness, double yScale, CarverOutput output, WorldCarver.CarveSkipChecker skipChecker) {
        double horizontalRadius = 1.5 + (double)(Mth.sin(1.5707963705062866) * thickness);
        double verticalRadius = horizontalRadius * yScale;
        WorldCarver.carveEllipsoid(chunkPos, x + 1.0, y, z, horizontalRadius, verticalRadius, output, skipChecker);
    }

    private void createTunnel(ChunkPos chunkPos, long tunnelSeed, double x, double y, double z, double horizontalRadiusMultiplier, double verticalRadiusMultiplier, float thickness, float horizontalRotation, float verticalRotation, int step, int dist, double yScale, CarverOutput output, WorldCarver.CarveSkipChecker skipChecker) {
        RandomSource random = RandomSource.createThreadLocalInstance(tunnelSeed);
        int splitPoint = random.nextInt(dist / 2) + dist / 4;
        boolean steep = random.nextInt(6) == 0;
        float yRota = 0.0f;
        float xRota = 0.0f;
        for (int currentStep = step; currentStep < dist; ++currentStep) {
            double horizontalRadius = 1.5 + (double)(Mth.sin((float)Math.PI * (float)currentStep / (float)dist) * thickness);
            double verticalRadius = horizontalRadius * yScale;
            float cosX = Mth.cos(verticalRotation);
            x += (double)(Mth.cos(horizontalRotation) * cosX);
            y += (double)Mth.sin(verticalRotation);
            z += (double)(Mth.sin(horizontalRotation) * cosX);
            verticalRotation *= steep ? 0.92f : 0.7f;
            verticalRotation += xRota * 0.1f;
            horizontalRotation += yRota * 0.1f;
            xRota *= 0.9f;
            yRota *= 0.75f;
            xRota += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f;
            yRota += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f;
            if (currentStep == splitPoint && thickness > 1.0f) {
                this.createTunnel(chunkPos, random.nextLong(), x, y, z, horizontalRadiusMultiplier, verticalRadiusMultiplier, random.nextFloat() * 0.5f + 0.5f, horizontalRotation - 1.5707964f, verticalRotation / 3.0f, currentStep, dist, 1.0, output, skipChecker);
                this.createTunnel(chunkPos, random.nextLong(), x, y, z, horizontalRadiusMultiplier, verticalRadiusMultiplier, random.nextFloat() * 0.5f + 0.5f, horizontalRotation + 1.5707964f, verticalRotation / 3.0f, currentStep, dist, 1.0, output, skipChecker);
                return;
            }
            if (random.nextInt(4) == 0) continue;
            if (!WorldCarver.canReach(chunkPos, x, z, currentStep, dist, thickness)) {
                return;
            }
            WorldCarver.carveEllipsoid(chunkPos, x, y, z, horizontalRadius * horizontalRadiusMultiplier, verticalRadius * verticalRadiusMultiplier, output, skipChecker);
        }
    }

    private static boolean shouldSkip(double xd, double yd, double zd, double floorLevel) {
        if (yd <= floorLevel) {
            return true;
        }
        return xd * xd + yd * yd + zd * zd >= 1.0;
    }

    public MapCodec<CaveWorldCarver> codec() {
        return MAP_CODEC;
    }
}

