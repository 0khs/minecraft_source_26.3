/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.client.worldupload;

import com.mojang.realmsclient.client.worldupload.RealmsUploadException;
import net.minecraft.network.chat.Component;

public class RealmsUploadWorldNotClosedException
extends RealmsUploadException {
    @Override
    public Component getStatusMessage() {
        return Component.translatable("mco.upload.close.failure");
    }
}

