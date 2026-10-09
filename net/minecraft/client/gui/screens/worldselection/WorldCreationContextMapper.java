/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.worldselection;

import net.minecraft.client.gui.screens.worldselection.DataPackReloadCookie;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;

@FunctionalInterface
public interface WorldCreationContextMapper {
    public WorldCreationContext apply(ReloadableServerResources var1, LayeredRegistryAccess<RegistryLayer> var2, DataPackReloadCookie var3);
}

