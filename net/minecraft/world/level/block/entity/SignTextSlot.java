/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.level.block.entity;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum SignTextSlot {
    BACK(0),
    FRONT(1);

    private final int id;
    private static final IntFunction<SignTextSlot> BY_ID;
    public static final StreamCodec<ByteBuf, SignTextSlot> STREAM_CODEC;

    private SignTextSlot(int id) {
        this.id = id;
    }

    static {
        BY_ID = ByIdMap.continuous(h -> h.id, SignTextSlot.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, h -> h.id);
    }
}

