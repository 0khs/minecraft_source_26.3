/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ReferenceArrayList
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.renderpearl.backend.util;

import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

public class TransientBlockAllocator<T extends Allocator.Block>
implements UncheckedAutoCloseable {
    private final long blockSize;
    private final long maxAlignment;
    private final Allocator<T> allocator;
    private final Consumer<T> onBlockUse;
    private final ReferenceArrayList<T> specialBlocks = new ReferenceArrayList();
    private final ReferenceArrayList<T> freeBlocks = new ReferenceArrayList();
    private final ReferenceArrayList<T> usedBlocks = new ReferenceArrayList();
    private @Nullable T currentBlock;
    private long currentOffset = 0L;

    public TransientBlockAllocator(long blockSize, long maxAlignment, Allocator<T> allocator) {
        this(blockSize, maxAlignment, allocator, block -> {});
    }

    public TransientBlockAllocator(long blockSize, long maxAlignment, Allocator<T> allocator, Consumer<T> onBlockUse) {
        this.blockSize = blockSize;
        this.maxAlignment = maxAlignment;
        this.allocator = allocator;
        this.onBlockUse = onBlockUse;
    }

    @Override
    public void close() {
        this.rotate().run();
        this.rotate().run();
    }

    public long blockSize() {
        return this.blockSize;
    }

    public Runnable rotate() {
        this.currentBlock = null;
        this.currentOffset = this.blockSize;
        this.freeBlocks.forEach(this.allocator::free);
        this.freeBlocks.clear();
        if (this.usedBlocks.isEmpty() && this.specialBlocks.isEmpty()) {
            return () -> {};
        }
        ReferenceArrayList blocksUsedThisRotation = this.usedBlocks.clone();
        this.usedBlocks.clear();
        ReferenceArrayList specialBlocksUsedThisRotation = this.specialBlocks.clone();
        this.specialBlocks.clear();
        return () -> {
            this.freeBlocks.ensureCapacity(this.freeBlocks.size() + blocksUsedThisRotation.size());
            for (Allocator.Block usedBlock : blocksUsedThisRotation) {
                if (usedBlock.suboptimal()) {
                    this.allocator.free(usedBlock);
                    continue;
                }
                this.freeBlocks.add((Object)usedBlock);
            }
            specialBlocksUsedThisRotation.forEach(this.allocator::free);
        };
    }

    @Contract(pure=true)
    public boolean canAllocateInBlock(long size, long alignment) {
        return size <= this.blockSize && alignment <= this.maxAlignment;
    }

    @Contract(pure=true)
    public boolean canAllocateInCurrentBlock(long size, long alignment) {
        if (this.currentBlock == null && this.canAllocateInBlock(size, alignment)) {
            return true;
        }
        long alignedOffset = Mth.roundToward(this.currentOffset, alignment);
        return size <= this.blockSize - alignedOffset && alignment <= this.maxAlignment;
    }

    private T allocateBlock() {
        if (this.freeBlocks.isEmpty()) {
            this.freeBlocks.add(this.allocator.alloc(this.blockSize));
        }
        Allocator.Block block = (Allocator.Block)this.freeBlocks.pop();
        this.onBlockUse.accept(block);
        this.usedBlocks.add((Object)block);
        return (T)block;
    }

    public Allocation<T> allocate(long size, long alignment, long minimumAllocation, long elementSize) {
        if (alignment > this.maxAlignment) {
            throw new IllegalArgumentException("Alignment requirement over maximum supported alignment");
        }
        if (size == this.blockSize) {
            return new Allocation<T>(this.allocateBlock(), 0L, this.blockSize);
        }
        if (!this.canAllocateInBlock(size, alignment)) {
            T specialBlock = this.allocator.alloc(size);
            this.onBlockUse.accept(specialBlock);
            this.specialBlocks.add(specialBlock);
            return new Allocation<T>(specialBlock, 0L, size);
        }
        if (this.currentBlock == null) {
            this.currentBlock = this.allocateBlock();
            this.currentOffset = 0L;
        }
        if (this.canAllocateInCurrentBlock(size, alignment)) {
            assert (this.currentBlock != null);
            long alignedOffset = Mth.roundToward(this.currentOffset, alignment);
            this.currentOffset = alignedOffset + size;
            T block = this.currentBlock;
            return new Allocation<T>(block, alignedOffset, size);
        }
        if (this.canAllocateInCurrentBlock(minimumAllocation, alignment)) {
            assert (this.currentBlock != null);
            long alignedOffset = Mth.roundToward(this.currentOffset, alignment);
            long allocatedSize = (this.blockSize - alignedOffset) / elementSize * elementSize;
            this.currentOffset = alignedOffset + allocatedSize;
            T block = this.currentBlock;
            return new Allocation<T>(block, alignedOffset, allocatedSize);
        }
        T newBlock = this.allocateBlock();
        if (this.currentOffset > size) {
            this.currentBlock = newBlock;
            this.currentOffset = size;
        }
        return new Allocation<T>(newBlock, 0L, size);
    }

    public static interface Allocator<T extends Block> {
        public T alloc(long var1);

        public void free(T var1);

        public static <T extends Block> Allocator<T> create(final LongFunction<T> alloc, final Consumer<T> free) {
            return new Allocator<T>(){

                @Override
                public T alloc(long size) {
                    return (Block)alloc.apply(size);
                }

                @Override
                public void free(T t) {
                    free.accept(t);
                }
            };
        }

        public record CpuBlock(long address) implements Block
        {
            public CpuBlock {
                if (address == 0L) {
                    throw new IllegalArgumentException("Failed to allocate CPU block");
                }
            }

            @Override
            public boolean suboptimal() {
                return false;
            }

            public static Allocator<CpuBlock> memalloc() {
                return Allocator.create(size -> new CpuBlock(MemoryUtil.nmemAlloc((long)size)), block -> MemoryUtil.nmemFree((long)block.address));
            }
        }

        public static interface Block {
            public boolean suboptimal();
        }
    }

    public record Allocation<T>(T block, long offset, long size) {
    }
}

