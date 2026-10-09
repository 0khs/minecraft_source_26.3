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

public record RealmTierConfigurationDto(@SerializedName(value="renderDistance") RealmTierRangeDto renderDistance, @SerializedName(value="simDistance") RealmTierRangeDto simDistance) implements ReflectionBasedSerialization
{

    public record RealmTierRangeDto(@SerializedName(value="min") int min, @SerializedName(value="max") int max, @SerializedName(value="defaultValue") int defaultValue, @SerializedName(value="current") @Nullable Integer current) implements ReflectionBasedSerialization
    {
    }
}

