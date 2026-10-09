/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.server;

import java.util.UUID;

public interface PackLoadFeedback {
    public void reportUpdate(UUID var1, Update var2);

    public void reportFinalResult(UUID var1, FinalResult var2);

    public static enum FinalResult {
        DECLINED,
        APPLIED,
        DISCARDED,
        DOWNLOAD_FAILED,
        ACTIVATION_FAILED;

    }

    public static enum Update {
        ACCEPTED,
        DOWNLOADED;

    }
}

