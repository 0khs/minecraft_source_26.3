/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogUtils
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.PipelineCache;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.MessageBox;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.io.IOException;
import java.lang.runtime.SwitchBootstraps;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Screenshot;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.GuiBannerResultRenderer;
import net.minecraft.client.gui.render.pip.GuiBookModelRenderer;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.GuiProfilerChartRenderer;
import net.minecraft.client.gui.render.pip.GuiSkinRenderer;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.DebugCrosshairRenderer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.Panorama;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.UiLightmap;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class GameRenderer
implements AutoCloseable,
TrackedWaypoint.Projector,
ResourceManagerReloadListener {
    private static final Identifier BLUR_POST_CHAIN_ID = Identifier.withDefaultNamespace("blur");
    public static Identifier END_OF_FRAME_POST_EFFECT = Identifier.withDefaultNamespace("end_of_frame");
    public static final int MAX_BLUR_RADIUS = 10;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    private final GameRenderState gameRenderState = new GameRenderState();
    public final FirstPersonHandsAndItemsRenderer firstPersonHandsAndItemsRenderer;
    private final ScreenEffectRenderer screenEffectRenderer;
    private final DebugCrosshairRenderer debugCrosshairRenderer;
    private final RenderBuffers renderBuffers;
    private final RenderTarget mainRenderTarget;
    private final RenderTarget hud3DTarget;
    private float bossOverlayWorldDarkening;
    private float bossOverlayWorldDarkeningO;
    private boolean renderBlockOutline = true;
    private long lastScreenshotAttempt;
    private boolean hasWorldScreenshot;
    private final Lightmap lightmap = new Lightmap();
    private final LightmapRenderStateExtractor lightmapRenderStateExtractor;
    private final UiLightmap uiLightmap = new UiLightmap();
    private boolean useUiLightmap;
    private final OverlayTexture overlayTexture = new OverlayTexture();
    protected final Panorama panorama = new Panorama();
    private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);
    private final FogRenderer fogRenderer = new FogRenderer();
    private final GuiRenderer guiRenderer;
    private final FeatureRenderDispatcher featureRenderDispatcher;
    private final SubmitNodeStorage handAndScreenSubmitNodeStorage = new SubmitNodeStorage();
    private @Nullable Identifier spectatedEntityPostEffect;
    private boolean spectatedEntityEffectActive;
    private final Camera mainCamera = new Camera();
    private final Projection hudProjection = new Projection();
    private final Lighting lighting = new Lighting();
    private final GlobalSettingsUniform globalSettingsUniform = new GlobalSettingsUniform();
    private final ProjectionMatrixBuffer levelProjectionMatrixBuffer = new ProjectionMatrixBuffer("level");
    private final ProjectionMatrixBuffer hud3dProjectionMatrixBuffer = new ProjectionMatrixBuffer("3d hud");
    private final List<Identifier> requestedPostEffects = new ArrayList<Identifier>();
    private final List<PostChain> appliedPostEffects = new ArrayList<PostChain>();
    private final List<Identifier> failedPostEffects = new ArrayList<Identifier>();
    private volatile boolean shouldResetFailedPostEffects;

    public GameRenderer(Minecraft minecraft, FirstPersonHandsAndItemsRenderer firstPersonHandsAndItemsRenderer, ModelManager modelManager, ItemModelResolver itemModelResolver) {
        this.minecraft = minecraft;
        this.firstPersonHandsAndItemsRenderer = firstPersonHandsAndItemsRenderer;
        this.lightmapRenderStateExtractor = new LightmapRenderStateExtractor(this, minecraft);
        try {
            int maxSectionBuilders = Runtime.getRuntime().availableProcessors();
            this.renderBuffers = new RenderBuffers(maxSectionBuilders);
        }
        catch (OutOfMemoryError e) {
            MessageBox.error("Oh no! The game was unable to allocate memory off-heap while trying to start. You may try to free some memory by closing other applications on your computer, check that your system meets the minimum requirements, and try again. If the problem persists, please visit: " + String.valueOf(CommonLinks.GENERAL_HELP));
            throw new SilentInitException("Unable to allocate render buffers", e);
        }
        AtlasManager atlasManager = minecraft.getAtlasManager();
        this.featureRenderDispatcher = new FeatureRenderDispatcher(this.renderBuffers, modelManager, atlasManager, minecraft.font, this.gameRenderState);
        this.guiRenderer = new GuiRenderer(this.gameRenderState.guiRenderState, this.featureRenderDispatcher, List.of(new GuiEntityRenderer(minecraft.getEntityRenderDispatcher()), new GuiSkinRenderer(), new GuiBookModelRenderer(), new GuiBannerResultRenderer(atlasManager), new GuiProfilerChartRenderer()));
        this.screenEffectRenderer = new ScreenEffectRenderer(this, atlasManager);
        this.debugCrosshairRenderer = new DebugCrosshairRenderer();
        this.mainRenderTarget = new MainTarget(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        this.hud3DTarget = new TextureTarget("hud_3d_depth", minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight(), null, GpuFormat.D32_FLOAT);
    }

    @Override
    public void close() {
        this.debugCrosshairRenderer.close();
        this.globalSettingsUniform.close();
        this.lightmap.close();
        this.overlayTexture.close();
        this.uiLightmap.close();
        this.resourcePool.close();
        this.guiRenderer.close();
        this.levelProjectionMatrixBuffer.close();
        this.hud3dProjectionMatrixBuffer.close();
        this.lighting.close();
        this.fogRenderer.close();
        this.featureRenderDispatcher.close();
        this.mainRenderTarget.destroyBuffers();
        this.hud3DTarget.destroyBuffers();
        this.renderBuffers.close();
    }

    public RenderBuffers renderBuffers() {
        return this.renderBuffers;
    }

    public FeatureRenderDispatcher featureRenderDispatcher() {
        return this.featureRenderDispatcher;
    }

    public GameRenderState gameRenderState() {
        return this.gameRenderState;
    }

    public void setRenderBlockOutline(boolean renderBlockOutline) {
        this.renderBlockOutline = renderBlockOutline;
    }

    public void clearSpectatedEntityPostEffect() {
        this.spectatedEntityPostEffect = null;
        this.spectatedEntityEffectActive = false;
    }

    public void toggleSpectatorPostEffect() {
        this.spectatedEntityEffectActive = !this.spectatedEntityEffectActive;
    }

    public void checkEntityPostEffect(@Nullable Entity cameraEntity) {
        Entity entity = cameraEntity;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Creeper.class, Spider.class, Enderman.class}, (Entity)entity, n)) {
            case 0: {
                Creeper ignored = (Creeper)entity;
                this.setSpectatedEntityPostEffect(Identifier.withDefaultNamespace("creeper"));
                break;
            }
            case 1: {
                Spider ignored = (Spider)entity;
                this.setSpectatedEntityPostEffect(Identifier.withDefaultNamespace("spider"));
                break;
            }
            case 2: {
                Enderman ignored = (Enderman)entity;
                this.setSpectatedEntityPostEffect(Identifier.withDefaultNamespace("invert"));
                break;
            }
            default: {
                this.clearSpectatedEntityPostEffect();
            }
        }
    }

    private void setSpectatedEntityPostEffect(Identifier id) {
        this.spectatedEntityPostEffect = id;
        this.spectatedEntityEffectActive = true;
    }

    public void processBlurEffect() {
        PostChain postChain = this.minecraft.getShaderManager().getPostChain(BLUR_POST_CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (postChain != null) {
            postChain.process(this.mainRenderTarget, this.resourcePool);
        }
    }

    public static void preloadUiShader(final ResourceManager resourceManager) {
        GpuDevice device = RenderSystem.getDevice();
        final Map<Identifier, ShaderSource.CachedIncludeSource> includes = ShaderManager.listAllIncludes(resourceManager);
        ShaderSource shaderSource = new ShaderSource(){

            @Override
            public @Nullable String getShader(Identifier id, ShaderType type) {
                Identifier location = type.idConverter().idToFile(id);
                try {
                    return resourceManager.getResourceOrThrow(location).readAllAsString();
                }
                catch (Exception exception) {
                    LOGGER.error("Couldn't preload shader {}", (Object)location, (Object)exception);
                    return null;
                }
            }

            @Override
            public @Nullable ShaderSource.CachedIncludeSource getInclude(Identifier id) {
                return (ShaderSource.CachedIncludeSource)includes.get(id);
            }

            @Override
            public void close() {
                includes.values().forEach(ShaderSource.CachedIncludeSource::close);
            }
        };
        RenderSystem.setFallbackPipelineCache(new PipelineCache(device, shaderSource));
        RenderSystem.getCompiledPipeline(RenderPipelines.GUI);
        RenderSystem.getCompiledPipeline(RenderPipelines.GUI_TEXTURED);
        if (TracyClient.isAvailable()) {
            RenderSystem.getCompiledPipeline(RenderPipelines.TRACY_BLIT);
        }
    }

    public void tick() {
        this.lightmapRenderStateExtractor.tick();
        LocalPlayer player = this.minecraft.player;
        if (this.mainCamera.entity() == null) {
            this.mainCamera.setEntity(player);
        }
        this.mainCamera.tick();
        if (!this.minecraft.level.tickRateManager().runsNormally()) {
            return;
        }
        this.bossOverlayWorldDarkeningO = this.bossOverlayWorldDarkening;
        if (this.minecraft.gui.hud.getBossOverlay().shouldDarkenScreen()) {
            this.bossOverlayWorldDarkening += 0.05f;
            if (this.bossOverlayWorldDarkening > 1.0f) {
                this.bossOverlayWorldDarkening = 1.0f;
            }
        } else if (this.bossOverlayWorldDarkening > 0.0f) {
            this.bossOverlayWorldDarkening -= 0.0125f;
        }
        if (player != null) {
            player.itemActivation().tick();
        }
    }

    public @Nullable Identifier spectatedEntityPostEffect() {
        return this.spectatedEntityPostEffect;
    }

    public List<Identifier> getRequestedPostEffects() {
        return this.requestedPostEffects;
    }

    @VisibleForDebug
    public List<Identifier> getAppliedPostEffects() {
        return this.appliedPostEffects.stream().map(PostChain::id).toList();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.shouldResetFailedPostEffects = true;
    }

    public void resize(int width, int height) {
        this.resourcePool.clear();
        this.mainRenderTarget.resize(width, height);
        this.hud3DTarget.resize(width, height);
        this.minecraft.levelRenderer.resize(width, height);
    }

    private void bobHurt(CameraRenderState cameraState, PoseStack poseStack) {
        if (cameraState.entityRenderState.isLiving) {
            float hurt = cameraState.entityRenderState.hurtTime;
            if (cameraState.entityRenderState.isDeadOrDying) {
                float duration = Math.min(cameraState.entityRenderState.deathTime, 20.0f);
                poseStack.rotateDegrees(Axis.ZP, 40.0f - 8000.0f / (duration + 200.0f));
            }
            if (hurt < 0.0f) {
                return;
            }
            hurt /= (float)cameraState.entityRenderState.hurtDuration;
            hurt = Mth.sin(hurt * hurt * hurt * hurt * (float)Math.PI);
            float rr = cameraState.entityRenderState.hurtDir;
            poseStack.rotateDegrees(Axis.YP, -rr);
            float tiltAmount = (float)((double)(-hurt) * 14.0 * this.gameRenderState.optionsRenderState.damageTiltStrength);
            poseStack.rotateDegrees(Axis.ZP, tiltAmount);
            poseStack.rotateDegrees(Axis.YP, rr);
        }
    }

    private void bobView(CameraRenderState cameraState, PoseStack poseStack) {
        if (!cameraState.entityRenderState.isPlayer) {
            return;
        }
        float backwardsInterpolatedWalkDistance = cameraState.entityRenderState.backwardsInterpolatedWalkDistance;
        float bob = cameraState.entityRenderState.bob;
        poseStack.translate(Mth.sin(backwardsInterpolatedWalkDistance * (float)Math.PI) * bob * 0.5f, -Math.abs(Mth.cos(backwardsInterpolatedWalkDistance * (float)Math.PI) * bob), 0.0f);
        poseStack.rotateDegrees(Axis.ZP, Mth.sin(backwardsInterpolatedWalkDistance * (float)Math.PI) * bob * 3.0f);
        poseStack.rotateDegrees(Axis.XP, Math.abs(Mth.cos(backwardsInterpolatedWalkDistance * (float)Math.PI - 0.2f) * bob) * 5.0f);
    }

    private void renderItemInHand(CameraRenderState cameraState, PlayerRenderState playerState, GpuTextureView depthTextureView) {
        if (cameraState.isPanoramicMode) {
            return;
        }
        if (!playerState.hasPlayer || !this.gameRenderState.optionsRenderState.cameraType.isFirstPerson() || cameraState.entityRenderState.isSleeping || this.gameRenderState.guiRenderState.isHudHidden || this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            return;
        }
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.mulPose((Matrix4fc)cameraState.viewRotationMatrix.invert(new Matrix4f()));
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix().mul((Matrix4fc)cameraState.viewRotationMatrix);
        this.bobHurt(cameraState, poseStack);
        if (this.gameRenderState.optionsRenderState.bobView) {
            this.bobView(cameraState, poseStack);
        }
        if (playerState.firstPersonHandsAndItems != null) {
            this.firstPersonHandsAndItemsRenderer.submitHandsWithItems(cameraState.cameraEntityPartialTicks, poseStack, this.handAndScreenSubmitNodeStorage, playerState, playerState.firstPersonHandsAndItems);
        }
        try (FeatureRenderDispatcher.PreparedFrame frame = this.featureRenderDispatcher.prepareFrame(this.handAndScreenSubmitNodeStorage);
             RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Item in hand", this.mainRenderTarget.getColorTextureView(), Optional.empty(), depthTextureView, OptionalDouble.empty());){
            RenderSystem.bindDefaultUniforms(renderPass);
            FeatureRenderDispatcher.renderAllFeatures(renderPass, frame);
        }
        modelViewStack.popMatrix();
        poseStack.popPose();
    }

    public static float nightVisionScale(LivingEntity camera, float a) {
        MobEffectInstance nightVision = camera.getEffect(MobEffects.NIGHT_VISION);
        if (!nightVision.endsWithin(200)) {
            return 1.0f;
        }
        return 0.7f + Mth.sin(((float)nightVision.getDuration() - a) * (float)Math.PI * 0.2f) * 0.3f;
    }

    public void update(DeltaTracker deltaTracker) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("camera");
        this.mainCamera.update(deltaTracker);
        profiler.pop();
        this.requestedPostEffects.clear();
        this.requestedPostEffects.add(END_OF_FRAME_POST_EFFECT);
        if (this.minecraft.player != null) {
            this.requestedPostEffects.addAll(this.minecraft.player.getActivePostEffects());
        }
        if (this.spectatedEntityPostEffect != null && this.spectatedEntityEffectActive) {
            this.requestedPostEffects.add(this.spectatedEntityPostEffect);
        }
    }

    public void extract(DeltaTracker deltaTracker, boolean advanceGameTime) {
        boolean resourcesLoaded = this.minecraft.isGameLoadFinished();
        this.gameRenderState.shouldRenderLevel = resourcesLoaded && advanceGameTime && this.minecraft.level != null;
        float worldPartialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
        this.extractWindow();
        this.extractOptions();
        if (this.gameRenderState.shouldRenderLevel) {
            this.lightmapRenderStateExtractor.extract(this.gameRenderState.lightmapRenderState, 1.0f);
            this.extractCamera(deltaTracker, worldPartialTicks);
            this.minecraft.levelExtractor.extract(deltaTracker, this.mainCamera, worldPartialTicks);
        } else {
            this.gameRenderState.levelRenderState.playerRenderState.reset();
        }
        this.minecraft.gui.extractRenderState(deltaTracker, this.gameRenderState.shouldRenderLevel, resourcesLoaded);
        this.gameRenderState.requestedPostEffects.clear();
        if (this.gameRenderState.levelRenderState.playerRenderState.hasPlayer) {
            for (Identifier postEffect : this.requestedPostEffects) {
                if (this.failedPostEffects.contains(postEffect)) continue;
                this.gameRenderState.requestedPostEffects.add(postEffect);
            }
        }
        this.minecraft.getMetricsRecorder().sampleDuringExtract();
    }

    public void render() {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("render");
        WindowRenderState windowRenderState = this.gameRenderState.windowRenderState;
        if (windowRenderState.width != this.mainRenderTarget.width || windowRenderState.height != this.mainRenderTarget.height) {
            this.resize(windowRenderState.width, windowRenderState.height);
        }
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(this.mainRenderTarget.getColorTexture(), (Vector4fc)this.gameRenderState.guiRenderState.clearColorOverride, this.mainRenderTarget.getDepthTexture(), 0.0);
        this.globalSettingsUniform.update(windowRenderState.width, windowRenderState.height, this.gameRenderState.optionsRenderState.glintStrength, this.gameRenderState.shouldRenderLevel ? this.gameRenderState.levelRenderState.gameTime : 0L, this.gameRenderState.shouldRenderLevel ? this.gameRenderState.levelRenderState.worldPartialTicks : 0.0f, this.gameRenderState.optionsRenderState.menuBackgroundBlurriness, this.gameRenderState.levelRenderState.cameraRenderState.pos, this.gameRenderState.optionsRenderState.textureFiltering == TextureFilteringMethod.RGSS);
        if (this.gameRenderState.shouldRenderLevel) {
            this.preparePostEffects(this.gameRenderState.requestedPostEffects);
            this.lightmap.render(this.gameRenderState.lightmapRenderState);
            profiler.push("world");
            this.renderLevel();
            this.tryTakeScreenshotIfNeeded();
            this.minecraft.levelRenderer.blitEntityOutline();
            this.applyPostEffects();
            profiler.pop();
        } else {
            this.preparePostEffects(Collections.emptyList());
        }
        this.fogRenderer.endFrame();
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(this.mainRenderTarget.getDepthTexture(), 0.0);
        this.lighting().setupFor(Lighting.Entry.ITEMS_3D);
        this.useUiLightmap = true;
        profiler.push("gui");
        this.guiRenderer.render();
        this.guiRenderer.endFrame();
        profiler.pop();
        this.useUiLightmap = false;
        this.renderBuffers.endFrame();
        this.resourcePool.endFrame();
        profiler.pop();
    }

    private void preparePostEffects(List<Identifier> requestedPostEffects) {
        HashSet<PostChain> previousPostEffects = new HashSet<PostChain>(this.appliedPostEffects);
        this.appliedPostEffects.clear();
        if (this.shouldResetFailedPostEffects) {
            this.failedPostEffects.clear();
            this.shouldResetFailedPostEffects = false;
        }
        for (Identifier identifier : requestedPostEffects) {
            try {
                ShaderManager shaderManager = this.minecraft.getShaderManager();
                if (!shaderManager.isPostEffectValid(identifier, LevelTargetBundle.MAIN_TARGETS)) {
                    this.failedPostEffects.add(identifier);
                    continue;
                }
                PostChain postChain = shaderManager.getPostChain(identifier, LevelTargetBundle.MAIN_TARGETS);
                if (postChain == null) continue;
                this.appliedPostEffects.add(postChain);
                previousPostEffects.remove(postChain);
            }
            catch (RuntimeException e) {
                LOGGER.warn("Failed to load post effect {}", (Object)identifier, (Object)e);
                this.failedPostEffects.add(identifier);
            }
        }
        for (PostChain postChain : previousPostEffects) {
            postChain.closePersistentTargets();
        }
    }

    private void applyPostEffects() {
        for (PostChain postChain : this.appliedPostEffects) {
            try {
                postChain.process(this.mainRenderTarget, this.resourcePool);
            }
            catch (RuntimeException e) {
                LOGGER.warn("Failed to apply post effect {}", (Object)postChain.id(), (Object)e);
                this.failedPostEffects.add(postChain.id());
            }
        }
    }

    private void tryTakeScreenshotIfNeeded() {
        if (this.hasWorldScreenshot || !this.minecraft.isLocalServer()) {
            return;
        }
        long time = Util.getMillis();
        if (time - this.lastScreenshotAttempt < 1000L) {
            return;
        }
        this.lastScreenshotAttempt = time;
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (server == null || server.isStopped()) {
            return;
        }
        server.getWorldScreenshotFile().ifPresent(path -> {
            if (Files.isRegularFile(path, new LinkOption[0])) {
                this.hasWorldScreenshot = true;
            } else {
                this.takeAutoScreenshot((Path)path);
            }
        });
    }

    private void takeAutoScreenshot(Path screenshotFile) {
        if (this.minecraft.levelExtractor.countRenderedSections() > 10 && this.minecraft.levelRenderer.hasRenderedAllSections()) {
            Screenshot.takeScreenshot(this.mainRenderTarget, screenshot -> Util.ioPool().execute(() -> {
                int width = screenshot.getWidth();
                int height = screenshot.getHeight();
                int x = 0;
                int y = 0;
                if (width > height) {
                    x = (width - height) / 2;
                    width = height;
                } else {
                    y = (height - width) / 2;
                    height = width;
                }
                try (NativeImage scaled = new NativeImage(64, 64, false);){
                    screenshot.resizeSubRectTo(x, y, width, height, scaled);
                    scaled.writeToFile(screenshotFile);
                }
                catch (IOException e) {
                    LOGGER.warn("Couldn't save auto screenshot", (Throwable)e);
                }
                finally {
                    screenshot.close();
                }
            }));
        }
    }

    private boolean shouldRenderBlockOutline() {
        boolean renderOutline;
        if (!this.renderBlockOutline) {
            return false;
        }
        Entity cameraEntity = this.minecraft.getCameraEntity();
        boolean bl = renderOutline = cameraEntity instanceof Player && !this.minecraft.gui.hud.isHidden();
        if (renderOutline && !((Player)cameraEntity).getAbilities().mayBuild) {
            ItemStack itemStack = ((LivingEntity)cameraEntity).getMainHandItem();
            HitResult hitResult = this.minecraft.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                BlockPos pos = blockHitResult.getBlockPos();
                if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                    BlockState blockState = this.minecraft.level.getBlockState(pos);
                    renderOutline = blockState.showAsInteractableInSpectatorMode(this.minecraft.level, pos, blockHitResult);
                } else {
                    BlockInWorld blockInWorld = new BlockInWorld(this.minecraft.level, pos, false);
                    renderOutline = !itemStack.isEmpty() && (itemStack.canBreakBlockInAdventureMode(blockInWorld) || itemStack.canPlaceOnBlockInAdventureMode(blockInWorld));
                }
            }
        }
        return renderOutline;
    }

    public void renderLevel() {
        PlayerRenderState playerState = this.gameRenderState.levelRenderState.playerRenderState;
        ProfilerFiller profiler = Profiler.get();
        boolean renderOutline = this.shouldRenderBlockOutline();
        OptionsRenderState optionsState = this.gameRenderState.optionsRenderState;
        CameraRenderState cameraState = this.gameRenderState.levelRenderState.cameraRenderState;
        profiler.push("matrices");
        Matrix4f projectionMatrix = new Matrix4f((Matrix4fc)cameraState.projectionMatrix);
        PoseStack bobStack = new PoseStack();
        this.bobHurt(cameraState, bobStack);
        if (optionsState.bobView) {
            this.bobView(cameraState, bobStack);
        }
        projectionMatrix.mul((Matrix4fc)bobStack.last().pose());
        float worldPartialTicks = this.gameRenderState.levelRenderState.worldPartialTicks;
        float screenEffectScale = optionsState.screenEffectScale;
        float portalIntensity = playerState.portalEffectIntensity;
        float nauseaIntensity = playerState.nauseaEffectIntensity;
        float spinningEffectIntensity = Math.max(portalIntensity, nauseaIntensity) * (screenEffectScale * screenEffectScale);
        if (spinningEffectIntensity > 0.0f) {
            float skew = 5.0f / (spinningEffectIntensity * spinningEffectIntensity + 5.0f) - spinningEffectIntensity * 0.04f;
            skew *= skew;
            Vector3f axis = new Vector3f(0.0f, Mth.SQRT_OF_TWO / 2.0f, Mth.SQRT_OF_TWO / 2.0f);
            float angle = playerState.spinningEffectAngle * ((float)Math.PI / 180);
            projectionMatrix.rotate(angle, (Vector3fc)axis);
            projectionMatrix.scale(1.0f / skew, 1.0f, 1.0f);
            projectionMatrix.rotate(-angle, (Vector3fc)axis);
        }
        RenderSystem.setProjectionMatrix(this.levelProjectionMatrixBuffer.getBuffer(projectionMatrix), ProjectionType.PERSPECTIVE);
        profiler.popPush("fog");
        this.fogRenderer.updateBuffer(cameraState.fogData);
        GpuBufferSlice terrainFog = this.fogRenderer.getBuffer(FogRenderer.FogMode.WORLD);
        profiler.popPush("level");
        boolean shouldCreateBossFog = this.minecraft.gui.hud.getBossOverlay().shouldCreateWorldFog();
        boolean consistentDepthRequired = !this.appliedPostEffects.isEmpty();
        this.minecraft.levelRenderer.render(this.resourcePool, renderOutline, cameraState, terrainFog, cameraState.fogData.color, !shouldCreateBossFog, consistentDepthRequired);
        this.render3dHud(cameraState, playerState, optionsState, consistentDepthRequired);
    }

    private void render3dHud(CameraRenderState cameraState, PlayerRenderState playerState, OptionsRenderState optionsState, boolean consistentDepthRequired) {
        GpuTextureView depthTextureView;
        ProfilerFiller profiler;
        block14: {
            profiler = Profiler.get();
            profiler.popPush("hand");
            this.hudProjection.setupPerspective(0.05f, cameraState.depthFar, cameraState.hudFov, this.gameRenderState.windowRenderState.width, this.gameRenderState.windowRenderState.height);
            RenderSystem.setProjectionMatrix(this.hud3dProjectionMatrixBuffer.getBuffer(this.hudProjection), ProjectionType.PERSPECTIVE);
            GpuTexture depthTexture = consistentDepthRequired ? this.hud3DTarget.getDepthTexture() : this.mainRenderTarget.getDepthTexture();
            depthTextureView = consistentDepthRequired ? this.hud3DTarget.getDepthTextureView() : this.mainRenderTarget.getDepthTextureView();
            RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(depthTexture, 0.0);
            this.renderItemInHand(cameraState, playerState, depthTextureView);
            profiler.popPush("screenEffects");
            this.screenEffectRenderer.submit(this.gameRenderState.levelRenderState.worldPartialTicks, this.handAndScreenSubmitNodeStorage, playerState, cameraState, this.gameRenderState.guiRenderState.isHudHidden);
            try (FeatureRenderDispatcher.PreparedFrame frame = this.featureRenderDispatcher.prepareFrame(this.handAndScreenSubmitNodeStorage);){
                if (frame.isEmpty()) break block14;
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Screen effects", this.mainRenderTarget.getColorTextureView(), Optional.empty(), depthTextureView, OptionalDouble.empty());){
                    RenderSystem.bindDefaultUniforms(renderPass);
                    FeatureRenderDispatcher.renderAllFeatures(renderPass, frame);
                }
            }
        }
        profiler.pop();
        RenderSystem.setShaderFog(this.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        if (this.gameRenderState.levelRenderState.render3dCrosshair && optionsState.cameraType.isFirstPerson() && !this.gameRenderState.guiRenderState.isHudHidden) {
            this.debugCrosshairRenderer.render(cameraState, this.gameRenderState.windowRenderState.guiScale, this.mainRenderTarget.getColorTextureView(), depthTextureView);
        }
        if (consistentDepthRequired) {
            this.integrate3DHudDepth();
        }
    }

    private void integrate3DHudDepth() {
        RenderPassDescriptor integrateDepthDescriptor = RenderPassDescriptor.builder(() -> "Integrate 3d hud depth").withDepthAttachment(this.mainRenderTarget.getDepthTextureView()).build();
        GpuSampler nearestSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(integrateDepthDescriptor);){
            renderPass.setUniform("InSampler", this.hud3DTarget.getDepthTextureView(), nearestSampler);
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.INTEGRATE_DEPTH));
            renderPass.draw(3, 1, 0, 0);
        }
    }

    private void extractWindow() {
        WindowRenderState windowState = this.gameRenderState.windowRenderState;
        Window window = this.minecraft.getWindow();
        windowState.width = window.getWidth();
        windowState.height = window.getHeight();
        windowState.guiScale = window.getGuiScale();
        windowState.appropriateLineWidth = window.getAppropriateLineWidth();
        windowState.isMinimized = window.isIconified();
    }

    private void extractOptions() {
        OptionsRenderState optionsState = this.gameRenderState.optionsRenderState;
        Options options = this.minecraft.options;
        optionsState.cloudRange = options.cloudRange().get();
        optionsState.cutoutLeaves = options.cutoutLeaves().get();
        optionsState.improvedTransparency = options.improvedTransparency().get();
        optionsState.ambientOcclusion = options.ambientOcclusion().get();
        optionsState.menuBackgroundBlurriness = options.getMenuBackgroundBlurriness();
        optionsState.panoramaSpeed = options.panoramaSpeed().get();
        optionsState.maxAnisotropyValue = options.maxAnisotropyValue();
        optionsState.textureFiltering = options.textureFiltering().get();
        optionsState.bobView = options.bobView().get();
        optionsState.screenEffectScale = options.screenEffectScale().get().floatValue();
        optionsState.glintSpeed = options.glintSpeed().get();
        optionsState.glintStrength = options.glintStrength().get();
        optionsState.damageTiltStrength = options.damageTiltStrength().get();
        optionsState.backgroundForChatOnly = options.backgroundForChatOnly().get();
        optionsState.textBackgroundOpacity = options.textBackgroundOpacity().get().floatValue();
        optionsState.cloudStatus = options.getCloudStatus();
        optionsState.cameraType = options.getCameraType();
        optionsState.renderDistance = options.getEffectiveRenderDistance();
        optionsState.chunkSectionFadeInTime = options.chunkSectionFadeInTime().get();
        optionsState.prioritizeChunkUpdates = options.prioritizeChunkUpdates().get();
        optionsState.fov = options.fov().get();
    }

    private void extractCamera(DeltaTracker deltaTracker, float worldPartialTicks) {
        CameraRenderState cameraState = this.gameRenderState.levelRenderState.cameraRenderState;
        this.mainCamera.extractRenderState(cameraState, deltaTracker);
        cameraState.fogType = this.mainCamera.getFluidInCamera();
        cameraState.fogData = this.fogRenderer.setupFog(this.mainCamera, this.minecraft.options.getEffectiveRenderDistance(), deltaTracker, this.bossOverlayWorldDarkening(worldPartialTicks), this.minecraft.level);
    }

    public void resetData() {
        if (this.minecraft.player != null) {
            this.minecraft.player.resetItemActivation();
        }
        this.minecraft.getMapTextureManager().resetData();
        this.mainCamera.reset();
        this.hasWorldScreenshot = false;
    }

    public float bossOverlayWorldDarkening(float a) {
        return Mth.lerp(a, this.bossOverlayWorldDarkeningO, this.bossOverlayWorldDarkening);
    }

    public Camera mainCamera() {
        return this.mainCamera;
    }

    public GpuTextureView lightmap() {
        return this.useUiLightmap ? this.uiLightmap.getTextureView() : this.lightmap.getTextureView();
    }

    public GpuTextureView levelLightmap() {
        return this.lightmap.getTextureView();
    }

    public OverlayTexture overlayTexture() {
        return this.overlayTexture;
    }

    public RenderTarget mainRenderTarget() {
        return this.mainRenderTarget;
    }

    @Override
    public Vec3 projectPointToScreen(Vec3 point) {
        Matrix4f mvp = this.mainCamera.getViewRotationProjectionMatrix(new Matrix4f());
        Vec3 camPos = this.mainCamera.position();
        Vec3 offset = point.subtract(camPos);
        Vector3f vector3f = mvp.transformProject(offset.toVector3f());
        return new Vec3((Vector3fc)vector3f);
    }

    @Override
    public double projectHorizonToScreen() {
        float xRot = this.mainCamera.xRot();
        if (xRot <= -90.0f) {
            return Double.NEGATIVE_INFINITY;
        }
        if (xRot >= 90.0f) {
            return Double.POSITIVE_INFINITY;
        }
        float fov = this.mainCamera.getFov();
        return Math.tan(xRot * ((float)Math.PI / 180)) / Math.tan(fov / 2.0f * ((float)Math.PI / 180));
    }

    public Lighting lighting() {
        return this.lighting;
    }

    public void setLevel(@Nullable ClientLevel level) {
        if (level != null) {
            this.lighting.updateLevel(level.dimensionType().cardinalLightType());
        }
        this.mainCamera.setLevel(level);
    }

    public Panorama panorama() {
        return this.panorama;
    }

    public void registerPanoramaTextures(TextureManager textureManager) {
        this.guiRenderer.registerPanoramaTextures(textureManager);
    }

    public boolean useImprovedTransparency() {
        return this.gameRenderState.optionsRenderState.improvedTransparency && !this.gameRenderState.levelRenderState.renderWireframeTerrain;
    }
}

