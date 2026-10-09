/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;

public enum InteractionHand {
    MAIN_HAND(0),
    OFF_HAND(1);

    private static final IntFunction<InteractionHand> BY_ID;
    public static final StreamCodec<ByteBuf, InteractionHand> STREAM_CODEC;
    private final int id;

    private InteractionHand(int id) {
        this.id = id;
    }

    public HumanoidArm asArm(HumanoidArm mainArm) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> mainArm;
            case 1 -> mainArm.getOpposite();
        };
    }

    public EquipmentSlot asEquipmentSlot() {
        return this == MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    static {
        BY_ID = ByIdMap.continuous(h -> h.id, InteractionHand.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, h -> h.id);
    }
}

