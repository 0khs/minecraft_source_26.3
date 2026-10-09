/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.entity;

import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.Nullable;

public interface ClientAvatarEntity {
    public ClientAvatarState avatarState();

    public PlayerSkin getSkin();

    public  @Nullable Parrot.Variant getParrotVariantOnShoulder(boolean var1);

    public boolean showExtraEars();
}

