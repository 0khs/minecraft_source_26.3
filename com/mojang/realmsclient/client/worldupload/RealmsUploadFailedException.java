/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.client.worldupload;

import com.mojang.realmsclient.client.worldupload.RealmsUploadException;
import net.minecraft.network.chat.Component;

public class RealmsUploadFailedException
extends RealmsUploadException {
    private final Component errorMessage;

    public RealmsUploadFailedException(Component errorMessage) {
        this.errorMessage = errorMessage;
    }

    public RealmsUploadFailedException(String errorMessage) {
        this(Component.literal(errorMessage));
    }

    @Override
    public Component getStatusMessage() {
        return Component.translatable("mco.upload.failed", this.errorMessage);
    }
}

