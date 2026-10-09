/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 */
package net.minecraft.world.entity.projectile.throwableitemprojectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.PotionTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class AbstractThrownPotion
extends ThrowableItemProjectile {
    public static final double SPLASH_RANGE = 4.0;
    protected static final double SPLASH_RANGE_SQ = 16.0;
    public static final Predicate<LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = livingEntity -> livingEntity.isSensitiveToWater() || livingEntity.isOnFire();

    public AbstractThrownPotion(EntityType<? extends AbstractThrownPotion> type, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)type, level);
    }

    public AbstractThrownPotion(EntityType<? extends AbstractThrownPotion> type, Level level, LivingEntity owner, ItemStack itemStack) {
        super(type, owner, level, itemStack);
    }

    public AbstractThrownPotion(EntityType<? extends AbstractThrownPotion> type, Level level, double x, double y, double z, ItemStack itemStack) {
        super(type, x, y, z, level, itemStack);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack potionItemStack = this.getItem();
        Direction hitDirection = hitResult.getDirection();
        BlockPos blockHitPos = hitResult.getBlockPos();
        BlockPos blockEffectPos = blockHitPos.relative(hitDirection);
        PotionContents potion = potionItemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (potion.is(PotionTags.DOUSES_FIRE)) {
            this.douseFire(blockEffectPos);
            this.douseFire(blockEffectPos.relative(hitDirection.getOpposite()));
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                this.douseFire(blockEffectPos.relative(direction));
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        ItemStack potionItemStack = this.getItem();
        PotionContents potion = potionItemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        this.affectEntitiesAround(level2, potion);
        if (potion.hasEffects()) {
            this.onHitAsPotion(level2, potionItemStack, hitResult);
        }
        if (potion.potion().isPresent() && potion.potion().get().value().hasInstantEffects()) {
            level2.levelEvent(2007, this.blockPosition(), potion.getColor());
            if (!this.isSilent()) {
                level2.levelEvent(1054, this.blockPosition(), 0);
            }
        } else {
            level2.levelEvent(2002, this.blockPosition(), potion.getColor());
            if (!this.isSilent()) {
                level2.levelEvent(1053, this.blockPosition(), 0);
            }
        }
        this.discard();
    }

    private void affectEntitiesAround(ServerLevel level, PotionContents potion) {
        boolean hurtsWaterSensitiveEntities = potion.is(PotionTags.HURTS_WATER_SENSITIVE_ENTITIES);
        boolean extinguishesEntities = potion.is(PotionTags.EXTINGUISHES_ENTITIES);
        AABB aabb = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        if (hurtsWaterSensitiveEntities || extinguishesEntities) {
            List<LivingEntity> burningOrWaterSensitiveEntities = this.level().getEntitiesOfClass(LivingEntity.class, aabb, WATER_SENSITIVE_OR_ON_FIRE);
            for (LivingEntity entity : burningOrWaterSensitiveEntities) {
                double dist = this.distanceToSqr(entity);
                if (!(dist < 16.0)) continue;
                if (hurtsWaterSensitiveEntities && entity.isSensitiveToWater()) {
                    entity.hurtServer(level, this.damageSources().indirectMagic(this, this.getOwner()), 1.0f);
                }
                if (!extinguishesEntities || !entity.isOnFire() || !entity.isAlive()) continue;
                entity.extinguishFire();
            }
        }
        if (potion.is(PotionTags.REHYDRATES_AXOLOTLS)) {
            List<Axolotl> axolotlEntities = this.level().getEntitiesOfClass(Axolotl.class, aabb);
            for (Axolotl axolotl : axolotlEntities) {
                axolotl.rehydrate();
            }
        }
    }

    protected abstract void onHitAsPotion(ServerLevel var1, ItemStack var2, HitResult var3);

    private void douseFire(BlockPos pos) {
        BlockState blockState = this.level().getBlockState(pos);
        if (blockState.is(BlockTags.FIRE)) {
            this.level().destroyBlock(pos, false, this);
        } else if (AbstractCandleBlock.isLit(blockState)) {
            AbstractCandleBlock.extinguish(null, blockState, this.level(), pos);
        } else if (CampfireBlock.isLitCampfire(blockState)) {
            this.level().levelEvent(null, 1009, pos, 0);
            CampfireBlock.douse(this.getOwner(), this.level(), pos, blockState);
            this.level().setBlockAndUpdate(pos, (BlockState)blockState.setValue(CampfireBlock.LIT, false));
        }
    }

    @Override
    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity hurtEntity, DamageSource damageSource) {
        double dx = hurtEntity.position().x - this.position().x;
        double dz = hurtEntity.position().z - this.position().z;
        return DoubleDoubleImmutablePair.of((double)dx, (double)dz);
    }
}

