/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.time.Instant;
import net.minecraft.util.LenientJsonParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record InviteCode(String code, @Nullable Instant expirationDate, boolean enabled) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static @Nullable InviteCode parse(String json) {
        try {
            JsonObject object = LenientJsonParser.parse(json).getAsJsonObject();
            JsonElement result = object.get("result");
            JsonObject payload = result != null && result.isJsonObject() ? result.getAsJsonObject() : object;
            return InviteCode.parse(payload);
        }
        catch (Exception e) {
            LOGGER.error("Could not parse InviteCode", (Throwable)e);
            return null;
        }
    }

    public static @Nullable InviteCode parse(JsonObject json) {
        try {
            String code = JsonUtils.getStringOr("code", json, "");
            if (code.isEmpty()) {
                LOGGER.error("Invite code response was missing a code");
                return null;
            }
            return new InviteCode(code, JsonUtils.getEpochMillisOr("expirationDate", json, null), JsonUtils.getBooleanOr("enabled", json, false));
        }
        catch (Exception e) {
            LOGGER.error("Could not parse InviteCode", (Throwable)e);
            return null;
        }
    }
}

