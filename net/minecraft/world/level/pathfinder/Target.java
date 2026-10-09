/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.level.pathfinder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.pathfinder.Node;

public class Target
extends Node {
    public static final StreamCodec<ByteBuf, Target> DEBUG_STREAM_CODEC = Target.createDebugStreamCodec(Target::new);
    private float bestHeuristic = Float.MAX_VALUE;
    private Node bestNode;
    private boolean reached;

    public Target(Node node) {
        super(node.x, node.y, node.z);
    }

    public Target(int x, int y, int z) {
        super(x, y, z);
    }

    public void updateBest(float heuristic, Node node) {
        if (heuristic < this.bestHeuristic) {
            this.bestHeuristic = heuristic;
            this.bestNode = node;
        }
    }

    public Node getBestNode() {
        return this.bestNode;
    }

    public void setReached() {
        this.reached = true;
    }

    public boolean isReached() {
        return this.reached;
    }
}

