/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.client.renderer.DynamicGpuDataStorage;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.client.renderer.DynamicGpuDataStorageNonMapped;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public class DynamicGpuData
implements AutoCloseable {
    private static final Vector4fc WHITE = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3fc NO_OFFSET = new Vector3f();
    private static final Matrix4fc IDENTITY_TEXTURE_TRANSFORM = new Matrix4f();
    public static final int TRANSFORM_UBO_SIZE = new Std140SizeCalculator().putMat4f().putMat4f().putVec4().putVec3().get();
    public static final int TERRAIN_TRANSFORM_UBO_SIZE = new Std140SizeCalculator().putMat4f().putIVec2().get();
    public static final int CHUNK_SECTION_UBO_SIZE = new Std140SizeCalculator().putIVec3().putFloat().get();
    private static final int INITIAL_CAPACITY = 2;
    private final DynamicGpuDataStorageMapped<Transform> transforms = new DynamicGpuDataStorageMapped("Dynamic Transforms UBO", TRANSFORM_UBO_SIZE, 128, 2);
    private final DynamicGpuDataStorageMapped<TerrainTransform> terrain = new DynamicGpuDataStorageMapped("Terrain UBO", TERRAIN_TRANSFORM_UBO_SIZE, 128, 1);
    private @Nullable DynamicGpuDataStorage<ChunkSectionInfo> chunkSections = null;
    private @Nullable DynamicGpuDataStorageMapped<IndexedDraw> chunkSectionsCommandBuffer = null;

    public void reset() {
        this.transforms.endFrame();
        this.terrain.endFrame();
        if (this.chunkSections != null) {
            this.chunkSections.endFrame();
        }
        if (this.chunkSectionsCommandBuffer != null) {
            this.chunkSectionsCommandBuffer.endFrame();
        }
    }

    @Override
    public void close() {
        this.transforms.close();
        this.terrain.close();
        if (this.chunkSections != null) {
            this.chunkSections.close();
        }
        if (this.chunkSectionsCommandBuffer != null) {
            this.chunkSectionsCommandBuffer.close();
        }
    }

    public GpuBufferSlice writeTransform(Matrix4f modelView) {
        return this.writeTransform(new Transform((Matrix4fc)modelView, WHITE, NO_OFFSET, IDENTITY_TEXTURE_TRANSFORM));
    }

    public GpuBufferSlice writeTransform(Matrix4f modelView, Vector4f colorModulator) {
        return this.writeTransform(new Transform((Matrix4fc)modelView, (Vector4fc)colorModulator, NO_OFFSET, IDENTITY_TEXTURE_TRANSFORM));
    }

    public GpuBufferSlice writeTransform(Matrix4f modelView, Matrix4f textureMatrix) {
        return this.writeTransform(new Transform((Matrix4fc)modelView, WHITE, NO_OFFSET, (Matrix4fc)textureMatrix));
    }

    public GpuBufferSlice writeTransform(Matrix4f modelView, Vector4f colorModulator, Vector3f modelOffset, Matrix4f textureMatrix) {
        return this.writeTransform(new Transform((Matrix4fc)modelView, (Vector4fc)colorModulator, (Vector3fc)modelOffset, (Matrix4fc)textureMatrix));
    }

    public GpuBufferSlice writeTransform(Transform transform) {
        return this.transforms.writeData(transform);
    }

    public GpuBufferSlice[] writeTransforms(Transform ... transforms) {
        return this.transforms.writeData(transforms);
    }

    public GpuBufferSlice writeTerrainTransform(Matrix4fc modelView, int textureAtlasWidth, int textureAtlasHeight) {
        return this.terrain.writeData(new TerrainTransform(modelView, textureAtlasWidth, textureAtlasHeight));
    }

    public GpuBufferSlice[] writeChunkSections(ChunkSectionInfo ... infos) {
        if (this.chunkSections != null && (this.chunkSections.usage() & 0x80) == 0) {
            this.chunkSections.close();
            this.chunkSections = null;
        }
        if (this.chunkSections == null) {
            this.chunkSections = new DynamicGpuDataStorageMapped<ChunkSectionInfo>("Chunk Sections UBO", CHUNK_SECTION_UBO_SIZE, 128, 2);
        }
        return this.chunkSections.writeData(infos);
    }

    public GpuBufferSlice writeChunkSectionsInstanced(List<ChunkSectionInfo> infos) {
        if (this.chunkSections != null && (this.chunkSections.usage() & 0x20) == 0) {
            this.chunkSections.close();
            this.chunkSections = null;
        }
        if (this.chunkSections == null) {
            this.chunkSections = new DynamicGpuDataStorageNonMapped<ChunkSectionInfo>("Chunk Sections Instanced", CHUNK_SECTION_UBO_SIZE, 32, 2);
        }
        return this.chunkSections.writeDataBatched(infos);
    }

    public GpuBufferSlice[] writeChunkSectionCommands(List<List<IndexedDraw>> draws) {
        if (this.chunkSectionsCommandBuffer == null) {
            this.chunkSectionsCommandBuffer = new DynamicGpuDataStorageMapped("Chunk Sections Command Buffer", 20, 512, 2);
        }
        return this.chunkSectionsCommandBuffer.writeDataBatchedMultiple(draws);
    }

    public record Transform(Matrix4fc modelView, Vector4fc colorModulator, Vector3fc modelOffset, Matrix4fc textureMatrix) implements DynamicGpuDataStorage.DynamicGpuData
    {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putMat4f(this.modelView).putMat4f(this.textureMatrix).putVec4(this.colorModulator).putVec3(this.modelOffset);
        }
    }

    public record TerrainTransform(Matrix4fc modelView, int textureAtlasWidth, int textureAtlasHeight) implements DynamicGpuDataStorage.DynamicGpuData
    {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putMat4f(this.modelView).putIVec2(this.textureAtlasWidth, this.textureAtlasHeight);
        }
    }

    public record IndexedDraw(int indexCount, int instanceCount, int firstIndex, int baseVertex, int baseInstance) implements DynamicGpuDataStorage.DynamicGpuData
    {
        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(this.indexCount).putInt(this.instanceCount).putInt(this.firstIndex).putInt(this.baseVertex).putInt(this.baseInstance);
        }
    }

    public record ChunkSectionInfo(int x, int y, int z, float visibility) implements DynamicGpuDataStorage.DynamicGpuData
    {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putIVec3(this.x, this.y, this.z).putFloat(this.visibility);
        }
    }
}

