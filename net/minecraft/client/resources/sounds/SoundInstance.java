/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.resources.sounds;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public interface SoundInstance {
    public Identifier getIdentifier();

    public @Nullable WeighedSoundEvents getOrResolve(SoundManager var1);

    public @Nullable Sound getSound();

    public @Nullable WeighedSoundEvents getSoundEvent();

    public SoundSource getSource();

    public boolean isLooping();

    public boolean isRelative();

    public int getDelay();

    public float getVolume();

    public float getPitch();

    public double getX();

    public double getY();

    public double getZ();

    public Attenuation getAttenuation();

    default public boolean canStartSilent() {
        return false;
    }

    default public boolean canPlaySound() {
        return true;
    }

    public static RandomSource createUnseededRandom() {
        return RandomSource.create();
    }

    public static enum Attenuation {
        NONE,
        LINEAR;

    }
}

