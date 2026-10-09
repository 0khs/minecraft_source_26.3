/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Continuation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class Cushion
extends BlockAttachedEntity {
    private static final DyeColor DEFAULT_COLOR = DyeColor.WHITE;
    private static final int LIGHTNING_DROP_INVULNERABLE_TICKS = 20;
    private static final EntityDataAccessor<DyeColor> DATA_COLOR = SynchedEntityData.defineId(Cushion.class, EntityDataSerializers.DYE_COLOR);

    public Cushion(EntityType<Cushion> type, Level level) {
        super((EntityType<? extends BlockAttachedEntity>)type, level);
    }

    public DyeColor getColor() {
        return this.entityData.get(DATA_COLOR);
    }

    public void setColor(DyeColor color) {
        this.entityData.set(DATA_COLOR, color);
    }

    @Override
    public void dropItem(ServerLevel level, @Nullable Entity causedBy) {
        Player player;
        this.playSound(SoundEvents.CUSHION_BREAK, 1.0f, 1.0f);
        this.showBreakingParticles();
        if (!level.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
            return;
        }
        if (causedBy instanceof Player && (player = (Player)causedBy).hasInfiniteMaterials()) {
            return;
        }
        ItemEntity itemEntity = this.spawnAtLocation(level, this.getCushionItemStackWithData());
        if (itemEntity != null && causedBy instanceof LightningBolt) {
            itemEntity.setInvulnerableTime(20);
        }
    }

    private static boolean isBreakingDeniedFor(DamageSource source) {
        Player player;
        Entity entity = source.getEntity();
        return entity instanceof Player && !(player = (Player)entity).mayBuild();
    }

    private boolean isBreakingDeniedAtPosFor(ServerLevel level, DamageSource source) {
        Player player;
        Entity entity = source.getEntity();
        return entity instanceof Player && !level.mayInteract(player = (Player)entity, this.pos);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (Cushion.isBreakingDeniedFor(source) || this.isBreakingDeniedAtPosFor(level, source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        if (Cushion.isBreakingDeniedFor(source)) {
            return false;
        }
        return super.hurtClient(source);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (!player.isSecondaryUseActive() && !this.isVehicle()) {
            if (!this.level().isClientSide() && player.startRiding(this)) {
                this.playSound(SoundEvents.CUSHION_SIT, 1.0f, 1.0f);
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide() && this.getRemovalReason() == null) {
            this.playSound(SoundEvents.CUSHION_GET_UP, 1.0f, 1.0f);
        }
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.CUSHION.pick(this.getColor()));
    }

    @Override
    protected void tickAtCheckInterval() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            BlockPos blockPos = this.blockPosition();
            FluidState fluidState = level2.getBlockState(blockPos).getFluidState();
            if (this.collidedWithFluid(fluidState, blockPos, this.position(), this.position())) {
                fluidState.entityInside(level2, blockPos, this, this.insideEffectCollector);
                this.insideEffectCollector.applyAndClear(this);
            }
            this.destroyIfInFire(level2);
        }
    }

    public void destroyIfInFire(ServerLevel level) {
        if (this.isRemoved()) {
            return;
        }
        level.findBlocksIn(this.getBoundingBox().nextDeflated()).filterState(state -> state.is(BlockTags.FIRE)).forEachUntil((blockPos, blockState) -> {
            this.hurtServer(level, this.damageSources().inFire(), 1.0f);
            return Continuation.ABORT;
        });
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightningBolt) {
        if (!this.isRemoved()) {
            this.kill(level, lightningBolt);
            this.dropItem(level, lightningBolt);
        }
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
        super.setPos(x, y, z);
    }

    private void showBreakingParticles() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            level2.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.WOOL.pick(this.getColor()).defaultBlockState()), this.getX(), this.getY(0.6666666666666666), this.getZ(), 10, this.getBbWidth() / 4.0f, this.getBbHeight() / 4.0f, this.getBbWidth() / 4.0f, 0.05);
        }
    }

    public static boolean canBePlacedAt(Level level, AABB boundingBox) {
        return Cushion.wouldSurviveAt(level, boundingBox) && !Cushion.isAnchorBuried(level, boundingBox);
    }

    public static boolean wouldSurviveAt(Level level, AABB boundingBox) {
        return Cushion.hasAnchorBelow(level, boundingBox) && !Cushion.isCoveredBySuffocatingBlocks(level, boundingBox);
    }

    private static boolean hasAnchorBelow(Level level, AABB boundingBox) {
        AABB anchorBox = new AABB(boundingBox.minX, boundingBox.minY - 0.015625, boundingBox.minZ, Math.nextDown(boundingBox.maxX), boundingBox.minY, Math.nextDown(boundingBox.maxZ));
        return level.findBlocksIn(anchorBox.expandTowards(0.0, -0.125, 0.0)).forEachUntil((blockPos, blockState) -> {
            VoxelShape shape = blockState.getShape(level, blockPos);
            if (!shape.isEmpty() && shape.bounds().move(blockPos).intersects(anchorBox)) {
                return Continuation.ABORT;
            }
            return Continuation.CONTINUE;
        });
    }

    private static boolean isAnchorBuried(Level level, AABB boundingBox) {
        AABB restingSlice = new AABB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.minY + 0.015625, boundingBox.maxZ).nextDeflated();
        VoxelShape exposedSurface = Shapes.create(restingSlice);
        for (VoxelShape collider : level.getBlockCollisions(null, restingSlice)) {
            if (!(exposedSurface = Shapes.join(exposedSurface, collider, BooleanOp.ONLY_FIRST)).isEmpty()) continue;
            return true;
        }
        return false;
    }

    private static boolean isCoveredBySuffocatingBlocks(Level level, AABB boundingBox) {
        for (BlockPos blockPos : BlockPos.betweenClosed(boundingBox.nextDeflated())) {
            if (level.getBlockState(blockPos).isSuffocating(level, blockPos)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean survives() {
        return Cushion.wouldSurviveAt(this.level(), this.getBoundingBox());
    }

    @Override
    protected void recalculateBoundingBox() {
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_COLOR, DEFAULT_COLOR);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("color", DyeColor.CODEC, this.getColor());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setColor(input.read("color", DyeColor.CODEC).orElse(DEFAULT_COLOR));
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> type) {
        if (type == DataComponents.CUSHION_COLOR) {
            return Cushion.castComponentValue(type, this.getColor());
        }
        return super.get(type);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        this.applyImplicitComponentIfPresent(components, DataComponents.CUSHION_COLOR);
        super.applyImplicitComponents(components);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> type, T value) {
        if (type == DataComponents.CUSHION_COLOR) {
            this.setColor(Cushion.castComponentValue(DataComponents.CUSHION_COLOR, value));
            return true;
        }
        return super.applyImplicitComponent(type, value);
    }

    private ItemStack getCushionItemStackWithData() {
        ItemStack itemStack = new ItemStack(Items.CUSHION.pick(this.getColor()));
        itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        return itemStack;
    }
}

