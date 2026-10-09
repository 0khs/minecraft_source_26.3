/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.conditional.ItemModelPropertyTest;

public interface ConditionalItemModelProperty
extends ItemModelPropertyTest {
    public MapCodec<? extends ConditionalItemModelProperty> type();
}

