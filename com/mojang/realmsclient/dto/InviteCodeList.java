/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.InviteCode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.LenientJsonParser;
import org.slf4j.Logger;

public record InviteCodeList(List<InviteCode> inviteCodes) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static InviteCodeList parse(String json) {
        ArrayList<InviteCode> codes = new ArrayList<InviteCode>();
        try {
            JsonElement root = LenientJsonParser.parse(json);
            JsonArray array = InviteCodeList.getInviteCodesArray(root);
            for (JsonElement element : array) {
                if (!element.isJsonObject()) {
                    LOGGER.error("Invite code list contained a non-object entry: {}", (Object)element);
                    continue;
                }
                InviteCode code = InviteCode.parse(element.getAsJsonObject());
                if (code == null) continue;
                codes.add(code);
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not parse InviteCodeList", (Throwable)e);
        }
        return new InviteCodeList(List.copyOf(codes));
    }

    private static JsonArray getInviteCodesArray(JsonElement root) {
        if (root.isJsonArray()) {
            return root.getAsJsonArray();
        }
        JsonObject object = root.getAsJsonObject();
        return object.has("result") && object.get("result").isJsonArray() ? object.getAsJsonArray("result") : new JsonArray();
    }
}

