/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.client.data.models.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.Identifier;

public class DelegatedModel
implements ModelInstance {
    private final Identifier parent;

    public DelegatedModel(Identifier parent) {
        this.parent = parent;
    }

    @Override
    public JsonElement get() {
        JsonObject result = new JsonObject();
        result.addProperty("parent", this.parent.toString());
        return result;
    }
}

