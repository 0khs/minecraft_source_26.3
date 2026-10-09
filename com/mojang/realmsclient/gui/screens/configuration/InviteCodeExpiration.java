/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.gui.screens.configuration;

import java.time.Duration;
import java.time.Instant;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public enum InviteCodeExpiration {
    THIRTY_MINUTES(Component.translatable("mco.configure.world.invite_codes.expiration.30_minutes"), Duration.ofMinutes(30L)),
    ONE_HOUR(Component.translatable("mco.configure.world.invite_codes.expiration.1_hour"), Duration.ofHours(1L)),
    SIX_HOURS(Component.translatable("mco.configure.world.invite_codes.expiration.6_hours"), Duration.ofHours(6L)),
    TWELVE_HOURS(Component.translatable("mco.configure.world.invite_codes.expiration.12_hours"), Duration.ofHours(12L)),
    ONE_DAY(Component.translatable("mco.configure.world.invite_codes.expiration.1_day"), Duration.ofDays(1L)),
    SEVEN_DAYS(Component.translatable("mco.configure.world.invite_codes.expiration.7_days"), Duration.ofDays(7L)),
    NEVER(Component.translatable("mco.configure.world.invite_codes.expiration.never"), null);

    private final Component label;
    private final @Nullable Duration duration;

    private InviteCodeExpiration(Component label, Duration duration) {
        this.label = label;
        this.duration = duration;
    }

    public Component getLabel() {
        return this.label;
    }

    static @Nullable Instant resolveExpirationDate(@Nullable Instant currentExpirationDate, InviteCodeExpiration selectedExpiration, boolean expirationChanged, Instant now) {
        if (!expirationChanged) {
            return currentExpirationDate;
        }
        Duration duration = selectedExpiration.duration;
        return duration != null ? now.plus(duration) : null;
    }

    public static InviteCodeExpiration closestTo(Instant expirationDate) {
        long remainingMillis = Duration.between(Instant.now(), expirationDate).toMillis();
        InviteCodeExpiration closest = NEVER;
        long bestDiff = Long.MAX_VALUE;
        for (InviteCodeExpiration candidate : InviteCodeExpiration.values()) {
            long diff;
            if (candidate.duration == null || (diff = Math.abs(candidate.duration.toMillis() - remainingMillis)) >= bestDiff) continue;
            bestDiff = diff;
            closest = candidate;
        }
        return closest;
    }
}

