/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.sounds;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class UnderLiquidSubSound
extends AbstractTickableSoundInstance {
    private final LocalPlayer player;

    protected UnderLiquidSubSound(LocalPlayer player, SoundEvent event) {
        super(event, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = false;
        this.delay = 0;
        this.volume = 1.0f;
        this.relative = true;
    }

    @Override
    public void tick() {
        if (this.player.isRemoved() || !this.player.isUnderWater()) {
            this.stop();
        }
    }
}

