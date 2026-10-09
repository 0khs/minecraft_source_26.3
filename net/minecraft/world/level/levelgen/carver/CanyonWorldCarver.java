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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public record CanyonWorldCarver(float probability, HeightProvider y, FloatProvider verticalRotation, Shape shape) implements WorldCarver
{
    public static final MapCodec<CanyonWorldCarver> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").forGetter(c -> Float.valueOf(c.probability)), (App)HeightProvider.CODEC.fieldOf("y").forGetter(c -> c.y), (App)FloatProviders.CODEC.fieldOf("vertical_rotation").forGetter(c -> c.verticalRotation), (App)Shape.CODEC.fieldOf("shape").forGetter(c -> c.shape)).apply((Applicative)i, CanyonWorldCarver::new));

    @Override
    public boolean isStartChunk(RandomSource random) {
        return random.nextFloat() <= this.probability;
    }

    @Override
    public boolean carve(WorldGenerationContext context, RandomSource random, ChunkPos chunkPos, ChunkPos sourceChunkPos, CarverOutput output) {
        int maxDistance = (this.getRange() * 2 - 1) * 16;
        double x = sourceChunkPos.getBlockX(random.nextInt(16));
        int y = this.y.sample(random, context);
        double z = sourceChunkPos.getBlockZ(random.nextInt(16));
        float horizontalRotation = random.nextFloat() * ((float)Math.PI * 2);
        float verticalRotation = this.verticalRotation.sample(random);
        double yScale = this.shape.yScale().sample(random);
        float thickness = this.shape.thickness().sample(random);
        int distance = (int)((float)maxDistance * this.shape.distanceFactor().sample(random));
        boolean initialStep = false;
        this.doCarve(context, chunkPos, random.nextLong(), x, y, z, thickness, horizontalRotation, verticalRotation, 0, distance, yScale, output);
        return true;
    }

    private void doCarve(WorldGenerationContext context, ChunkPos chunkPos, long tunnelSeed, double x, double y, double z, float thickness, float horizontalRotation, float verticalRotation, int step, int distance, double yScale, CarverOutput output) {
        RandomSource random = RandomSource.createThreadLocalInstance(tunnelSeed);
        float[] widthFactorPerHeight = this.initWidthFactors(context, random);
        float yRota = 0.0f;
        float xRota = 0.0f;
        for (int currentStep = step; currentStep < distance; ++currentStep) {
            double horizontalRadius = 1.5 + (double)(Mth.sin((float)currentStep * (float)Math.PI / (float)distance) * thickness);
            double verticalRadius = horizontalRadius * yScale;
            horizontalRadius *= (double)this.shape.horizontalRadiusFactor().sample(random);
            verticalRadius = this.updateVerticalRadius(random, verticalRadius, distance, currentStep);
            float xc = Mth.cos(verticalRotation);
            float xs = Mth.sin(verticalRotation);
            x += (double)(Mth.cos(horizontalRotation) * xc);
            y += (double)xs;
            z += (double)(Mth.sin(horizontalRotation) * xc);
            verticalRotation *= 0.7f;
            verticalRotation += xRota * 0.05f;
            horizontalRotation += yRota * 0.05f;
            xRota *= 0.8f;
            yRota *= 0.5f;
            xRota += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f;
            yRota += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f;
            if (random.nextInt(4) == 0) continue;
            if (!WorldCarver.canReach(chunkPos, x, z, currentStep, distance, thickness)) {
                return;
            }
            WorldCarver.carveEllipsoid(chunkPos, x, y, z, horizontalRadius, verticalRadius, output, (xd, yd, zd, y1) -> this.shouldSkip(context, widthFactorPerHeight, xd, yd, zd, y1));
        }
    }

    private float[] initWidthFactors(WorldGenerationContext context, RandomSource random) {
        int depth = context.getGenDepth();
        float[] widthFactorPerHeight = new float[depth];
        float widthFactor = 1.0f;
        for (int yIndex = 0; yIndex < depth; ++yIndex) {
            if (yIndex == 0 || random.nextInt(this.shape.widthSmoothness()) == 0) {
                widthFactor = 1.0f + random.nextFloat() * random.nextFloat();
            }
            widthFactorPerHeight[yIndex] = widthFactor * widthFactor;
        }
        return widthFactorPerHeight;
    }

    private double updateVerticalRadius(RandomSource random, double verticalRadius, float distance, float currentStep) {
        float verticalMultiplier = 1.0f - Mth.abs(0.5f - currentStep / distance) * 2.0f;
        float factor = this.shape.verticalRadiusDefaultFactor() + this.shape.verticalRadiusCenterFactor() * verticalMultiplier;
        return (double)factor * verticalRadius * (double)Mth.randomBetween(random, 0.75f, 1.0f);
    }

    private boolean shouldSkip(WorldGenerationContext context, float[] widthFactorPerHeight, double xd, double yd, double zd, int y) {
        int yIndex = y - context.getMinGenY();
        return (xd * xd + zd * zd) * (double)widthFactorPerHeight[yIndex - 1] + yd * yd / 6.0 >= 1.0;
    }

    public MapCodec<CanyonWorldCarver> codec() {
        return MAP_CODEC;
    }

    public record Shape(FloatProvider distanceFactor, FloatProvider thickness, int widthSmoothness, FloatProvider horizontalRadiusFactor, float verticalRadiusDefaultFactor, float verticalRadiusCenterFactor, FloatProvider yScale) {
        public static final Codec<Shape> CODEC = RecordCodecBuilder.create(i -> i.group((App)FloatProviders.CODEC.fieldOf("distance_factor").forGetter(c -> c.distanceFactor), (App)FloatProviders.CODEC.fieldOf("thickness").forGetter(c -> c.thickness), (App)ExtraCodecs.POSITIVE_INT.fieldOf("width_smoothness").forGetter(c -> c.widthSmoothness), (App)FloatProviders.CODEC.fieldOf("horizontal_radius_factor").forGetter(c -> c.horizontalRadiusFactor), (App)Codec.FLOAT.fieldOf("vertical_radius_default_factor").forGetter(c -> Float.valueOf(c.verticalRadiusDefaultFactor)), (App)Codec.FLOAT.fieldOf("vertical_radius_center_factor").forGetter(c -> Float.valueOf(c.verticalRadiusCenterFactor)), (App)FloatProviders.CODEC.fieldOf("y_scale").forGetter(c -> c.yScale)).apply((Applicative)i, Shape::new));
    }
}

