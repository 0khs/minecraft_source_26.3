/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.feature.phase;

import java.util.Collection;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

public interface FeatureRenderPhase<Submit extends SubmitNode> {
    public void submit(Submit var1);

    public void sortInto(Output var1);

    public boolean isEmpty();

    @FunctionalInterface
    public static interface Output {
        public void accept(SubmitNode var1, boolean var2);

        default public <Submit extends SubmitNode> void acceptFeatureGroup(FeatureRendererType<Submit> featureType, Collection<Submit> submits, boolean strictlyOrdered) {
            for (SubmitNode submit : submits) {
                if (submit.featureType() != featureType) {
                    throw new IllegalArgumentException(String.valueOf(submit) + " was not of feature type " + String.valueOf(featureType));
                }
                this.accept(submit, strictlyOrdered);
            }
        }
    }
}

