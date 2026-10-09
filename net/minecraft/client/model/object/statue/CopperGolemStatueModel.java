/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.object.statue;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;

public class CopperGolemStatueModel
extends Model<Unit> {
    public CopperGolemStatueModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }

    @Override
    public void setupAnim(Unit ignored) {
        this.root.y = 0.0f;
        this.root.zRot = (float)Math.PI;
    }
}

