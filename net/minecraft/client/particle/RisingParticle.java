/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class RisingParticle
extends SingleQuadParticle {
    protected RisingParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z, xd, yd, zd, sprite);
        this.friction = 0.96f;
        this.xd = this.xd * (double)0.01f + xd;
        this.yd = this.yd * (double)0.01f + yd;
        this.zd = this.zd * (double)0.01f + zd;
        this.x += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05f);
        this.y += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05f);
        this.z += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05f);
        this.lifetime = (int)(8.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 4;
    }
}

