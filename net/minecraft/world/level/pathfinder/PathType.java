/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.level.pathfinder;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum PathType {
    BLOCKED(0, -1.0f),
    OPEN(1, 0.0f),
    WALKABLE(2, 0.0f),
    WALKABLE_DOOR(3, 0.0f),
    TRAPDOOR(4, 0.0f),
    POWDER_SNOW(5, -1.0f),
    ON_TOP_OF_POWDER_SNOW(6, 0.0f),
    FENCE(7, -1.0f),
    LAVA(8, -1.0f),
    WATER(9, 8.0f),
    WATER_BORDER(10, 8.0f),
    RAIL(11, 0.0f),
    UNPASSABLE_RAIL(12, -1.0f),
    FIRE_IN_NEIGHBOR(13, 8.0f),
    FIRE(14, 16.0f),
    DAMAGING_IN_NEIGHBOR(15, 8.0f),
    DAMAGING(16, -1.0f),
    DOOR_OPEN(17, 0.0f),
    DOOR_WOOD_CLOSED(18, -1.0f),
    DOOR_IRON_CLOSED(19, -1.0f),
    BREACH(20, 4.0f),
    LEAVES(21, -1.0f),
    STICKY_HONEY(22, 8.0f),
    COCOA(23, 0.0f),
    DAMAGE_CAUTIOUS(24, 0.0f),
    ON_TOP_OF_TRAPDOOR(25, 0.0f),
    BIG_MOBS_CLOSE_TO_DANGER(26, 4.0f);

    private static final IntFunction<PathType> BY_ID;
    public static final StreamCodec<ByteBuf, PathType> STREAM_CODEC;
    private final int id;
    private final float malus;

    private PathType(int id, float defaultCost) {
        this.id = id;
        this.malus = defaultCost;
    }

    public float getMalus() {
        return this.malus;
    }

    static {
        BY_ID = ByIdMap.continuous(t -> t.id, PathType.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, t -> t.id);
    }
}

