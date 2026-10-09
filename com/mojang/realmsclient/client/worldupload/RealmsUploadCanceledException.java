/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.client.worldupload;

import com.mojang.realmsclient.client.worldupload.RealmsUploadException;
import net.minecraft.network.chat.Component;

public class RealmsUploadCanceledException
extends RealmsUploadException {
    private static final Component UPLOAD_CANCELED = Component.translatable("mco.upload.cancelled");

    @Override
    public Component getStatusMessage() {
        return UPLOAD_CANCELED;
    }
}

