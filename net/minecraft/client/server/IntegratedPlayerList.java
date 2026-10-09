/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.server;

import java.net.SocketAddress;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class IntegratedPlayerList
extends PlayerList {
    public IntegratedPlayerList(IntegratedServer server, LayeredRegistryAccess<RegistryLayer> registryHolder, PlayerDataStorage playerDataStorage) {
        super(server, registryHolder, playerDataStorage, server.notificationManager());
        this.setViewDistance(10);
    }

    @Override
    public Component canPlayerLogin(SocketAddress address, NameAndId nameAndId) {
        if (this.getServer().isSingleplayerOwner(nameAndId) && this.getPlayerByName(nameAndId.name()) != null) {
            return Component.translatable("multiplayer.disconnect.name_taken");
        }
        return super.canPlayerLogin(address, nameAndId);
    }

    @Override
    public IntegratedServer getServer() {
        return (IntegratedServer)super.getServer();
    }
}

