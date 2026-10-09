/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.chunk;

import java.util.BitSet;
import net.minecraft.world.level.chunk.CarverOutput;

public class CarvingMask
implements CarverOutput {
    private final int minY;
    private final int maxY;
    private final int height;
    private final BitSet mask;

    public CarvingMask(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
        this.height = maxY - minY + 1;
        this.mask = new BitSet(256 * this.height);
    }

    private int getIndex(int x, int y, int z) {
        return y - this.minY + (z + (x << 4)) * this.height;
    }

    @Override
    public int minY() {
        return this.minY;
    }

    @Override
    public int maxY() {
        return this.maxY;
    }

    @Override
    public void carve(int x, int y, int z) {
        this.mask.set(this.getIndex(x, y, z));
    }

    public void visit(Visitor visitor) {
        int startIndex = this.mask.nextSetBit(0);
        while (startIndex != -1) {
            int endIndex = this.mask.nextClearBit(startIndex) - 1;
            this.visitSegment(visitor, startIndex, endIndex);
            startIndex = this.mask.nextSetBit(endIndex + 1);
        }
    }

    private void visitSegment(Visitor visitor, int startIndex, int endIndex) {
        int startColumn = startIndex / this.height;
        int endColumn = endIndex / this.height;
        for (int column = startColumn; column <= endColumn; ++column) {
            int columnX = column >> 4 & 0xF;
            int columnZ = column & 0xF;
            int columnBaseIndex = column * this.height;
            int bottomY = Math.max(startIndex - columnBaseIndex, 0) + this.minY;
            int topY = Math.min(endIndex - columnBaseIndex, this.height - 1) + this.minY;
            visitor.visitColumn(columnX, columnZ, bottomY, topY);
        }
    }

    public boolean isEmpty() {
        return this.mask.isEmpty();
    }

    @FunctionalInterface
    public static interface Visitor {
        public void visitColumn(int var1, int var2, int var3, int var4);
    }

    @FunctionalInterface
    public static interface Filter {
        public boolean test(int var1, int var2, int var3);
    }
}

