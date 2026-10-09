/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.PostSpawnProcessor;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CushionItem
extends Item {
    private static final double COLLISION_SHAPE_RAYCAST_EPSILON = 0.001;

    public CushionItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        UseOnContext recalculatedContext = CushionItem.recalculateContextForSpecialCollisionShapes(context);
        Level level = context.getLevel();
        Direction clickedFace = recalculatedContext.getClickedFace();
        if (clickedFace != Direction.UP) {
            return InteractionResult.FAIL;
        }
        BlockPlaceContext placeContext = new BlockPlaceContext(recalculatedContext);
        BlockPos blockPos = placeContext.getClickedPos();
        Vec3 entityPos = Vec3.atCenterOfWithY(blockPos, recalculatedContext.getClickLocation().y);
        AABB spawnAABB = EntityTypes.CUSHION.getSpawnAABB(entityPos);
        if (!Cushion.canBePlacedAt(level, spawnAABB)) {
            return InteractionResult.FAIL;
        }
        ItemStack itemStack = context.getItemInHand();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (!serverLevel.getEntitiesOfClass(Cushion.class, spawnAABB).isEmpty()) {
                return InteractionResult.FAIL;
            }
            PostSpawnProcessor entityConfig = EntityType.createDefaultStackConfig(serverLevel, itemStack, context.getPlayer());
            Cushion cushion = EntityTypes.CUSHION.create(serverLevel, entityConfig, blockPos, EntitySpawnReason.SPAWN_ITEM_USE, true, true);
            if (cushion == null) {
                return InteractionResult.FAIL;
            }
            cushion.snapTo(entityPos, Direction.fromYRot(placeContext.getRotation()).toYRot(), 0.0f);
            serverLevel.addFreshEntity(cushion);
            cushion.destroyIfInFire(serverLevel);
            level.playSound(null, cushion.getX(), cushion.getY(), cushion.getZ(), SoundEvents.CUSHION_PLACE, SoundSource.BLOCKS, 0.75f, 0.8f);
            cushion.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
            itemStack.consume(1, placeContext.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }

    private static UseOnContext recalculateContextForSpecialCollisionShapes(UseOnContext context) {
        BlockPos clickedPos;
        Player player = context.getPlayer();
        if (player == null) {
            return context;
        }
        Level level = context.getLevel();
        BlockState clickedState = level.getBlockState(clickedPos = context.getClickedPos());
        if (!clickedState.is(BlockTags.CUSHION_USES_COLLISION_SHAPE)) {
            return context;
        }
        Vec3 rayFrom = player.getEyePosition();
        Vec3 ray = context.getClickLocation().subtract(rayFrom);
        Vec3 rayTo = context.getClickLocation().add(ray.normalize().scale(0.001));
        BlockHitResult collisionHitResult = clickedState.getCollisionShape(level, clickedPos).clip(rayFrom, rayTo, clickedPos);
        if (collisionHitResult == null) {
            return context;
        }
        return new UseOnContext(player, context.getHand(), collisionHitResult);
    }
}

