/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.texture;

import java.io.IOException;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public class SimpleTexture
extends ReloadableTexture {
    public SimpleTexture(Identifier location) {
        super(location);
    }

    @Override
    public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
        return TextureContents.load(resourceManager, this.resourceId());
    }
}

