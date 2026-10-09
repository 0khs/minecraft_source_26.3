/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.net.URI;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record WorldTemplate(String id, String name, String version, String author, @Nullable URI link, @Nullable String image, @Nullable URI trailer, String recommendedPlayers, WorldTemplateType type) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static @Nullable WorldTemplate parse(JsonObject node) {
        try {
            String templateTypeName = JsonUtils.getStringOr("type", node, null);
            return new WorldTemplate(JsonUtils.getStringOr("id", node, ""), JsonUtils.getStringOr("name", node, ""), JsonUtils.getStringOr("version", node, ""), JsonUtils.getStringOr("author", node, ""), JsonUtils.getUriNullable("link", node), JsonUtils.getStringOr("image", node, null), JsonUtils.getUriNullable("trailer", node), JsonUtils.getStringOr("recommendedPlayers", node, ""), templateTypeName == null ? WorldTemplateType.WORLD_TEMPLATE : WorldTemplateType.valueOf(templateTypeName));
        }
        catch (Exception e) {
            LOGGER.error("Could not parse WorldTemplate", (Throwable)e);
            return null;
        }
    }

    public static enum WorldTemplateType {
        WORLD_TEMPLATE,
        MINIGAME,
        ADVENTUREMAP,
        EXPERIENCE,
        INSPIRATION;

    }
}

