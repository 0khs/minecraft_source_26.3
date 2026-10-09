/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.item.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.slot.CompositeSlotSource;
import net.minecraft.world.item.slot.SlotSource;

public class GroupSlotSource
extends CompositeSlotSource {
    public static final MapCodec<GroupSlotSource> MAP_CODEC = GroupSlotSource.createCodec(GroupSlotSource::new);
    public static final Codec<GroupSlotSource> INLINE_CODEC = GroupSlotSource.createInlineCodec(GroupSlotSource::new);

    private GroupSlotSource(HolderSet<SlotSource> terms) {
        super(terms);
    }

    public MapCodec<GroupSlotSource> codec() {
        return MAP_CODEC;
    }
}

