/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.PipelineCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ShaderManager
implements PreparableReloadListener,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MAX_LOG_LENGTH = 32768;
    public static final String SHADER_PATH = "shaders";
    public static final String SHADER_INCLUDE_PATH = "shaders/include/";
    public static final String SHADER_INCLUDE_EXTENSION = ".glsl";
    public static final FileToIdConverter SHADER_INCLUDE_CONVERTER = new FileToIdConverter("shaders/include", ".glsl");
    private static final FileToIdConverter POST_CHAIN_ID_CONVERTER = FileToIdConverter.json("post_effect");
    private final TextureManager textureManager;
    private final Consumer<Exception> recoveryHandler;
    private PostChainCache postChains = new PostChainCache(this, Configs.EMPTY);
    private final Projection postChainProjection = new Projection();
    private final ProjectionMatrixBuffer postChainProjectionMatrixBuffer = new ProjectionMatrixBuffer("post");

    public ShaderManager(TextureManager textureManager, Consumer<Exception> recoveryHandler) {
        this.textureManager = textureManager;
        this.recoveryHandler = recoveryHandler;
        this.postChainProjection.setupOrtho(0.1f, 1000.0f, 1.0f, 1.0f, false);
    }

    @Override
    public final CompletableFuture<Void> reload(PreparableReloadListener.SharedState currentReload, Executor taskExecutor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor reloadExecutor) {
        ResourceManager manager = currentReload.resourceManager();
        GpuDevice device = RenderSystem.getDevice();
        return ((CompletableFuture)((CompletableFuture)CompletableFuture.supplyAsync(() -> ShaderManager.loadConfigs(manager), taskExecutor).thenComposeAsync(configs -> {
            List<RenderPipeline> requiredPipelines = RenderPipelines.requiredPipelines();
            List<RenderPipeline> optionalPipelines = RenderPipelines.optionalPipelines();
            return ShaderManager.compilePipelines(device, configs, requiredPipelines, taskExecutor, reloadExecutor).thenCombine(ShaderManager.compilePipelines(device, configs, optionalPipelines, taskExecutor, reloadExecutor), (compiledRequiredPipelines, compiledOptionalPipelines) -> new PendingResults((Configs)configs, requiredPipelines, optionalPipelines, (List<CompiledRenderPipeline>)compiledRequiredPipelines, (List<CompiledRenderPipeline>)compiledOptionalPipelines));
        }, reloadExecutor)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(compilations -> this.apply(device, (PendingResults)compilations), reloadExecutor);
    }

    private static CompletableFuture<List<@Nullable CompiledRenderPipeline>> compilePipelines(GpuDevice device, ShaderSource shaderSource, List<RenderPipeline> pipelines, Executor taskExecutor, Executor reloadExecutor) {
        return Util.sequence(pipelines.stream().map(pipeline -> device.compilePipeline((RenderPipeline)pipeline, shaderSource, taskExecutor).thenApplyAsync(CompiledRenderPipeline.Pending::finishCompile, reloadExecutor)).toList());
    }

    private static Configs loadConfigs(ResourceManager manager) {
        ImmutableMap.Builder shaderSources = ImmutableMap.builder();
        ImmutableMap.Builder includeSources = ImmutableMap.builder();
        Map<Identifier, Resource> files = manager.listResources(SHADER_PATH, identifier -> true);
        for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            Identifier location = entry.getKey();
            ShaderType shaderType = ShaderType.byLocation(location);
            if (shaderType != null) {
                ShaderManager.loadShader(location, entry.getValue(), shaderType, (ImmutableMap.Builder<ShaderSourceKey, String>)shaderSources);
                continue;
            }
            if (!SHADER_INCLUDE_CONVERTER.matches(location)) continue;
            ShaderManager.loadInclude(location, entry.getValue(), (ImmutableMap.Builder<Identifier, ShaderSource.CachedIncludeSource>)includeSources);
        }
        ImmutableMap.Builder postChains = ImmutableMap.builder();
        for (Map.Entry<Identifier, Resource> entry : POST_CHAIN_ID_CONVERTER.listMatchingResources(manager).entrySet()) {
            ShaderManager.loadPostChain(entry.getKey(), entry.getValue(), (ImmutableMap.Builder<Identifier, PostChainConfig>)postChains);
        }
        return new Configs((Map<ShaderSourceKey, String>)shaderSources.build(), (Map<Identifier, ShaderSource.CachedIncludeSource>)includeSources.build(), (Map<Identifier, PostChainConfig>)postChains.build());
    }

    private static void loadShader(Identifier location, Resource resource, ShaderType type, ImmutableMap.Builder<ShaderSourceKey, String> output) {
        try {
            String contents = resource.readAllAsString();
            Identifier id = type.idConverter().fileToId(location);
            output.put((Object)new ShaderSourceKey(id, type), (Object)contents);
        }
        catch (IOException e) {
            LOGGER.error("Failed to load shader source at {}", (Object)location, (Object)e);
        }
    }

    private static void loadInclude(Identifier location, Resource resource, ImmutableMap.Builder<Identifier, ShaderSource.CachedIncludeSource> output) {
        try {
            String contents = resource.readAllAsString();
            Identifier id = ShaderManager.includeShaderLocationToId(location);
            output.put((Object)id, (Object)ShaderSource.CachedIncludeSource.create(id, contents));
        }
        catch (IOException e) {
            LOGGER.error("Failed to load shader source at {}", (Object)location, (Object)e);
        }
    }

    private static void loadPostChain(Identifier location, Resource resource, ImmutableMap.Builder<Identifier, PostChainConfig> output) {
        Identifier id = POST_CHAIN_ID_CONVERTER.fileToId(location);
        try (BufferedReader reader = resource.openAsReader();){
            JsonElement json = StrictJsonParser.parse(reader);
            output.put((Object)id, (Object)((PostChainConfig)PostChainConfig.CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)json).getOrThrow(JsonSyntaxException::new)));
        }
        catch (JsonParseException | IOException e) {
            LOGGER.error("Failed to parse post chain at {}", (Object)location, (Object)e);
        }
    }

    public static Map<Identifier, ShaderSource.CachedIncludeSource> listAllIncludes(ResourceManager resourceManager) {
        HashMap<Identifier, ShaderSource.CachedIncludeSource> includes = new HashMap<Identifier, ShaderSource.CachedIncludeSource>();
        SHADER_INCLUDE_CONVERTER.listMatchingResources(resourceManager).forEach((location, resource) -> {
            try {
                String contents = resource.readAllAsString();
                Identifier includeId = ShaderManager.includeShaderLocationToId(location);
                includes.put(includeId, ShaderSource.CachedIncludeSource.create(includeId, contents));
            }
            catch (Exception exception) {
                LOGGER.error("Couldn't read shader file {}", location, (Object)exception);
            }
        });
        return includes;
    }

    private static Identifier includeShaderLocationToId(Identifier location) {
        return SHADER_INCLUDE_CONVERTER.fileToId(location).withSuffix(SHADER_INCLUDE_EXTENSION);
    }

    private void apply(GpuDevice device, PendingResults compilations) {
        CompiledRenderPipeline compiled;
        RenderPipeline pipeline;
        int i;
        PostChainCache newPostChains = new PostChainCache(this, compilations.configs);
        ArrayList<Identifier> failedLoads = new ArrayList<Identifier>();
        PipelineCache pipelineCache = new PipelineCache(device, compilations.configs);
        pipelineCache.clear();
        for (i = 0; i < compilations.requiredPipelines.size(); ++i) {
            pipeline = compilations.requiredPipelines.get(i);
            compiled = compilations.compiledRequiredPipelines.get(i);
            if (compiled != null) {
                pipelineCache.insert(pipeline, compiled);
                continue;
            }
            failedLoads.add(pipeline.getLocation());
        }
        if (!failedLoads.isEmpty()) {
            for (CompiledRenderPipeline builtPipeline : compilations.compiledOptionalPipelines) {
                if (builtPipeline == null) continue;
                builtPipeline.close();
            }
            pipelineCache.close();
            throw new RuntimeException("Failed to load required shader programs:\n" + failedLoads.stream().map(entry -> " - " + String.valueOf(entry)).collect(Collectors.joining("\n")));
        }
        for (i = 0; i < compilations.optionalPipelines.size(); ++i) {
            pipeline = compilations.optionalPipelines.get(i);
            compiled = compilations.compiledOptionalPipelines.get(i);
            if (compiled != null) {
                pipelineCache.insert(pipeline, compiled);
                continue;
            }
            failedLoads.add(pipeline.getLocation());
        }
        if (!failedLoads.isEmpty()) {
            LOGGER.warn("Failed to load optional shader programs:\n{}", (Object)failedLoads.stream().map(entry -> " - " + String.valueOf(entry)).collect(Collectors.joining("\n")));
        }
        this.postChains.close();
        this.postChains = newPostChains;
        PipelineCache oldPipelineCache = RenderSystem.setCurrentPipelineCache(pipelineCache);
        if (oldPipelineCache != null) {
            oldPipelineCache.close();
        }
    }

    @Override
    public String getName() {
        return "Shader Loader";
    }

    private void tryTriggerRecovery(Exception exception) {
        if (this.postChains.triggeredRecovery) {
            return;
        }
        this.recoveryHandler.accept(exception);
        this.postChains.triggeredRecovery = true;
    }

    public boolean isPostEffectValid(Identifier id, Set<Identifier> allowedTargets) {
        PostChainConfig postChainConfig = this.postChains.configs.postChains.get(id);
        if (postChainConfig == null) {
            LOGGER.warn("Requested post effect does not exist: {}", (Object)id);
            return false;
        }
        Sets.SetView invalidExternalTargets = Sets.difference(PostChain.getReferencedExternalTargets(postChainConfig), allowedTargets);
        if (!invalidExternalTargets.isEmpty()) {
            LOGGER.warn("Requested post chain {} can not be used as a post effect because it uses targets inaccessible to post effects: {}", (Object)id, (Object)invalidExternalTargets);
            return false;
        }
        return true;
    }

    public @Nullable PostChain getPostChain(Identifier id, Set<Identifier> allowedTargets) {
        try {
            return this.postChains.getOrLoadPostChain(id, allowedTargets);
        }
        catch (CompilationException e) {
            LOGGER.error("Failed to load post chain: {}", (Object)id, (Object)e);
            this.postChains.postChains.put(id, Optional.empty());
            this.tryTriggerRecovery(e);
            return null;
        }
    }

    @Override
    public void close() {
        this.postChains.close();
        this.postChainProjectionMatrixBuffer.close();
    }

    public Stream<Identifier> getAvailablePostEffects() {
        return this.postChains.getKnownPostEffects();
    }

    private class PostChainCache
    implements AutoCloseable {
        private final Configs configs;
        private final Map<Identifier, Optional<PostChain>> postChains;
        private boolean triggeredRecovery;
        final /* synthetic */ ShaderManager this$0;

        private PostChainCache(ShaderManager shaderManager, Configs configs) {
            ShaderManager shaderManager2 = shaderManager;
            Objects.requireNonNull(shaderManager2);
            this.this$0 = shaderManager2;
            this.postChains = new HashMap<Identifier, Optional<PostChain>>();
            this.configs = configs;
        }

        public @Nullable PostChain getOrLoadPostChain(Identifier id, Set<Identifier> allowedTargets) throws CompilationException {
            Optional<PostChain> cached = this.postChains.get(id);
            if (cached != null) {
                return cached.orElse(null);
            }
            PostChain postChain = this.loadPostChain(id, allowedTargets);
            this.postChains.put(id, Optional.ofNullable(postChain));
            return postChain;
        }

        private @Nullable PostChain loadPostChain(Identifier id, Set<Identifier> allowedTargets) throws CompilationException {
            PostChainConfig config = this.configs.postChains.get(id);
            if (config == null) {
                if (!id.equals(GameRenderer.END_OF_FRAME_POST_EFFECT)) {
                    LOGGER.warn("Attempted to load a non-existent post effect {}", (Object)id);
                }
                return null;
            }
            return PostChain.load(config, this.this$0.textureManager, allowedTargets, id, this.this$0.postChainProjection, this.this$0.postChainProjectionMatrixBuffer);
        }

        @Override
        public void close() {
            this.postChains.values().forEach(chain -> chain.ifPresent(PostChain::close));
            this.postChains.clear();
        }

        public Stream<Identifier> getKnownPostEffects() {
            return this.configs.postChains.keySet().stream();
        }
    }

    public record Configs(Map<ShaderSourceKey, String> shaderSources, Map<Identifier, ShaderSource.CachedIncludeSource> includeSources, Map<Identifier, PostChainConfig> postChains) implements ShaderSource
    {
        public static final Configs EMPTY = new Configs(Map.of(), Map.of(), Map.of());

        @Override
        public String getShader(Identifier id, ShaderType type) {
            return this.shaderSources.get(new ShaderSourceKey(id, type));
        }

        @Override
        public ShaderSource.CachedIncludeSource getInclude(Identifier id) {
            return this.includeSources.get(id);
        }

        @Override
        public void close() {
            this.includeSources.values().forEach(ShaderSource.CachedIncludeSource::close);
        }
    }

    private record ShaderSourceKey(Identifier id, ShaderType type) {
        @Override
        public String toString() {
            return String.valueOf(this.id) + " (" + String.valueOf((Object)this.type) + ")";
        }
    }

    private record PendingResults(Configs configs, List<RenderPipeline> requiredPipelines, List<RenderPipeline> optionalPipelines, List<@Nullable CompiledRenderPipeline> compiledRequiredPipelines, List<@Nullable CompiledRenderPipeline> compiledOptionalPipelines) {
    }

    public static class CompilationException
    extends Exception {
        public CompilationException(String message) {
            super(message);
        }
    }
}

