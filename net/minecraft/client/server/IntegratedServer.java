/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.server;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WorldOptionsScreen;
import net.minecraft.client.server.IntegratedPlayerList;
import net.minecraft.client.server.LanServerPinger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.notifications.NotificationManager;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import net.minecraft.stats.Stats;
import net.minecraft.util.ModCheck;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class IntegratedServer
extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_SIM_DISTANCE = 2;
    public static final int MAX_PLAYERS = 8;
    private final Minecraft minecraft;
    private boolean paused = true;
    private int publishedPort = -1;
    private @Nullable LanServerPinger lanPinger;
    private @Nullable UUID uuid;
    private int previousSimulationDistance = 0;
    private volatile List<SimpleGizmoCollector.GizmoInstance> latestTicksGizmos = new ArrayList<SimpleGizmoCollector.GizmoInstance>();
    private final SimpleGizmoCollector gizmoCollector = new SimpleGizmoCollector();
    private MinecraftServer.MultiplayerScope multiplayerScope = MinecraftServer.MultiplayerScope.OFF;
    private boolean guestCommandAccess = false;
    private boolean forceGameMode = true;

    public IntegratedServer(Thread serverThread, Minecraft minecraft, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Optional<GameRules> gameRules, Services services, LevelLoadListener levelLoadListener) {
        super(serverThread, levelStorageAccess, packRepository, worldStem, gameRules, minecraft.getProxy(), minecraft.getFixerUpper(), services, levelLoadListener, false, new NotificationManager());
        this.setSingleplayerProfile(minecraft.getGameProfile());
        this.setDemo(minecraft.isDemo());
        this.setPlayerList(new IntegratedPlayerList(this, this.registries(), this.playerDataStorage));
        this.minecraft = minecraft;
    }

    @Override
    protected boolean initServer() {
        LOGGER.info("Starting integrated minecraft server version {}", (Object)SharedConstants.getCurrentVersion().name());
        this.setUsesAuthentication(true);
        this.initializeKeyPair();
        this.loadLevel();
        GameProfile host = this.getSingleplayerProfile();
        String levelName = this.getWorldData().getLevelName();
        this.setMotd((String)(host != null ? host.name() + " - " + levelName : levelName));
        this.saveEverything(false, true, true);
        return true;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    protected void processPacketsAndTick(boolean sprinting) {
        try (Gizmos.TemporaryCollection ignored = Gizmos.withCollector(this.gizmoCollector);){
            super.processPacketsAndTick(sprinting);
        }
        if (this.tickRateManager().runsNormally()) {
            this.latestTicksGizmos = this.gizmoCollector.drainGizmos();
        }
    }

    @Override
    protected void tickServer(BooleanSupplier haveTime) {
        int serverSimulationDistance;
        boolean wasPaused = this.paused;
        this.paused = Minecraft.getInstance().isPaused() || this.getPlayerList().getPlayers().isEmpty();
        ProfilerFiller profiler = Profiler.get();
        if (!wasPaused && this.paused) {
            profiler.push("autoSave");
            LOGGER.info("Saving and pausing game...");
            this.saveEverything(false, false, false);
            profiler.pop();
        }
        if (this.paused) {
            this.tickPaused();
            return;
        }
        if (wasPaused) {
            this.forceGameTimeSynchronization();
        }
        super.tickServer(haveTime);
        int serverViewDistance = Math.max(2, this.minecraft.options.renderDistance().get());
        if (serverViewDistance != this.getPlayerList().getViewDistance()) {
            LOGGER.info("Changing view distance to {}, from {}", (Object)serverViewDistance, (Object)this.getPlayerList().getViewDistance());
            this.getPlayerList().setViewDistance(serverViewDistance);
        }
        if ((serverSimulationDistance = Math.max(2, this.minecraft.options.simulationDistance().get())) != this.previousSimulationDistance) {
            LOGGER.info("Changing simulation distance to {}, from {}", (Object)serverSimulationDistance, (Object)this.previousSimulationDistance);
            this.getPlayerList().setSimulationDistance(serverSimulationDistance);
            this.previousSimulationDistance = serverSimulationDistance;
        }
    }

    @Override
    protected LocalSampleLogger getTickTimeLogger() {
        return this.minecraft.getDebugOverlay().getTickTimeLogger();
    }

    @Override
    public boolean isTickTimeLoggingEnabled() {
        return true;
    }

    private void tickPaused() {
        this.tickConnection();
        for (ServerPlayer player : this.getPlayerList().getPlayers()) {
            player.awardStat(Stats.TOTAL_WORLD_TIME);
        }
    }

    @Override
    public boolean shouldRconBroadcast() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return true;
    }

    @Override
    public Path getServerDirectory() {
        return this.minecraft.gameDirectory.toPath();
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return 0;
    }

    @Override
    public int getCommandSpamThresholdSeconds() {
        return 0;
    }

    @Override
    public int getChatSpamThresholdSeconds() {
        return 0;
    }

    @Override
    public boolean useNativeTransport() {
        return this.minecraft.options.useNativeTransport();
    }

    @Override
    protected void onServerCrash(CrashReport report) {
        BlockableEventLoop.relayDelayCrash(report);
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport systemReport) {
        systemReport.setDetail("Type", "Integrated Server");
        systemReport.setDetail("Is Modded", () -> this.getModdedStatus().fullDescription());
        systemReport.setDetail("Launched Version", this.minecraft::getLaunchedVersion);
        return systemReport;
    }

    @Override
    public ModCheck getModdedStatus() {
        return Minecraft.checkModStatus().merge(super.getModdedStatus());
    }

    @Override
    public boolean publishServer(MinecraftServer.MultiplayerScope scope, boolean allowCommands, int port) {
        this.setGuestCommandAccess(allowCommands);
        return this.publishServer(scope, port);
    }

    public boolean publishServer(MinecraftServer.MultiplayerScope scope, int port) {
        if (scope == MinecraftServer.MultiplayerScope.OFF || this.isPublished()) {
            return false;
        }
        try {
            this.minecraft.prepareForMultiplayer();
            this.minecraft.getConnection().prepareKeyPair();
            this.getConnection().startTcpServerListener(null, port);
            LOGGER.info("Published LAN server on port {}", (Object)port);
            this.publishedPort = port;
            this.lanPinger = new LanServerPinger(this.getMotd(), Integer.toString(port));
            this.lanPinger.start();
            this.setMultiplayerScope(scope);
            return true;
        }
        catch (IOException iOException) {
            return false;
        }
    }

    public void setWorldGameType(GameType gameMode) {
        this.setDefaultGameType(gameMode);
        this.updateGameModeBasedOnPermission();
    }

    public void setPersonalGameType(GameType gameMode) {
        if (this.minecraft.player == null) {
            return;
        }
        ServerPlayer serverPlayer = this.getPlayerList().getPlayer(this.minecraft.player.getUUID());
        if (serverPlayer == null) {
            return;
        }
        serverPlayer.setGameMode(gameMode);
    }

    @Override
    public int enforceGameTypeForPlayers(@Nullable GameType gameType) {
        if (gameType == null) {
            return 0;
        }
        return this.updateGameModeBasedOnPermission();
    }

    public void setWorldAllowCommands(boolean allowCommands) {
        this.getWorldData().setAllowCommands(allowCommands);
        this.updateGameModeBasedOnPermission();
        this.updatePermissions();
    }

    public @Nullable GameType getPersonalGameMode() {
        if (this.minecraft.player == null) {
            return null;
        }
        ServerPlayer serverPlayer = this.getPlayerList().getPlayer(this.minecraft.player.getUUID());
        if (serverPlayer == null) {
            return null;
        }
        return serverPlayer.gameMode();
    }

    private int updateGameModeBasedOnPermission() {
        if (!this.forceGameMode()) {
            return 0;
        }
        int count = 0;
        for (ServerPlayer player : this.getPlayerList().getPlayers()) {
            if (player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || !player.setGameMode(this.getWorldData().getGameType())) continue;
            ++count;
        }
        return count;
    }

    @Override
    public LevelBasedPermissionSet getProfilePermissions(NameAndId nameAndId) {
        if (this.isSingleplayerOwner(nameAndId)) {
            if (this.getPlayerList().isOp(nameAndId)) {
                return LevelBasedPermissionSet.OWNER;
            }
        } else {
            LevelBasedPermissionSet customPermissionLevel = this.getCustomPermissionLevel();
            if (customPermissionLevel != null) {
                return customPermissionLevel;
            }
        }
        return super.getProfilePermissions(nameAndId);
    }

    protected @Nullable LevelBasedPermissionSet getCustomPermissionLevel() {
        if (this.worldData.isAllowCommands()) {
            return this.guestCommandAccess ? LevelBasedPermissionSet.GAMEMASTER : LevelBasedPermissionSet.ALL;
        }
        return null;
    }

    public boolean getGuestCommandAccess() {
        return this.guestCommandAccess;
    }

    public void setGuestCommandAccess(boolean commandAccess) {
        this.guestCommandAccess = commandAccess;
        this.updatePermissions();
        this.updateGameModeBasedOnPermission();
    }

    private void updatePermissions() {
        for (ServerPlayer player : this.getPlayerList().getPlayers()) {
            this.getPlayerList().sendPlayerPermissionLevel(player);
        }
    }

    private void teardownPublishedState() {
        this.stopLanPinger();
        this.publishedPort = -1;
        this.setMultiplayerScope(MinecraftServer.MultiplayerScope.OFF);
    }

    private void stopLanPinger() {
        if (this.lanPinger != null) {
            this.lanPinger.interrupt();
            this.lanPinger = null;
        }
    }

    @Override
    public boolean unpublishServer() {
        if (!this.isPublished()) {
            return false;
        }
        if (this.multiplayerScope == MinecraftServer.MultiplayerScope.LAN) {
            LOGGER.info("Unpublishing integrated server (was on port {})", (Object)this.publishedPort);
        }
        this.getConnection().stopTcpServerListener();
        MutableComponent reason = Component.translatable("multiplayer.disconnect.server_shutdown");
        for (ServerPlayer player : Lists.newArrayList(this.getPlayerList().getPlayers())) {
            if (player.getUUID().equals(this.uuid)) continue;
            player.connection.disconnect(reason);
        }
        this.teardownPublishedState();
        return true;
    }

    @Override
    public void stopServer() {
        this.teardownPublishedState();
        super.stopServer();
    }

    @Override
    public void halt(boolean wait) {
        this.executeBlocking(() -> {
            ArrayList players = Lists.newArrayList(this.getPlayerList().getPlayers());
            for (ServerPlayer player : players) {
                if (player.getUUID().equals(this.uuid)) continue;
                this.getPlayerList().remove(player);
            }
        });
        super.halt(wait);
        this.stopLanPinger();
    }

    @Override
    public boolean isPublished() {
        return this.multiplayerScope != MinecraftServer.MultiplayerScope.OFF;
    }

    @Override
    public void setDefaultGameType(GameType gameType) {
        super.setDefaultGameType(gameType);
        Screen screen = this.minecraft.gui.screen();
        if (screen instanceof WorldOptionsScreen) {
            WorldOptionsScreen worldOptionsScreen = (WorldOptionsScreen)screen;
            worldOptionsScreen.onDefaultGameModeChanged(gameType);
        }
    }

    @Override
    public int getPort() {
        return this.publishedPort;
    }

    @Override
    public LevelBasedPermissionSet operatorUserPermissions() {
        return LevelBasedPermissionSet.GAMEMASTER;
    }

    @Override
    public LevelBasedPermissionSet getFunctionCompilationPermissions() {
        return LevelBasedPermissionSet.GAMEMASTER;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean isSingleplayerOwner(NameAndId nameAndId) {
        return this.getSingleplayerProfile() != null && nameAndId.name().equalsIgnoreCase(this.getSingleplayerProfile().name());
    }

    @Override
    public int getScaledTrackingDistance(int baseRange) {
        return (int)(this.minecraft.options.entityDistanceScaling().get() * (double)baseRange);
    }

    @Override
    public boolean forceSynchronousWrites() {
        return this.minecraft.options.syncWrites;
    }

    @Override
    public @Nullable GameType getForcedGameType() {
        if (this.forceGameMode() && this.isPublished() && !this.isHardcore()) {
            return this.worldData.getGameType();
        }
        return null;
    }

    @Override
    public boolean forceGameMode() {
        return this.forceGameMode;
    }

    @Override
    public void setForceGameMode(boolean forceGameMode) {
        this.forceGameMode = forceGameMode;
        this.enforceGameTypeForPlayers(this.getDefaultGameType());
    }

    @Override
    protected GlobalPos selectLevelLoadFocusPos() {
        UUID lastSinglePlayerOwnerUUID = this.worldData.getSinglePlayerUUID();
        if (lastSinglePlayerOwnerUUID == null) {
            return super.selectLevelLoadFocusPos();
        }
        Optional<CompoundTag> playerData = this.playerDataStorage.load(new NameAndId(lastSinglePlayerOwnerUUID, "<single player owner>"));
        if (playerData.isEmpty()) {
            return super.selectLevelLoadFocusPos();
        }
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER);){
            ValueInput input = TagValueInput.create((ProblemReporter)reporter, (HolderLookup.Provider)this.registryAccess(), playerData.get());
            ServerPlayer.SavedPosition loadedPosition = input.read(ServerPlayer.SavedPosition.MAP_CODEC).orElse(ServerPlayer.SavedPosition.EMPTY);
            if (loadedPosition.dimension().isPresent() && loadedPosition.position().isPresent()) {
                GlobalPos globalPos = new GlobalPos(loadedPosition.dimension().get(), BlockPos.containing(loadedPosition.position().get()));
                return globalPos;
            }
        }
        return super.selectLevelLoadFocusPos();
    }

    @Override
    public void sendLowDiskSpaceWarning() {
        super.sendLowDiskSpaceWarning();
        this.minecraft.sendLowDiskSpaceWarning();
    }

    @Override
    public void reportChunkLoadFailure(Throwable throwable, RegionStorageInfo storageInfo, ChunkPos pos) {
        super.reportChunkLoadFailure(throwable, storageInfo, pos);
        this.warnOnLowDiskSpace();
        this.minecraft.execute(() -> SystemToast.onChunkLoadFailure(this.minecraft, pos));
    }

    @Override
    public void reportChunkSaveFailure(Throwable throwable, RegionStorageInfo storageInfo, ChunkPos pos) {
        super.reportChunkSaveFailure(throwable, storageInfo, pos);
        this.warnOnLowDiskSpace();
        this.minecraft.execute(() -> SystemToast.onChunkSaveFailure(this.minecraft, pos));
    }

    @Override
    public int getMaxPlayers() {
        return 8;
    }

    public Collection<SimpleGizmoCollector.GizmoInstance> getPerTickGizmos() {
        return this.latestTicksGizmos;
    }

    private void setMultiplayerScope(MinecraftServer.MultiplayerScope multiplayerScope) {
        if (this.multiplayerScope == multiplayerScope) {
            return;
        }
        this.multiplayerScope = multiplayerScope;
    }

    public MinecraftServer.MultiplayerScope getMultiplayerScope() {
        return this.multiplayerScope;
    }
}

