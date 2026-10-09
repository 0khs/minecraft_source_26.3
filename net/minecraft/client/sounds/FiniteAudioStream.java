/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.sounds;

import java.io.IOException;
import java.nio.ByteBuffer;
import net.minecraft.client.sounds.AudioStream;

public interface FiniteAudioStream
extends AudioStream {
    public ByteBuffer readAll() throws IOException;
}

