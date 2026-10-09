/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import org.jspecify.annotations.Nullable;

public record InviteCodeRequestDto(@SerializedName(value="linkId") @Nullable String code, @SerializedName(value="realmId") long realmId, @SerializedName(value="enabled") boolean enabled, @SerializedName(value="expirationDate") @Nullable Long expirationDate) implements ReflectionBasedSerialization
{
}

