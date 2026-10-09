/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Floats
 *  it.unimi.dsi.fastutil.floats.FloatArrayList
 *  it.unimi.dsi.fastutil.floats.FloatList
 *  it.unimi.dsi.fastutil.ints.IntArrays
 */
package net.minecraft.client.renderer.feature.phase;

import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;

public class TranslucentFeatureRenderPhase
implements FeatureRenderPhase<TranslucentSubmit> {
    private final List<TranslucentSubmit> submits = new ArrayList<TranslucentSubmit>();
    private final FloatList distances = new FloatArrayList();

    @Override
    public void submit(TranslucentSubmit submit) {
        this.submits.add(submit);
        this.distances.add(submit.distanceToCameraSq());
    }

    @Override
    public void sortInto(FeatureRenderPhase.Output output) {
        if (this.submits.isEmpty()) {
            return;
        }
        for (int index : this.sortIndices()) {
            output.accept(this.submits.get(index), true);
        }
        this.submits.clear();
        this.distances.clear();
    }

    private int[] sortIndices() {
        int[] indices = new int[this.submits.size()];
        for (int i = 0; i < this.submits.size(); ++i) {
            indices[i] = i;
        }
        IntArrays.unstableSort((int[])indices, (i1, i2) -> {
            int byDistance = Floats.compare((float)this.distances.getFloat(i2), (float)this.distances.getFloat(i1));
            return byDistance != 0 ? byDistance : Integer.compare(i1, i2);
        });
        return indices;
    }

    @Override
    public boolean isEmpty() {
        return this.submits.isEmpty();
    }
}

