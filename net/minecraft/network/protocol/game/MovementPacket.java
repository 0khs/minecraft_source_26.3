/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public interface MovementPacket<T extends PacketListener>
extends Packet<T> {
    @Override
    public PacketType<? extends MovementPacket<T>> type();

    public boolean hasPosition();

    public boolean hasRotation();
}

