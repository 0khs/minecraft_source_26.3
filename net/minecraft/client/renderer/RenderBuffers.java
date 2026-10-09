/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.SectionBufferBuilderPool;
import net.minecraft.client.renderer.StagedVertexBuffer;

public class RenderBuffers
implements AutoCloseable {
    private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
    private final SectionBufferBuilderPool sectionBufferPool;
    private final StagedVertexBuffer stagedVertexBuffer;

    public RenderBuffers(int maxSectionBuilders) {
        this.sectionBufferPool = SectionBufferBuilderPool.allocate(maxSectionBuilders);
        this.stagedVertexBuffer = new StagedVertexBuffer(() -> "Shared Buffer", 0x400000);
    }

    public SectionBufferBuilderPack fixedBufferPack() {
        return this.fixedBufferPack;
    }

    public SectionBufferBuilderPool sectionBufferPool() {
        return this.sectionBufferPool;
    }

    public StagedVertexBuffer stagedVertexBuffer() {
        return this.stagedVertexBuffer;
    }

    public void endFrame() {
        this.stagedVertexBuffer.endFrame();
    }

    @Override
    public void close() {
        this.sectionBufferPool.close();
        this.stagedVertexBuffer.close();
    }
}

