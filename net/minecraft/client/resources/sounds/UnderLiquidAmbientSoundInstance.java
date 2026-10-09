/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.sounds;

import java.util.function.Predicate;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class UnderLiquidAmbientSoundInstance
extends AbstractTickableSoundInstance {
    public static final int FADE_DURATION = 40;
    private final LocalPlayer player;
    private final Predicate<LocalPlayer> isUnderLiquid;
    private int fade;

    public UnderLiquidAmbientSoundInstance(SoundEvent event, LocalPlayer player, Predicate<LocalPlayer> isUnderLiquid) {
        super(event, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.isUnderLiquid = isUnderLiquid;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0f;
        this.relative = true;
    }

    public static UnderLiquidAmbientSoundInstance underwater(LocalPlayer player) {
        return new UnderLiquidAmbientSoundInstance(SoundEvents.AMBIENT_UNDERWATER_LOOP, player, LocalPlayer::isUnderWater);
    }

    @Override
    public void tick() {
        if (this.player.isRemoved() || this.fade < 0) {
            this.stop();
            return;
        }
        this.fade = this.isUnderLiquid.test(this.player) ? ++this.fade : (this.fade -= 2);
        this.fade = Math.min(this.fade, 40);
        this.volume = Math.max(0.0f, Math.min((float)this.fade / 40.0f, 1.0f));
    }
}

