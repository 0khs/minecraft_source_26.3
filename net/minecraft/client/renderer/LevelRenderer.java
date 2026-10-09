/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.LongCollection
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator
 *  it.unimi.dsi.fastutil.objects.ReferenceArrayList
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.device.DeviceFeatures;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.DeviceLimits;
import com.mojang.renderpearl.api.device.HintsAndWorkarounds;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.SequencedCollection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.DynamicGpuData;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.oit.OitRenderPassProvider;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SectionUpdateRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.state.level.TransientBlockRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public class LevelRenderer
implements AutoCloseable {
    public static final int OIT_WAVELET_RANK = 2;
    public static final int OIT_COEFFICIENT_COUNT = Math.powExact((int)2, (int)3);
    public static final int OIT_TRANSMITTANCE_TARGET_COUNT = OIT_COEFFICIENT_COUNT / 4;
    private static final Identifier ENTITY_OUTLINE_POST_CHAIN_ID = Identifier.withDefaultNamespace("entity_outline");
    private static final int MINIMUM_TRANSPARENT_SORT_COUNT = 15;
    private static final float CHUNK_VISIBILITY_THRESHOLD = 0.3f;
    private static final Vector4fc DEPTH_BOUNDS_CLEAR_COLOR = new Vector4f(-3.4028235E38f, 0.0f, 0.0f, 0.0f);
    private static final Vector4fc ZERO_CLEAR_COLOR = new Vector4f(0.0f);
    private final GameRenderer gameRenderer;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final RenderBuffers renderBuffers;
    private final FeatureRenderDispatcher featureRenderDispatcher;
    private final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
    private final ModelManager modelManager;
    private final TextureManager textureManager;
    private final AtlasManager atlasManager;
    private final ShaderManager shaderManager;
    private final LevelRenderState levelRenderState;
    private final OptionsRenderState optionsRenderState;
    private @Nullable SkyRenderer skyRenderer;
    private final CloudRenderer cloudRenderer = new CloudRenderer();
    private final WorldBorderRenderer worldBorderRenderer = new WorldBorderRenderer();
    private final WeatherEffectRenderer weatherEffectRenderer = new WeatherEffectRenderer();
    private final SectionOcclusionGraph sectionOcclusionGraph = new SectionOcclusionGraph();
    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections = new ObjectArrayList(10000);
    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> nearbyVisibleSections = new ObjectArrayList(50);
    private final Long2ObjectLinkedOpenHashMap<TransientBlockRenderState> transientBlocks = new Long2ObjectLinkedOpenHashMap();
    private final BlockingQueue<TransientBlockRenderState.Removal> transientBlockRemovalQueue = new LinkedBlockingQueue<TransientBlockRenderState.Removal>();
    private @Nullable ViewArea viewArea;
    private final RenderTarget entityOutlineTarget;
    private final LevelTargetBundle targets = new LevelTargetBundle();
    private @Nullable SectionRenderDispatcher sectionRenderDispatcher;
    private @Nullable BlockPos lastTranslucentSortBlockPos;
    private int translucencyResortIterationIndex;
    private @Nullable GpuSampler chunkLayerSampler;
    private boolean currentFrameRendersEntityOutline;
    private final boolean multiDrawIndirectAvailable;
    private boolean usingMultiDrawIndirectForTerrain;
    private final SimpleGizmoCollector renderThreadGizmos = new SimpleGizmoCollector();
    private FinalizedGizmos finalizedGizmos = new FinalizedGizmos(new DrawableGizmoPrimitives(), new DrawableGizmoPrimitives());

    public LevelRenderer(EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, ModelManager modelManager, TextureManager textureManager, AtlasManager atlasManager, ShaderManager shaderManager, GameRenderer gameRenderer, int width, int height) {
        this.gameRenderer = gameRenderer;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.renderBuffers = gameRenderer.renderBuffers();
        this.featureRenderDispatcher = gameRenderer.featureRenderDispatcher();
        this.modelManager = modelManager;
        this.textureManager = textureManager;
        this.atlasManager = atlasManager;
        this.shaderManager = shaderManager;
        this.levelRenderState = gameRenderer.gameRenderState().levelRenderState;
        this.optionsRenderState = gameRenderer.gameRenderState().optionsRenderState;
        this.entityOutlineTarget = new TextureTarget("Entity Outline", width, height, GpuFormat.RGBA8_UNORM, null);
        DeviceInfo deviceInfo = RenderSystem.getDevice().getDeviceInfo();
        DeviceFeatures deviceFeatures = deviceInfo.features();
        DeviceLimits deviceLimits = deviceInfo.limits();
        HintsAndWorkarounds hintsAndWorkarounds = deviceInfo.hintsAndWorkarounds();
        this.usingMultiDrawIndirectForTerrain = this.multiDrawIndirectAvailable = deviceLimits.maxDrawIndirectDrawCount() > 0 && deviceFeatures.nonZeroFirstInstance() && !hintsAndWorkarounds.multiDrawIndirectHasKnownIssues();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired) {
        long chunkFadeDuration;
        PostChain entityOutlineChain;
        RenderSystem.isRenderingLevel = true;
        final ProfilerFiller profiler = Profiler.get();
        this.submitNodeStorage.setUseImprovedTransparency(this.gameRenderer.useImprovedTransparency());
        profiler.push("repositionCamera");
        this.repositionCamera(cameraState);
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.mul((Matrix4fc)cameraState.viewRotationMatrix);
        profiler.popPush("submitFeatures");
        this.submitFeatures(this.levelRenderState, this.submitNodeStorage, renderOutline);
        profiler.popPush("prepareFeatures");
        FeatureRenderDispatcher.PreparedFrame featureFrame = this.featureRenderDispatcher.prepareFrame(this.submitNodeStorage);
        this.currentFrameRendersEntityOutline = featureFrame.hasAnyOutline() && this.levelRenderState.shouldShowEntityOutlines;
        profiler.popPush("setupFrameGraph");
        FrameGraphBuilder frame = new FrameGraphBuilder();
        this.targets.main = frame.importExternal("main", this.gameRenderer.mainRenderTarget());
        int screenWidth = this.gameRenderer.mainRenderTarget().width;
        int screenHeight = this.gameRenderer.mainRenderTarget().height;
        RenderTargetDescriptor extraDepthTargetDescriptor = new RenderTargetDescriptor(screenWidth, screenHeight, null, new RenderTargetDescriptor.TextureProperties(null, GpuFormat.D32_FLOAT));
        RenderTargetDescriptor extraDepthTargetDescriptorCleared = new RenderTargetDescriptor(screenWidth, screenHeight, null, RenderTargetDescriptor.TextureProperties.DEFAULT_DEPTH);
        if (this.gameRenderer.useImprovedTransparency()) {
            RenderTargetDescriptor depthBoundsTargetDescriptor = new RenderTargetDescriptor(screenWidth, screenHeight, new RenderTargetDescriptor.TextureProperties(DEPTH_BOUNDS_CLEAR_COLOR, GpuFormat.RGBA32_FLOAT), null);
            this.targets.depthBounds = frame.createInternal("depth_bounds", depthBoundsTargetDescriptor);
            RenderTargetDescriptor depthBoundsCulledTargetDescriptor = new RenderTargetDescriptor(screenWidth, screenHeight, new RenderTargetDescriptor.TextureProperties(null, GpuFormat.RGBA32_FLOAT), null);
            this.targets.depthBoundsCulled = frame.createInternal("depth_bounds_culled", depthBoundsCulledTargetDescriptor);
            RenderTargetDescriptor transmittanceTargetDescriptor = new RenderTargetDescriptor(screenWidth, screenHeight, new RenderTargetDescriptor.TextureProperties(ZERO_CLEAR_COLOR, GpuFormat.RGBA16_FLOAT), null);
            for (int i = 0; i < OIT_TRANSMITTANCE_TARGET_COUNT; ++i) {
                this.targets.transmittance.set(i, frame.createInternal("transmittance", transmittanceTargetDescriptor));
            }
            RenderTargetDescriptor accumulateTargetDescriptor = new RenderTargetDescriptor(screenWidth, screenHeight, new RenderTargetDescriptor.TextureProperties(ZERO_CLEAR_COLOR, GpuFormat.RGBA16_FLOAT), null);
            this.targets.accumulate = frame.createInternal("accumulate", accumulateTargetDescriptor);
            this.targets.oitCloudDepth = frame.createInternal("cloud_depth", extraDepthTargetDescriptor);
            this.targets.oitTerrainWithWaterPatchDepth = frame.createInternal("terrain_depth", extraDepthTargetDescriptor);
        }
        if (this.frameHasAlwaysOnTopGizmos() && consistentDepthRequired) {
            this.targets.alwaysOnTopDepth = frame.createInternal("always_on_top_depth", extraDepthTargetDescriptorCleared);
        }
        this.targets.entityOutline = frame.importExternal("entity_outline", this.entityOutlineTarget);
        FramePass clearPass = frame.addPass("clear");
        this.targets.main = clearPass.readsAndWrites(this.targets.main);
        clearPass.executes(() -> {
            RenderTarget mainRenderTarget = this.gameRenderer.mainRenderTarget();
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(mainRenderTarget.getColorTexture(), (Vector4fc)new Vector4f(fogColor.x, fogColor.y, fogColor.z, 0.0f), mainRenderTarget.getDepthTexture(), 0.0);
        });
        if (shouldRenderSky) {
            this.addSkyPass(frame, cameraState, terrainFog);
        }
        Matrix4f terrainMatrix = new Matrix4f((Matrix4fc)this.levelRenderState.cameraRenderState.viewRotationMatrix);
        boolean bl = this.usingMultiDrawIndirectForTerrain = this.multiDrawIndirectAvailable && this.levelRenderState.shouldUseMultiDrawIndirectForTerrain;
        ChunkSectionsToRender chunkSectionsToRender = this.usingMultiDrawIndirectForTerrain ? this.prepareChunkRendersIndirect((Matrix4fc)terrainMatrix, !this.gameRenderer.useImprovedTransparency()) : this.prepareChunkRenders((Matrix4fc)terrainMatrix, !this.gameRenderer.useImprovedTransparency());
        this.addMainPass(frame, featureFrame, terrainFog, chunkSectionsToRender, consistentDepthRequired);
        if (this.currentFrameRendersEntityOutline && (entityOutlineChain = this.shaderManager.getPostChain(ENTITY_OUTLINE_POST_CHAIN_ID, LevelTargetBundle.OUTLINE_TARGETS)) != null) {
            entityOutlineChain.addToFrame(frame, screenWidth, screenHeight, this.targets);
        }
        profiler.popPush("executeFrameGraph");
        frame.execute(resourceAllocator, new FrameGraphBuilder.Inspector(){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public void beforeExecutePass(String name) {
                profiler.push(name);
            }

            @Override
            public void afterExecutePass(String name) {
                profiler.pop();
            }
        });
        profiler.pop();
        this.targets.clear();
        modelViewStack.popMatrix();
        featureFrame.close();
        profiler.push("compileSections");
        this.compileSections(cameraState);
        profiler.pop();
        if (this.sectionRenderDispatcher != null) {
            this.sectionRenderDispatcher.lock();
            profiler.push("uploadTerrainBuffers");
            try {
                this.sectionRenderDispatcher.uploadTerrainBuffersToGpu();
            }
            finally {
                this.sectionRenderDispatcher.unlock();
            }
            profiler.pop();
        }
        profiler.push("updateSectionOcclusion");
        this.sectionOcclusionGraph.update(cameraState, this.optionsRenderState.fov, this.levelRenderState.chunkLoadingRenderState);
        profiler.pop();
        Runnable playerCompiledSectionCallback = this.levelRenderState.playerCompiledSectionCallback;
        if (playerCompiledSectionCallback != null && this.isSectionCompiledAndVisible(this.levelRenderState.cameraRenderState.blockPos, chunkFadeDuration = Util.toMillis(this.optionsRenderState.chunkSectionFadeInTime))) {
            playerCompiledSectionCallback.run();
        }
        RenderSystem.isRenderingLevel = false;
    }

    private void submitFeatures(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, boolean renderOutline) {
        PoseStack poseStack = new PoseStack();
        this.submitEntities(poseStack, levelRenderState, submitNodeCollector);
        levelRenderState.entityRenderStates.clear();
        this.submitBlockEntities(poseStack, levelRenderState, submitNodeCollector);
        levelRenderState.blockEntityRenderStates.clear();
        this.submitBlockDestroyAnimation(poseStack, submitNodeCollector, levelRenderState);
        levelRenderState.blockBreakingRenderStates.clear();
        this.submitTransientBlocks(poseStack, submitNodeCollector, levelRenderState);
        levelRenderState.particlesRenderState.submit(submitNodeCollector, levelRenderState.cameraRenderState);
        if (renderOutline) {
            this.submitBlockOutline(poseStack, this.submitNodeStorage, levelRenderState);
        }
        this.finalizeGizmoCollection();
        this.finalizedGizmos.standardPrimitives().submit(submitNodeCollector, levelRenderState.cameraRenderState, false);
        this.finalizedGizmos.alwaysOnTopPrimitives().submit(submitNodeCollector, levelRenderState.cameraRenderState, true);
        if (!levelRenderState.shouldShowEntityOutlines) {
            for (SubmitNodeCollection collection : this.submitNodeStorage.getSubmitsPerOrder().values()) {
                collection.outline.clear();
            }
        }
        this.checkPoseStack(poseStack);
    }

    private void repositionCamera(CameraRenderState camera) {
        Vec3 cameraPos = camera.pos;
        SectionPos cameraSectionPos = SectionPos.of(cameraPos);
        if (this.viewArea.repositionCamera(cameraSectionPos)) {
            this.worldBorderRenderer.invalidate();
        }
        this.sectionRenderDispatcher.setCameraPosition(cameraPos);
    }

    private void addSkyPass(FrameGraphBuilder frame, CameraRenderState cameraState, GpuBufferSlice skyFog) {
        FogType fogType = cameraState.fogType;
        if (fogType == FogType.POWDER_SNOW || fogType == FogType.LAVA || cameraState.entityRenderState.doesMobEffectBlockSky) {
            return;
        }
        if (this.levelRenderState.shouldResetSkyRenderer || this.skyRenderer == null) {
            if (this.skyRenderer != null) {
                this.skyRenderer.close();
            }
            this.skyRenderer = new SkyRenderer(this.textureManager, this.atlasManager, this.gameRenderer.mainRenderTarget());
        }
        SkyRenderState state = this.levelRenderState.skyRenderState;
        if (state.skybox == DimensionType.Skybox.NONE) {
            return;
        }
        FramePass pass = frame.addPass("sky");
        this.targets.main = pass.readsAndWrites(this.targets.main);
        pass.executes(() -> this.skyRenderer.render(skyFog, state));
    }

    private void addMainPass(FrameGraphBuilder frame, FeatureRenderDispatcher.PreparedFrame featureFrame, GpuBufferSlice terrainFog, ChunkSectionsToRender chunkSectionsToRender, boolean consistentDepthRequired) {
        boolean hasAlwaysOnTopGizmos;
        FramePass pass = frame.addPass("main");
        this.targets.main = pass.readsAndWrites(this.targets.main);
        boolean useImprovedTransparency = this.gameRenderer.useImprovedTransparency();
        if (useImprovedTransparency) {
            this.targets.depthBounds = pass.readsAndWrites(this.targets.depthBounds);
            this.targets.depthBoundsCulled = pass.readsAndWrites(this.targets.depthBoundsCulled);
            for (int i = 0; i < OIT_TRANSMITTANCE_TARGET_COUNT; ++i) {
                this.targets.transmittance.set(i, pass.readsAndWrites(this.targets.transmittance.get(i)));
            }
            this.targets.accumulate = pass.readsAndWrites(this.targets.accumulate);
            if (this.optionsRenderState.cloudStatus != CloudStatus.OFF && ARGB.alpha(this.levelRenderState.cloudColor) > 0) {
                this.targets.oitCloudDepth = pass.readsAndWrites(this.targets.oitCloudDepth);
            }
            if (featureFrame.hasAnyWaterMask()) {
                this.targets.oitTerrainWithWaterPatchDepth = pass.readsAndWrites(this.targets.oitTerrainWithWaterPatchDepth);
            }
        }
        if ((hasAlwaysOnTopGizmos = this.frameHasAlwaysOnTopGizmos()) && consistentDepthRequired) {
            this.targets.alwaysOnTopDepth = pass.readsAndWrites(this.targets.alwaysOnTopDepth);
        }
        if (this.currentFrameRendersEntityOutline && this.targets.entityOutline != null) {
            this.targets.entityOutline = pass.readsAndWrites(this.targets.entityOutline);
        }
        pass.executes(() -> {
            RenderSystem.setShaderFog(terrainFog);
            if (this.levelRenderState.shouldResetChunkLayerSampler || this.chunkLayerSampler == null) {
                if (this.chunkLayerSampler != null) {
                    this.chunkLayerSampler.close();
                }
                int maxAnisotropy = this.optionsRenderState.textureFiltering == TextureFilteringMethod.ANISOTROPIC ? this.optionsRenderState.maxAnisotropyValue : 1;
                this.chunkLayerSampler = RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, maxAnisotropy, OptionalDouble.empty());
            }
            this.prepareTranslucents();
            this.gameRenderer.lighting().setupFor(Lighting.Entry.LEVEL);
            RenderTarget mainTarget = this.targets.main.get();
            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> useImprovedTransparency ? "Solid" : "Main", mainTarget.getColorTextureView(), Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty());){
                RenderSystem.bindDefaultUniforms(renderPass);
                this.executeSolid(chunkSectionsToRender, featureFrame, renderPass);
                if (!useImprovedTransparency) {
                    this.executeClassicTransparency(chunkSectionsToRender, featureFrame, renderPass);
                }
            }
            if (useImprovedTransparency) {
                this.executeOit(chunkSectionsToRender, featureFrame);
            }
            this.executeOutline(featureFrame);
            if (featureFrame.hasAnySeeThrough()) {
                this.executeSeeThrough(featureFrame, mainTarget);
            }
            if (hasAlwaysOnTopGizmos) {
                this.executeAlwaysOnTop(featureFrame, mainTarget, consistentDepthRequired);
            }
        });
    }

    private void executeSeeThrough(FeatureRenderDispatcher.PreparedFrame featureFrame, RenderTarget mainTarget) {
        RenderPassDescriptor descriptor = RenderPassDescriptor.builder(() -> "See through features").withColorAttachment(mainTarget.getColorTextureView()).build();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(descriptor);){
            RenderSystem.bindDefaultUniforms(renderPass);
            featureFrame.executeSeeThrough(renderPass);
        }
    }

    private void executeAlwaysOnTop(FeatureRenderDispatcher.PreparedFrame featureFrame, RenderTarget mainTarget, boolean consistentDepthRequired) {
        GpuTextureView depthTextureView = consistentDepthRequired ? this.targets.alwaysOnTopDepth.get().getDepthTextureView() : mainTarget.getDepthTextureView();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Always on top features", mainTarget.getColorTextureView(), Optional.empty(), depthTextureView, OptionalDouble.of(0.0));){
            RenderSystem.bindDefaultUniforms(renderPass);
            featureFrame.executeAlwaysOnTop(renderPass);
        }
        if (consistentDepthRequired) {
            RenderPassDescriptor integrateDepthDescriptor = RenderPassDescriptor.builder(() -> "Integrate always on top depth").withDepthAttachment(mainTarget.getDepthTextureView()).build();
            GpuSampler nearestSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(integrateDepthDescriptor);){
                renderPass.setUniform("InSampler", depthTextureView, nearestSampler);
                renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.INTEGRATE_DEPTH));
                renderPass.draw(3, 1, 0, 0);
            }
        }
    }

    private boolean frameHasAlwaysOnTopGizmos() {
        return !this.finalizedGizmos.alwaysOnTopPrimitives().isEmpty();
    }

    private void executeSolid(ChunkSectionsToRender chunkSectionsToRender, FeatureRenderDispatcher.PreparedFrame featureFrame, RenderPass renderPass) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("solidTerrain");
        GpuTextureView blockAtlas = this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        chunkSectionsToRender.renderGroup(ChunkSectionLayerGroup.OPAQUE, renderPass, this.chunkLayerSampler, blockAtlas, this.levelRenderState.renderWireframeTerrain);
        profiler.popPush("renderSolidFeatures");
        featureFrame.executeSolid(renderPass);
        profiler.pop();
    }

    private void prepareTranslucents() {
        boolean shouldRenderClouds;
        CloudStatus cloudStatus = this.optionsRenderState.cloudStatus;
        boolean bl = shouldRenderClouds = cloudStatus != CloudStatus.OFF && ARGB.alpha(this.levelRenderState.cloudColor) > 0;
        if (shouldRenderClouds) {
            this.cloudRenderer.prepare(this.levelRenderState.cloudColor, cloudStatus, this.levelRenderState.cloudHeight, this.optionsRenderState.cloudRange, this.levelRenderState.cameraRenderState.pos, this.levelRenderState.gameTime, this.levelRenderState.worldPartialTicks);
        }
        int renderDistance = this.optionsRenderState.renderDistance * 16;
        CameraRenderState cameraState = this.levelRenderState.cameraRenderState;
        this.worldBorderRenderer.prepare(this.levelRenderState.worldBorderRenderState, cameraState.pos, renderDistance, this.levelRenderState.cameraRenderState.depthFar);
        this.weatherEffectRenderer.prepare(cameraState.pos, this.levelRenderState.weatherRenderState);
        RenderSystem.resizeAllAutoStorageIndexBuffers();
    }

    private void executeOit(ChunkSectionsToRender chunkSectionsToRender, FeatureRenderDispatcher.PreparedFrame featureFrame) {
        OitRenderPassProvider.Parameters terrainParams;
        OitRenderPassProvider.Parameters cloudParams;
        boolean frameHasWaterMask = featureFrame.hasAnyWaterMask();
        CloudStatus cloudStatus = this.optionsRenderState.cloudStatus;
        boolean shouldRenderClouds = cloudStatus != CloudStatus.OFF && ARGB.alpha(this.levelRenderState.cloudColor) > 0;
        int renderDistance = this.optionsRenderState.renderDistance * 16;
        CameraRenderState cameraState = this.levelRenderState.cameraRenderState;
        GpuTextureView mainDepthTextureView = this.gameRenderer.mainRenderTarget().getDepthTextureView();
        RenderTarget mainTarget = this.targets.main.get();
        if (frameHasWaterMask) {
            this.executeOitWaterMask(featureFrame, mainTarget);
        }
        GpuTextureView depthBoundsTargetView = this.targets.depthBounds.get().getColorTextureView();
        GpuTextureView depthBoundsCulledTargetView = this.targets.depthBoundsCulled.get().getColorTextureView();
        GpuTextureView accumulateTargetView = this.targets.accumulate.get().getColorTextureView();
        GpuTextureView[] transmittanceTargetViews = new GpuTextureView[OIT_TRANSMITTANCE_TARGET_COUNT];
        for (int i = 0; i < OIT_TRANSMITTANCE_TARGET_COUNT; ++i) {
            transmittanceTargetViews[i] = this.targets.transmittance.get(i).get().getColorTextureView();
        }
        OitRenderPassProvider.Parameters params = new OitRenderPassProvider.Parameters(depthBoundsTargetView, transmittanceTargetViews, accumulateTargetView, mainDepthTextureView);
        if (shouldRenderClouds) {
            GpuTextureView cloudDepthTextureView = this.targets.oitCloudDepth.get().getDepthTextureView();
            cloudParams = new OitRenderPassProvider.Parameters(depthBoundsCulledTargetView, transmittanceTargetViews, accumulateTargetView, cloudDepthTextureView);
        } else {
            cloudParams = null;
        }
        if (frameHasWaterMask) {
            GpuTextureView terrainDepthTextureView = this.targets.oitTerrainWithWaterPatchDepth.get().getDepthTextureView();
            terrainParams = new OitRenderPassProvider.Parameters(depthBoundsTargetView, transmittanceTargetViews, accumulateTargetView, terrainDepthTextureView);
        } else {
            terrainParams = params;
        }
        GpuTextureView blockAtlas = this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        for (OitStage stage : OitStage.values()) {
            chunkSectionsToRender.renderOit(this.chunkLayerSampler, stage, terrainParams, blockAtlas, this.gameRenderer.lightmap());
            try (RenderPass renderPass = OitRenderPassProvider.createRenderPass(stage, () -> "Features, World Border, Weather", params);){
                featureFrame.executeOit(stage, renderPass);
                this.worldBorderRenderer.renderOit(this.levelRenderState.worldBorderRenderState, cameraState.pos, renderDistance, stage, renderPass);
                this.weatherEffectRenderer.renderOit(stage, this.levelRenderState.weatherRenderState, renderPass);
            }
            if (stage == OitStage.DEPTH_BOUNDS) {
                this.executeDepthBoundsCull();
                params.setDepthBoundsTargetView(depthBoundsCulledTargetView);
                terrainParams.setDepthBoundsTargetView(depthBoundsCulledTargetView);
            }
            if (shouldRenderClouds) {
                this.cloudRenderer.renderOit(cloudStatus, stage, mainDepthTextureView, cloudParams);
            }
            if (stage != OitStage.DEPTH_BOUNDS || !frameHasWaterMask) continue;
            this.executeOitWaterMask(featureFrame, mainTarget);
        }
        GpuSampler nearestSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        RenderTarget renderTarget = this.gameRenderer.mainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "OIT Composite", renderTarget.getColorTextureView(), Optional.empty(), renderTarget.getDepthTextureView(), OptionalDouble.empty());){
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("Sampler0", accumulateTargetView, nearestSampler);
            for (int i = 0; i < OIT_TRANSMITTANCE_TARGET_COUNT; ++i) {
                renderPass.setUniform("Coeff" + i, this.targets.transmittance.get(i).get().getColorTextureView(), nearestSampler);
            }
            renderPass.setUniform("DepthBoundsSampler", depthBoundsTargetView, nearestSampler);
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.OIT_COMPOSITE));
            renderPass.draw(3, 1, 0, 0);
        }
    }

    private void executeDepthBoundsCull() {
        GpuSampler nearestSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        RenderTarget depthBoundsTarget = this.targets.depthBounds.get();
        RenderTarget depthBoundsCulledTarget = this.targets.depthBoundsCulled.get();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "OIT cull Depth Bounds", depthBoundsCulledTarget.getColorTextureView(), Optional.empty(), this.gameRenderer.mainRenderTarget().getDepthTextureView(), OptionalDouble.empty());){
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.BLIT_DEPTH_BOUNDS));
            renderPass.setUniform("InSampler", depthBoundsTarget.getColorTextureView(), nearestSampler);
            renderPass.draw(3, 1, 0, 0);
            renderPass.setUniform("DepthBoundsSampler", depthBoundsTarget.getColorTextureView(), nearestSampler);
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.OIT_DEPTH_BOUNDS_CULL));
            renderPass.draw(3, 1, 0, 0);
        }
    }

    private void executeOitWaterMask(FeatureRenderDispatcher.PreparedFrame featureFrame, RenderTarget mainTarget) {
        GpuSampler nearestSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        RenderTarget terrainTarget = this.targets.oitTerrainWithWaterPatchDepth.get();
        RenderPassDescriptor descriptor = RenderPassDescriptor.builder(() -> "Water mask").withDepthAttachment(terrainTarget.getDepthTextureView()).build();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(descriptor);){
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.BLIT_DEPTH));
            renderPass.setUniform("InSampler", mainTarget.getDepthTextureView(), nearestSampler);
            renderPass.draw(3, 1, 0, 0);
            featureFrame.executeWaterMask(renderPass);
        }
    }

    private void executeClassicTransparency(ChunkSectionsToRender chunkSectionsToRender, FeatureRenderDispatcher.PreparedFrame featureFrame, RenderPass renderPass) {
        ProfilerFiller profiler = Profiler.get();
        CloudStatus cloudStatus = this.optionsRenderState.cloudStatus;
        boolean shouldRenderClouds = cloudStatus != CloudStatus.OFF && ARGB.alpha(this.levelRenderState.cloudColor) > 0;
        int renderDistance = this.optionsRenderState.renderDistance * 16;
        profiler.push("renderTranslucentFeatures");
        featureFrame.executeTranslucent(renderPass);
        profiler.pop();
        GpuTextureView blockAtlas = this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        profiler.push("translucentTerrain");
        chunkSectionsToRender.renderGroup(ChunkSectionLayerGroup.TRANSLUCENT, renderPass, this.chunkLayerSampler, blockAtlas, this.levelRenderState.renderWireframeTerrain);
        profiler.pop();
        featureFrame.executeTranslucentAfterTerrain(renderPass);
        if (shouldRenderClouds) {
            this.cloudRenderer.render(cloudStatus, renderPass);
        }
        Vec3 cameraPos = this.levelRenderState.cameraRenderState.pos;
        this.weatherEffectRenderer.render(this.levelRenderState.weatherRenderState, renderPass);
        this.worldBorderRenderer.render(this.levelRenderState.worldBorderRenderState, renderPass, cameraPos, renderDistance);
    }

    private void executeOutline(FeatureRenderDispatcher.PreparedFrame featureFrame) {
        if (this.currentFrameRendersEntityOutline) {
            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Outline", this.entityOutlineTarget.getColorTextureView(), Optional.of(ZERO_CLEAR_COLOR), null, OptionalDouble.empty());){
                RenderSystem.bindDefaultUniforms(renderPass);
                featureFrame.executeOutline(renderPass);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int extractSectionDrawGroups(boolean respectTranslucentOrder, List<DynamicGpuData.ChunkSectionInfo> sectionInfos, Map<ChunkSectionLayer, List<ChunkDrawGroup>> drawGroups) {
        int largestIndexCount = 0;
        Int2ObjectOpenHashMap drawGroupCache = new Int2ObjectOpenHashMap();
        if (this.sectionRenderDispatcher != null) {
            this.sectionRenderDispatcher.lock();
            long fadeDuration = Util.toMillis(this.optionsRenderState.chunkSectionFadeInTime);
            long now = Util.getMillis();
            int lastTransparentGroupHash = 0;
            try {
                for (SectionRenderDispatcher.RenderSection section : this.visibleSections) {
                    SectionMesh sectionMesh = section.getSectionMesh();
                    BlockPos renderOffset = section.getRenderOrigin();
                    int sectionInfoDataIndex = -1;
                    for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
                        IndexType indexType;
                        GpuBuffer indexBuffer;
                        SectionMesh.SectionDraw draw = sectionMesh.getSectionDraw(layer);
                        SectionRenderDispatcher.RenderSectionBufferSlice slice = this.sectionRenderDispatcher.getRenderSectionSlice(sectionMesh, layer);
                        if (slice == null || draw == null || draw.hasCustomIndexBuffer() && slice.indexBuffer() == null) continue;
                        if (sectionInfoDataIndex == -1) {
                            sectionInfoDataIndex = sectionInfos.size();
                            sectionInfos.add(new DynamicGpuData.ChunkSectionInfo(renderOffset.getX(), renderOffset.getY(), renderOffset.getZ(), section.getVisibility(now, fadeDuration)));
                        }
                        int combinedHash = 173;
                        VertexFormat vertexFormat = layer.pipeline(false).getVertexFormatBinding(0);
                        GpuBuffer vertexBuffer = slice.vertexBuffer();
                        combinedHash = 31 * combinedHash + vertexBuffer.hashCode();
                        int firstIndex = 0;
                        if (!draw.hasCustomIndexBuffer()) {
                            if (draw.indexCount() > largestIndexCount) {
                                largestIndexCount = draw.indexCount();
                            }
                            indexBuffer = null;
                            indexType = null;
                        } else {
                            indexBuffer = slice.indexBuffer();
                            indexType = draw.indexType();
                            combinedHash = 31 * combinedHash + indexBuffer.hashCode();
                            combinedHash = 31 * combinedHash + indexType.hashCode();
                            firstIndex = (int)(slice.indexBufferOffset() / (long)indexType.bytes);
                        }
                        int baseVertex = (int)(slice.vertexBufferOffset() / (long)vertexFormat.getVertexSize());
                        ChunkDrawGroup drawGroup = null;
                        if (layer.translucent() && respectTranslucentOrder) {
                            if (combinedHash == lastTransparentGroupHash) {
                                drawGroup = drawGroups.get((Object)layer).getLast();
                            }
                            lastTransparentGroupHash = combinedHash;
                        } else {
                            drawGroup = (ChunkDrawGroup)drawGroupCache.getOrDefault(combinedHash, null);
                        }
                        if (drawGroup == null) {
                            drawGroup = new ChunkDrawGroup(vertexBuffer.slice(), indexBuffer != null ? indexBuffer.slice() : null, indexType, new ArrayList<DynamicGpuData.IndexedDraw>());
                            drawGroupCache.put(combinedHash, (Object)drawGroup);
                            drawGroups.get((Object)layer).add(drawGroup);
                        }
                        drawGroup.draws.add(new DynamicGpuData.IndexedDraw(draw.indexCount(), 1, firstIndex, baseVertex, sectionInfoDataIndex));
                    }
                }
            }
            finally {
                this.sectionRenderDispatcher.unlock();
            }
        }
        return largestIndexCount;
    }

    public ChunkSectionsToRender prepareChunkRenders(Matrix4fc modelViewMatrix, boolean respectTranslucentOrder) {
        Map<ChunkSectionLayer, List<ChunkDrawGroup>> drawGroups = Util.makeEnumMap(ChunkSectionLayer.class, chunkSectionLayer -> new ReferenceArrayList());
        ArrayList<DynamicGpuData.ChunkSectionInfo> sectionInfos = new ArrayList<DynamicGpuData.ChunkSectionInfo>();
        GpuTextureView blockAtlas = this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        int textureAtlasWidth = blockAtlas.getWidth(0);
        int textureAtlasHeight = blockAtlas.getHeight(0);
        int largestIndexCount = this.extractSectionDrawGroups(respectTranslucentOrder, sectionInfos, drawGroups);
        Map<ChunkSectionLayer, List<RenderPass.Draw<GpuBufferSlice[]>>> flattenDraws = Util.makeEnumMap(ChunkSectionLayer.class, layer -> new ReferenceArrayList());
        for (ChunkSectionLayer layer2 : ChunkSectionLayer.values()) {
            SequencedCollection<ChunkDrawGroup> sortedDrawGroups = drawGroups.get((Object)layer2);
            if (layer2.translucent() && respectTranslucentOrder) {
                sortedDrawGroups = sortedDrawGroups.reversed();
            }
            for (ChunkDrawGroup drawGroup : sortedDrawGroups) {
                List dest = flattenDraws.get((Object)layer2);
                SequencedCollection<DynamicGpuData.IndexedDraw> sortedDraws = drawGroup.draws;
                if (layer2.translucent() && respectTranslucentOrder) {
                    sortedDraws = sortedDraws.reversed();
                }
                for (DynamicGpuData.IndexedDraw draw : sortedDraws) {
                    int sectionInfoDataIndex = draw.baseInstance();
                    GpuBuffer indexBuffer = drawGroup.indexBuffer == null ? null : drawGroup.indexBuffer().buffer();
                    dest.add(new RenderPass.Draw<GpuBufferSlice[]>(0, drawGroup.vertexBuffer.buffer(), indexBuffer, drawGroup.indexType, draw.firstIndex(), draw.indexCount(), draw.baseVertex(), (sectionUbos, uploader) -> uploader.setUniform("ChunkSection", sectionUbos[sectionInfoDataIndex])));
                }
            }
        }
        GpuBufferSlice terrainTransformUbo = RenderSystem.getDynamicUniforms().writeTerrainTransform(modelViewMatrix, textureAtlasWidth, textureAtlasHeight);
        GpuBufferSlice[] chunkSectionInfos = RenderSystem.getDynamicUniforms().writeChunkSections(sectionInfos.toArray(new DynamicGpuData.ChunkSectionInfo[0]));
        RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        if (largestIndexCount != 0) {
            autoIndices.requestIndexCount(largestIndexCount);
        }
        return new ChunkSectionsToRender.DrawSeparate(terrainTransformUbo, flattenDraws, largestIndexCount, chunkSectionInfos);
    }

    public void addTransientBlock(long blockNode, TransientBlockRenderState state) {
        this.transientBlocks.put(blockNode, (Object)state);
    }

    public void removeTransientBlocksInSection(long sectionNode, long compileTaskStartTimeNs) {
        this.transientBlockRemovalQueue.add(new TransientBlockRenderState.Removal(sectionNode, compileTaskStartTimeNs));
    }

    private void performTransientBlockRemovals() {
        TransientBlockRenderState.Removal removal;
        block0: while ((removal = (TransientBlockRenderState.Removal)this.transientBlockRemovalQueue.poll()) != null) {
            ObjectBidirectionalIterator iterator = Minecraft.getInstance().levelRenderer.transientBlocks.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)iterator.next();
                TransientBlockRenderState state = (TransientBlockRenderState)entry.getValue();
                if (state.createTimeNs > removal.compileTaskStartTimeNs()) continue block0;
                if (SectionPos.blockToSection(entry.getLongKey()) != removal.sectionNode()) continue;
                iterator.remove();
            }
        }
    }

    public ChunkSectionsToRender prepareChunkRendersIndirect(Matrix4fc modelViewMatrix, boolean respectTranslucentOrder) {
        EnumMap<ChunkSectionLayer, List<ChunkDrawGroup>> drawGroups = new EnumMap<ChunkSectionLayer, List<ChunkDrawGroup>>(ChunkSectionLayer.class);
        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            drawGroups.put(layer, (List<ChunkDrawGroup>)new ReferenceArrayList());
        }
        ArrayList<DynamicGpuData.ChunkSectionInfo> sectionInfos = new ArrayList<DynamicGpuData.ChunkSectionInfo>();
        GpuTextureView blockAtlas = this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        int textureAtlasWidth = blockAtlas.getWidth(0);
        int textureAtlasHeight = blockAtlas.getHeight(0);
        int largestIndexCount = this.extractSectionDrawGroups(respectTranslucentOrder, sectionInfos, drawGroups);
        EnumMap<ChunkSectionLayer, List<ChunkSectionsToRender.GpuMultiDrawIndexedIndirect>> indirectDraws = new EnumMap<ChunkSectionLayer, List<ChunkSectionsToRender.GpuMultiDrawIndexedIndirect>>(ChunkSectionLayer.class);
        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            indirectDraws.put(layer, new ArrayList());
        }
        ArrayList<List<DynamicGpuData.IndexedDraw>> batchedDraws = new ArrayList<List<DynamicGpuData.IndexedDraw>>();
        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            SequencedCollection<ChunkDrawGroup> sortedDrawGroups = drawGroups.get((Object)layer);
            if (layer.translucent() && respectTranslucentOrder) {
                sortedDrawGroups = sortedDrawGroups.reversed();
            }
            for (ChunkDrawGroup chunkDrawGroup : sortedDrawGroups) {
                SequencedCollection<DynamicGpuData.IndexedDraw> sortedDraws = chunkDrawGroup.draws;
                if (layer.translucent() && respectTranslucentOrder) {
                    sortedDraws = sortedDraws.reversed();
                }
                batchedDraws.add((List<DynamicGpuData.IndexedDraw>)sortedDraws);
            }
        }
        GpuBufferSlice[] indirectBufferSlices = RenderSystem.getDynamicUniforms().writeChunkSectionCommands(batchedDraws);
        int index = 0;
        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            SequencedCollection<ChunkDrawGroup> sortedDrawGroups = drawGroups.get((Object)layer);
            if (layer.translucent() && respectTranslucentOrder) {
                sortedDrawGroups = sortedDrawGroups.reversed();
            }
            for (ChunkDrawGroup chunkDrawGroup : sortedDrawGroups) {
                List<DynamicGpuData.IndexedDraw> draws = chunkDrawGroup.draws;
                GpuBufferSlice indirectBuffer = indirectBufferSlices[index++];
                indirectDraws.get((Object)layer).add(new ChunkSectionsToRender.GpuMultiDrawIndexedIndirect(chunkDrawGroup.vertexBuffer, chunkDrawGroup.indexBuffer, chunkDrawGroup.indexType, indirectBuffer, draws.size()));
            }
        }
        GpuBufferSlice terrainTransformUbo = RenderSystem.getDynamicUniforms().writeTerrainTransform(modelViewMatrix, textureAtlasWidth, textureAtlasHeight);
        GpuBufferSlice chunkSectionInfos = RenderSystem.getDynamicUniforms().writeChunkSectionsInstanced(sectionInfos);
        RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        if (largestIndexCount != 0) {
            autoIndices.requestIndexCount(largestIndexCount);
        }
        return new ChunkSectionsToRender.DrawIndirect(terrainTransformUbo, indirectDraws, largestIndexCount, chunkSectionInfos);
    }

    private void compileSections(CameraRenderState camera) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("populateSectionsToCompile");
        BlockPos cameraPosition = camera.blockPos;
        for (SectionUpdateRenderState state : this.levelRenderState.sectionUpdateRenderStates) {
            BlockPos center = SectionPos.of(state.sectionNode()).center();
            double distSqr = center.distSqr(cameraPosition);
            boolean isNearby = distSqr < 768.0;
            boolean rebuildSync = false;
            if (this.optionsRenderState.prioritizeChunkUpdates == PrioritizeChunkUpdates.NEARBY) {
                rebuildSync = isNearby || state.playerChanged();
            } else if (this.optionsRenderState.prioritizeChunkUpdates == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                rebuildSync = state.playerChanged();
            }
            SectionRenderDispatcher.RenderSection section = this.viewArea.getRenderSection(state.sectionNode());
            if (rebuildSync) {
                profiler.push("compileSectionSynchronously");
                section.compileSync(state.region());
                profiler.pop();
                continue;
            }
            section.compileAsync(state.region());
        }
        profiler.popPush("scheduleTranslucentResort");
        this.scheduleTranslucentSectionResort(camera.pos);
        profiler.pop();
    }

    private void checkPoseStack(PoseStack poseStack) {
        if (!poseStack.isEmpty()) {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    private void submitEntities(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector output) {
        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();
        for (EntityRenderState state : levelRenderState.entityRenderStates) {
            this.entityRenderDispatcher.submit(state, levelRenderState.cameraRenderState, state.x - camX, state.y - camY, state.z - camZ, poseStack, output);
        }
    }

    private void submitBlockEntities(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector) {
        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();
        for (BlockEntityRenderState renderState : levelRenderState.blockEntityRenderStates) {
            BlockPos blockPos = renderState.blockPos;
            poseStack.pushPose();
            poseStack.translate((double)blockPos.getX() - camX, (double)blockPos.getY() - camY, (double)blockPos.getZ() - camZ);
            this.blockEntityRenderDispatcher.submit(renderState, poseStack, submitNodeCollector, levelRenderState.cameraRenderState);
            poseStack.popPose();
        }
    }

    private void submitBlockDestroyAnimation(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState) {
        if (levelRenderState.blockBreakingRenderStates.isEmpty()) {
            return;
        }
        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();
        ArrayList<BlockStateModelPart> parts = new ArrayList<BlockStateModelPart>();
        RandomSource random = RandomSource.createThreadLocalInstance();
        for (BlockBreakingRenderState state : levelRenderState.blockBreakingRenderStates) {
            if (state.blockState().getRenderShape() != RenderShape.MODEL) continue;
            BlockPos pos = state.blockPos();
            poseStack.pushPose();
            poseStack.translate((double)pos.getX() - camX, (double)pos.getY() - camY, (double)pos.getZ() - camZ);
            poseStack.translate(state.blockState().getOffset(pos));
            BlockStateModel model = this.modelManager.getBlockStateModelSet().get(state.blockState());
            random.setSeed(state.blockState().getSeed(pos));
            model.collectParts(random, parts);
            submitNodeCollector.submitBreakingBlockModel(poseStack, List.copyOf(parts), state.progress(), model.hasMaterialFlag(1));
            parts.clear();
            poseStack.popPose();
        }
    }

    private void submitTransientBlocks(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState) {
        this.performTransientBlockRemovals();
        ObjectBidirectionalIterator iterator = this.transientBlocks.long2ObjectEntrySet().fastIterator();
        long currentTimeMs = Util.getMillis();
        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)iterator.next();
            TransientBlockRenderState state = (TransientBlockRenderState)entry.getValue();
            if (currentTimeMs >= state.liveUntilMs) {
                iterator.remove();
                continue;
            }
            poseStack.pushPose();
            BlockPos blockPos = state.movingBlockRenderState.blockPos;
            poseStack.translate((double)blockPos.getX() - camX, (double)blockPos.getY() - camY, (double)blockPos.getZ() - camZ);
            submitNodeCollector.submitMovingBlock(poseStack, state.movingBlockRenderState, 0);
            poseStack.popPose();
        }
    }

    private void submitBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState) {
        int outlineColor;
        BlockOutlineRenderState state = levelRenderState.blockOutlineRenderState;
        if (state == null) {
            return;
        }
        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        BlockPos pos = state.pos();
        poseStack.pushPose();
        poseStack.translate((double)pos.getX() - cameraPos.x, (double)pos.getY() - cameraPos.y, (double)pos.getZ() - cameraPos.z);
        if (state.highContrast()) {
            this.submitHitOutline(poseStack, submitNodeCollector, RenderTypes.secondaryBlockOutline(), state, -16777216, 7.0f, state.isTranslucent());
        }
        int n = outlineColor = state.highContrast() ? -11010079 : ARGB.black(102);
        RenderType blockOutlineRenderType = state.highContrast() ? RenderTypes.linesDepthBias() : (this.gameRenderer.useImprovedTransparency() ? RenderTypes.linesTranslucentNoDepthWrite() : RenderTypes.linesTranslucent());
        this.submitHitOutline(poseStack, submitNodeCollector, blockOutlineRenderType, state, outlineColor, this.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth, state.isTranslucent());
        poseStack.popPose();
    }

    private void submitHitOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, BlockOutlineRenderState state, int color, float width, boolean afterTerrain) {
        if (SharedConstants.DEBUG_SHAPES) {
            submitNodeCollector.submitShapeOutline(poseStack, state.shape(), renderType, -1, width, afterTerrain);
            if (state.collisionShape() != null) {
                submitNodeCollector.submitShapeOutline(poseStack, state.collisionShape(), renderType, ARGB.colorFromFloat(0.4f, 0.0f, 0.0f, 0.0f), width, afterTerrain);
            }
            if (state.occlusionShape() != null) {
                submitNodeCollector.submitShapeOutline(poseStack, state.occlusionShape(), renderType, ARGB.colorFromFloat(0.4f, 0.0f, 1.0f, 0.0f), width, afterTerrain);
            }
            if (state.interactionShape() != null) {
                submitNodeCollector.submitShapeOutline(poseStack, state.interactionShape(), renderType, ARGB.colorFromFloat(0.4f, 0.0f, 0.0f, 1.0f), width, afterTerrain);
            }
        } else {
            submitNodeCollector.submitShapeOutline(poseStack, state.shape(), renderType, color, width, afterTerrain);
        }
    }

    public void resize(int width, int height) {
        this.sectionOcclusionGraph.invalidate();
        this.entityOutlineTarget.resize(width, height);
    }

    public void endFrame() {
        this.cloudRenderer.endFrame();
    }

    @Override
    public void close() {
        this.resetLevelRenderData();
        this.entityOutlineTarget.destroyBuffers();
        if (this.skyRenderer != null) {
            this.skyRenderer.close();
        }
        if (this.chunkLayerSampler != null) {
            this.chunkLayerSampler.close();
        }
        this.worldBorderRenderer.close();
        this.cloudRenderer.close();
        this.weatherEffectRenderer.close();
    }

    public void blitEntityOutline() {
        if (this.currentFrameRendersEntityOutline) {
            this.entityOutlineTarget.blitAndBlendToTexture(this.gameRenderer.mainRenderTarget().getColorTextureView(), this.gameRenderer.mainRenderTarget().getDepthTextureView());
        }
    }

    public void invalidateCompiledGeometry(ClientLevel level, Options options, Camera camera, BlockColors blockColors) {
        SectionCompiler sectionCompiler = new SectionCompiler(options.ambientOcclusion().get(), options.cutoutLeaves().get(), this.modelManager.getBlockStateModelSet(), this.modelManager.getFluidStateModelSet(), blockColors);
        if (this.sectionRenderDispatcher == null) {
            this.sectionRenderDispatcher = new SectionRenderDispatcher(Util.backgroundExecutor(), this.renderBuffers, sectionCompiler, this.sectionOcclusionGraph::schedulePropagationFrom);
        } else {
            this.sectionRenderDispatcher.setCompiler(sectionCompiler);
        }
        this.cloudRenderer().markForRebuild();
        LeavesBlock.setCutoutLeaves(options.cutoutLeaves().get());
        if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
        }
        this.sectionRenderDispatcher.clearCompileQueue();
        this.viewArea = new ViewArea(this.sectionRenderDispatcher, level.getMinY(), level.getMaxY(), level.getMinSectionY(), level.getMaxSectionY(), options.getEffectiveRenderDistance(), this.sectionOcclusionGraph);
        this.sectionOcclusionGraph().waitAndReset(this.viewArea);
        this.clearVisibleSections();
        SectionPos cameraSectionPos = SectionPos.of(camera.position());
        this.viewArea.repositionCamera(cameraSectionPos);
    }

    private void scheduleTranslucentSectionResort(Vec3 cameraPos) {
        if (this.visibleSections.isEmpty()) {
            return;
        }
        BlockPos cameraBlockPos = BlockPos.containing(cameraPos);
        boolean blockPosChanged = !cameraBlockPos.equals(this.lastTranslucentSortBlockPos);
        TranslucencyPointOfView pointOfView = new TranslucencyPointOfView();
        for (SectionRenderDispatcher.RenderSection section : this.nearbyVisibleSections) {
            this.scheduleResort(section, pointOfView, cameraPos, blockPosChanged, true);
        }
        this.translucencyResortIterationIndex %= this.visibleSections.size();
        int resortsLeft = Math.max(this.visibleSections.size() / 8, 15);
        while (resortsLeft-- > 0) {
            int index = this.translucencyResortIterationIndex++ % this.visibleSections.size();
            this.scheduleResort((SectionRenderDispatcher.RenderSection)this.visibleSections.get(index), pointOfView, cameraPos, blockPosChanged, false);
        }
        this.lastTranslucentSortBlockPos = cameraBlockPos;
    }

    private void scheduleResort(SectionRenderDispatcher.RenderSection section, TranslucencyPointOfView pointOfView, Vec3 cameraPos, boolean blockPosChanged, boolean isNearby) {
        boolean resortBecauseBlockPosChanged;
        pointOfView.set(cameraPos, section.getSectionNode());
        boolean pointOfViewChanged = section.getSectionMesh().isDifferentPointOfView(pointOfView);
        boolean bl = resortBecauseBlockPosChanged = blockPosChanged && (pointOfView.isAxisAligned() || isNearby);
        if ((resortBecauseBlockPosChanged || pointOfViewChanged) && !section.transparencyResortingScheduled() && section.hasTranslucentGeometry() && !this.gameRenderer.useImprovedTransparency()) {
            section.resortTransparency();
        }
    }

    public void clearVisibleSections() {
        this.visibleSections.clear();
        this.nearbyVisibleSections.clear();
    }

    public void resetLevelRenderData() {
        if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
            this.viewArea = null;
        }
        if (this.sectionRenderDispatcher != null) {
            this.sectionRenderDispatcher.dispose();
        }
        this.sectionRenderDispatcher = null;
        this.sectionOcclusionGraph.waitAndReset(null);
        this.clearVisibleSections();
    }

    public boolean hasRenderedAllSections() {
        return this.sectionRenderDispatcher == null || this.sectionRenderDispatcher.isQueueEmpty();
    }

    public boolean isSectionCompiledAndVisible(BlockPos blockPos, long chunkFadeDuration) {
        if (this.viewArea == null) {
            return false;
        }
        SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSectionAt(blockPos);
        if (renderSection == null || renderSection.sectionMesh.get() == CompiledSectionMesh.UNCOMPILED) {
            return false;
        }
        return renderSection.getVisibility(Util.getMillis(), chunkFadeDuration) >= 0.3f;
    }

    public @Nullable SectionRenderDispatcher sectionRenderDispatcher() {
        return this.sectionRenderDispatcher;
    }

    public EntityRenderDispatcher entityRenderDispatcher() {
        return this.entityRenderDispatcher;
    }

    public BlockEntityRenderDispatcher blockEntityRenderDispatcher() {
        return this.blockEntityRenderDispatcher;
    }

    public CloudRenderer cloudRenderer() {
        return this.cloudRenderer;
    }

    public @Nullable SkyRenderer skyRenderer() {
        return this.skyRenderer;
    }

    public WeatherEffectRenderer weatherEffectRenderer() {
        return this.weatherEffectRenderer;
    }

    public WorldBorderRenderer worldBorderRenderer() {
        return this.worldBorderRenderer;
    }

    public @Nullable ViewArea viewArea() {
        return this.viewArea;
    }

    public ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections() {
        return this.visibleSections;
    }

    public ObjectArrayList<SectionRenderDispatcher.RenderSection> nearbyVisibleSections() {
        return this.nearbyVisibleSections;
    }

    public LongCollection expectedChunks() {
        return this.sectionOcclusionGraph.expectedChunks();
    }

    public SectionOcclusionGraph sectionOcclusionGraph() {
        return this.sectionOcclusionGraph;
    }

    public Gizmos.TemporaryCollection collectPerFrameRenderThreadGizmos() {
        return Gizmos.withCollector(this.renderThreadGizmos);
    }

    public boolean isChunkRenderingUsingMultiDrawIndirect() {
        return this.usingMultiDrawIndirectForTerrain;
    }

    private void finalizeGizmoCollection() {
        DrawableGizmoPrimitives standardPrimitives = new DrawableGizmoPrimitives();
        DrawableGizmoPrimitives alwaysOnTopPrimitives = new DrawableGizmoPrimitives();
        long currentMillis = Util.getMillis();
        for (SimpleGizmoCollector.GizmoInstance instance : this.renderThreadGizmos.drainGizmos()) {
            instance.gizmo().emit(instance.isAlwaysOnTop() ? alwaysOnTopPrimitives : standardPrimitives, instance.getAlphaMultiplier(currentMillis));
        }
        this.finalizedGizmos = new FinalizedGizmos(standardPrimitives, alwaysOnTopPrimitives);
    }

    public void addMainThreadGizmos(List<SimpleGizmoCollector.GizmoInstance> mainThreadGizmos) {
        this.renderThreadGizmos.addTemporaryGizmos(mainThreadGizmos);
    }

    private record FinalizedGizmos(DrawableGizmoPrimitives standardPrimitives, DrawableGizmoPrimitives alwaysOnTopPrimitives) {
    }

    private record ChunkDrawGroup(GpuBufferSlice vertexBuffer, @Nullable GpuBufferSlice indexBuffer, @Nullable IndexType indexType, List<DynamicGpuData.IndexedDraw> draws) {
    }
}

