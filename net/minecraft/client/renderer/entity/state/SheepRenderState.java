/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.color.ColorLerper;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.DyeColor;

public class SheepRenderState
extends LivingEntityRenderState {
    public float headEatPositionScale;
    public float headEatAngleScale;
    public boolean isSheared;
    public DyeColor woolColor = DyeColor.WHITE;
    public boolean isJebSheep;

    public int getWoolColor() {
        if (this.isJebSheep) {
            return ColorLerper.getLerpedColor(ColorLerper.Type.SHEEP, this.ageInTicks);
        }
        return ColorLerper.Type.SHEEP.getColor(this.woolColor);
    }
}

