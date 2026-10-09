/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMaps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityFluidInteraction {
    private final List<Tracker> fluidTrackers = new ArrayList<Tracker>();
    private final Reference2ObjectMap<TagKey<Fluid>, CurrentAccumulator> currentAccumulators = new Reference2ObjectArrayMap();

    public EntityFluidInteraction(Set<TagKey<Fluid>> fluidsWithCurrent) {
        for (TagKey<Fluid> fluid : fluidsWithCurrent) {
            this.currentAccumulators.put(fluid, (Object)new CurrentAccumulator());
        }
    }

    public boolean update(Entity entity, boolean ignoreCurrent) {
        this.fluidTrackers.removeIf(Tracker::reset);
        this.currentAccumulators.values().forEach(CurrentAccumulator::reset);
        AABB box = entity.getFluidInteractionBox();
        if (box == null) {
            return false;
        }
        int x0 = Mth.floor(box.minX);
        int y0 = Mth.floor(box.minY);
        int z0 = Mth.floor(box.minZ);
        int x1 = Mth.ceil(box.maxX) - 1;
        int y1 = Mth.ceil(box.maxY) - 1;
        int z1 = Mth.ceil(box.maxZ) - 1;
        if (!EntityFluidInteraction.hasFluidAndLoaded(entity.level(), x0 - 1, y0, z0 - 1, x1 + 1, y1, z1 + 1)) {
            return false;
        }
        double entityY = entity.getBoundingBox().minY;
        int eyeBlockX = entity.getBlockX();
        double eyeY = entity.getEyeY();
        int eyeBlockZ = entity.getBlockZ();
        Holder<Fluid> lastFluidType = null;
        Tracker tracker = null;
        CurrentAccumulator current = null;
        Level level = entity.level();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = x0; x <= x1; ++x) {
            for (int y = y0; y <= y1; ++y) {
                for (int z = z0; z <= z1; ++z) {
                    double fluidTopForCamera;
                    double fluidBottom;
                    double fluidTop;
                    mutablePos.set(x, y, z);
                    FluidState fluidState = level.getFluidState(mutablePos);
                    if (fluidState.isEmpty() || (fluidTop = (fluidBottom = (double)mutablePos.getY()) + (double)fluidState.getHeight(level, mutablePos)) < box.minY) continue;
                    Holder<Fluid> fluidType = fluidState.typeHolder();
                    if (fluidType != lastFluidType) {
                        lastFluidType = fluidType;
                        tracker = this.getOrCreateTrackerFor(fluidType);
                        if (!ignoreCurrent) {
                            current = this.getCurrentAccumulatorFor(fluidType);
                        }
                    }
                    if (x == eyeBlockX && z == eyeBlockZ && eyeY >= fluidBottom && eyeY <= (fluidTopForCamera = fluidBottom + (double)fluidState.getHeightForCamera(level, mutablePos))) {
                        tracker.eyesInside = true;
                    }
                    tracker.height = Math.max(fluidTop - entityY, tracker.height);
                    if (current == null) continue;
                    Vec3 flow = fluidState.getFlow(level, mutablePos);
                    current.height = Math.max(tracker.height, current.height);
                    if (current.height < 0.4) {
                        flow = flow.scale(current.height);
                    }
                    current.accumulate(flow);
                }
            }
        }
        return lastFluidType != null;
    }

    private static boolean hasFluidAndLoaded(Level level, int x0, int y0, int z0, int x1, int y1, int z1) {
        int sectionX0 = SectionPos.blockToSectionCoord(x0);
        int sectionY0 = SectionPos.blockToSectionCoord(y0);
        int sectionZ0 = SectionPos.blockToSectionCoord(z0);
        int sectionX1 = SectionPos.blockToSectionCoord(x1);
        int sectionY1 = SectionPos.blockToSectionCoord(y1);
        int sectionZ1 = SectionPos.blockToSectionCoord(z1);
        boolean hasFluid = false;
        for (int chunkZ = sectionZ0; chunkZ <= sectionZ1; ++chunkZ) {
            for (int chunkX = sectionX0; chunkX <= sectionX1; ++chunkX) {
                ChunkAccess chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk == null) {
                    return false;
                }
                LevelChunkSection[] sections = chunk.getSections();
                for (int sectionY = sectionY0; sectionY <= sectionY1; ++sectionY) {
                    int sectionIndex = chunk.getSectionIndexFromSectionY(sectionY);
                    if (sectionIndex < 0 || sectionIndex >= sections.length) continue;
                    hasFluid |= sections[sectionIndex].hasFluid();
                }
            }
        }
        return hasFluid;
    }

    private Tracker getOrCreateTrackerFor(Holder<Fluid> fluidType) {
        for (Tracker tracker : this.fluidTrackers) {
            if (!tracker.fluidType.equals(fluidType)) continue;
            return tracker;
        }
        Tracker tracker = new Tracker(fluidType);
        this.fluidTrackers.add(tracker);
        return tracker;
    }

    private @Nullable CurrentAccumulator getCurrentAccumulatorFor(Holder<Fluid> fluid) {
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.currentAccumulators)) {
            if (!fluid.is((TagKey)entry.getKey())) continue;
            return (CurrentAccumulator)entry.getValue();
        }
        return null;
    }

    public void applyCurrentTo(TagKey<Fluid> fluid, Entity entity, double scale) {
        CurrentAccumulator current = (CurrentAccumulator)this.currentAccumulators.get(fluid);
        if (current != null) {
            current.applyTo(entity, scale);
        }
    }

    public double getFluidHeight(TagKey<Fluid> fluid) {
        double height = 0.0;
        for (Tracker tracker : this.fluidTrackers) {
            if (!(tracker.height > height) || !tracker.fluidType.is(fluid)) continue;
            height = tracker.height;
        }
        return height;
    }

    public boolean isInFluid(TagKey<Fluid> fluid) {
        return this.getFluidHeight(fluid) > 0.0;
    }

    public boolean isEyeInFluid(TagKey<Fluid> fluid) {
        for (Tracker tracker : this.fluidTrackers) {
            if (!tracker.eyesInside || !tracker.fluidType.is(fluid)) continue;
            return true;
        }
        return false;
    }

    private static class CurrentAccumulator {
        private double height;
        private Vec3 accumulatedCurrent = Vec3.ZERO;
        private int currentCount;

        private CurrentAccumulator() {
        }

        public void reset() {
            this.height = 0.0;
            this.accumulatedCurrent = Vec3.ZERO;
            this.currentCount = 0;
        }

        public void accumulate(Vec3 flow) {
            this.accumulatedCurrent = this.accumulatedCurrent.add(flow);
            ++this.currentCount;
        }

        public void applyTo(Entity entity, double scale) {
            if (this.currentCount == 0 || this.accumulatedCurrent.lengthSqr() < (double)1.0E-5f) {
                return;
            }
            Vec3 impulse = !(entity instanceof Player) ? this.accumulatedCurrent.normalize() : this.accumulatedCurrent.scale(1.0 / (double)this.currentCount);
            Vec3 oldMovement = entity.getDeltaMovement();
            impulse = impulse.scale(scale);
            double min = 0.003;
            if (Math.abs(oldMovement.x) < 0.003 && Math.abs(oldMovement.z) < 0.003 && impulse.length() < 0.0045000000000000005) {
                impulse = impulse.normalize().scale(0.0045000000000000005);
            }
            entity.addDeltaMovement(impulse);
        }
    }

    private static class Tracker {
        private final Holder<Fluid> fluidType;
        private double height;
        private boolean eyesInside;

        public Tracker(Holder<Fluid> fluidType) {
            this.fluidType = fluidType;
        }

        public boolean reset() {
            if (this.height == 0.0 && !this.eyesInside) {
                return true;
            }
            this.height = 0.0;
            this.eyesInside = false;
            return false;
        }
    }
}

