/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.phys;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlockHitResult
extends HitResult {
    public static final StreamCodec<ByteBuf, BlockHitResult> STREAM_CODEC = new StreamCodec<ByteBuf, BlockHitResult>(){

        @Override
        public BlockHitResult decode(ByteBuf input) {
            BlockPos pos = (BlockPos)BlockPos.STREAM_CODEC.decode(input);
            Direction face = (Direction)Direction.STREAM_CODEC.decode(input);
            float clickX = input.readFloat();
            float clickY = input.readFloat();
            float clickZ = input.readFloat();
            boolean inside = input.readBoolean();
            boolean worldBorder = input.readBoolean();
            return new BlockHitResult(new Vec3((double)pos.getX() + (double)clickX, (double)pos.getY() + (double)clickY, (double)pos.getZ() + (double)clickZ), face, pos, inside, worldBorder);
        }

        @Override
        public void encode(ByteBuf output, BlockHitResult blockHit) {
            BlockPos blockPos = blockHit.getBlockPos();
            BlockPos.STREAM_CODEC.encode(output, blockPos);
            Direction.STREAM_CODEC.encode(output, blockHit.getDirection());
            Vec3 location = blockHit.getLocation();
            output.writeFloat((float)(location.x - (double)blockPos.getX()));
            output.writeFloat((float)(location.y - (double)blockPos.getY()));
            output.writeFloat((float)(location.z - (double)blockPos.getZ()));
            output.writeBoolean(blockHit.isInside());
            output.writeBoolean(blockHit.isWorldBorderHit());
        }
    };
    private final Direction direction;
    private final BlockPos blockPos;
    private final boolean miss;
    private final boolean inside;
    private final boolean worldBorderHit;

    public static BlockHitResult miss(Vec3 location, Direction direction, BlockPos pos) {
        return new BlockHitResult(true, location, direction, pos, false, false);
    }

    public BlockHitResult(Vec3 location, Direction direction, BlockPos pos, boolean inside) {
        this(false, location, direction, pos, inside, false);
    }

    public BlockHitResult(Vec3 location, Direction direction, BlockPos pos, boolean inside, boolean worldBorderHit) {
        this(false, location, direction, pos, inside, worldBorderHit);
    }

    private BlockHitResult(boolean miss, Vec3 location, Direction direction, BlockPos blockPos, boolean inside, boolean worldBorderHit) {
        super(location);
        this.miss = miss;
        this.direction = direction;
        this.blockPos = blockPos;
        this.inside = inside;
        this.worldBorderHit = worldBorderHit;
    }

    public BlockHitResult withDirection(Direction direction) {
        return new BlockHitResult(this.miss, this.location, direction, this.blockPos, this.inside, this.worldBorderHit);
    }

    public BlockHitResult withPosition(BlockPos blockPos) {
        return new BlockHitResult(this.miss, this.location, this.direction, blockPos, this.inside, this.worldBorderHit);
    }

    public BlockHitResult hitBorder() {
        return new BlockHitResult(this.miss, this.location, this.direction, this.blockPos, this.inside, true);
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public HitResult.Type getType() {
        return this.miss ? HitResult.Type.MISS : HitResult.Type.BLOCK;
    }

    public boolean isInside() {
        return this.inside;
    }

    public boolean isWorldBorderHit() {
        return this.worldBorderHit;
    }
}

