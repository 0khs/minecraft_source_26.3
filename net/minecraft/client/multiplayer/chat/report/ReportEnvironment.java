/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.services.request.AbuseReportRequest$ClientInfo
 *  com.mojang.authlib.services.request.AbuseReportRequest$RealmInfo
 *  com.mojang.authlib.services.request.AbuseReportRequest$ThirdPartyServerInfo
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.services.request.AbuseReportRequest;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.Locale;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

public record ReportEnvironment(String clientVersion, @Nullable Server server) {
    public static ReportEnvironment local() {
        return ReportEnvironment.create(null);
    }

    public static ReportEnvironment thirdParty(String ip) {
        return ReportEnvironment.create(new Server.ThirdParty(ip));
    }

    public static ReportEnvironment realm(RealmsServer realm) {
        return ReportEnvironment.create(new Server.Realm(realm));
    }

    public static ReportEnvironment create(@Nullable Server server) {
        return new ReportEnvironment(ReportEnvironment.getClientVersion(), server);
    }

    public AbuseReportRequest.ClientInfo clientInfo() {
        return new AbuseReportRequest.ClientInfo(this.clientVersion, Locale.getDefault().toLanguageTag());
    }

    public // Could not load outer class - annotation placement on inner may be incorrect
     @Nullable AbuseReportRequest.ThirdPartyServerInfo thirdPartyServerInfo() {
        Server server = this.server;
        if (server instanceof Server.ThirdParty) {
            Server.ThirdParty thirdParty = (Server.ThirdParty)server;
            return new AbuseReportRequest.ThirdPartyServerInfo(thirdParty.ip);
        }
        return null;
    }

    public // Could not load outer class - annotation placement on inner may be incorrect
     @Nullable AbuseReportRequest.RealmInfo realmInfo() {
        Server server = this.server;
        if (server instanceof Server.Realm) {
            Server.Realm realm = (Server.Realm)server;
            return new AbuseReportRequest.RealmInfo(String.valueOf(realm.realmId()), realm.slotId());
        }
        return null;
    }

    private static String getClientVersion() {
        StringBuilder version = new StringBuilder();
        version.append(SharedConstants.getCurrentVersion().id());
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            version.append(" (modded)");
        }
        return version.toString();
    }

    public static interface Server {

        public record Realm(long realmId, int slotId) implements Server
        {
            public Realm(RealmsServer realm) {
                this(realm.id, realm.activeSlot);
            }
        }

        public record ThirdParty(String ip) implements Server
        {
        }
    }
}

