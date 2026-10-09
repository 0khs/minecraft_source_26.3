/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;

public abstract class HumanoidMobRenderer<T extends Mob, S extends HumanoidRenderState, M extends HumanoidModel<S>>
extends AgeableMobRenderer<T, S, M> {
    public HumanoidMobRenderer(EntityRendererProvider.Context context, M model, float shadow) {
        this(context, model, model, shadow);
    }

    public HumanoidMobRenderer(EntityRendererProvider.Context context, M model, M babyModel, float shadow) {
        this(context, model, babyModel, shadow, CustomHeadLayer.Transforms.DEFAULT);
    }

    public HumanoidMobRenderer(EntityRendererProvider.Context context, M model, M babyModel, float shadow, CustomHeadLayer.Transforms customHeadTransforms) {
        super(context, model, babyModel, shadow);
        this.addLayer(new CustomHeadLayer(this, context.getModelSet(), context.getPlayerSkinRenderCache(), customHeadTransforms));
        this.addLayer(new WingsLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new ItemInHandLayer(this));
    }

    protected HumanoidModel.ArmPose getArmPose(T mob, HumanoidArm arm) {
        ItemStack itemHeldByArm = ((LivingEntity)mob).getItemHeldByArm(arm);
        if (HumanoidMobRenderer.usesSpearPose(itemHeldByArm, arm, mob)) {
            return HumanoidModel.ArmPose.SPEAR;
        }
        return HumanoidModel.ArmPose.EMPTY;
    }

    public static boolean usesSpearPose(ItemStack item, HumanoidArm arm, LivingEntity entity) {
        LivingEntity.SwingDescription currentSwing = entity.getCurrentSwing();
        return currentSwing != null && currentSwing.hand().asArm(entity.getMainArm()) == arm && currentSwing.animation().type() == SwingAnimationType.STAB || item.is(ItemTags.SPEARS);
    }

    @Override
    public void extractRenderState(T entity, S state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
        ((HumanoidRenderState)state).leftArmPose = this.getArmPose(entity, HumanoidArm.LEFT);
        ((HumanoidRenderState)state).rightArmPose = this.getArmPose(entity, HumanoidArm.RIGHT);
    }

    public static void extractHumanoidRenderState(LivingEntity entity, HumanoidRenderState state, float partialTicks, ItemModelResolver itemModelResolver) {
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, itemModelResolver, partialTicks);
        state.isCrouching = entity.isCrouching();
        state.isFallFlying = entity.isFallFlying();
        state.isVisuallySwimming = entity.isVisuallySwimming();
        state.isPassenger = entity.isPassenger();
        state.speedValue = 1.0f;
        if (state.isFallFlying) {
            state.speedValue = (float)entity.getDeltaMovement().lengthSqr();
            state.speedValue /= 0.2f;
            state.speedValue *= state.speedValue * state.speedValue;
        }
        if (state.speedValue < 1.0f) {
            state.speedValue = 1.0f;
        }
        state.swimAmount = entity.getSwimAmount(partialTicks);
        state.useItemHand = entity.getUsedItemHand();
        state.maxCrossbowChargeDuration = CrossbowItem.getChargeDuration(entity.getUseItem(), entity);
        state.ticksUsingItem = entity.getTicksUsingItem(partialTicks);
        state.isUsingItem = entity.isUsingItem();
        state.elytraRotX = entity.elytraAnimationState.getRotX(partialTicks);
        state.elytraRotY = entity.elytraAnimationState.getRotY(partialTicks);
        state.elytraRotZ = entity.elytraAnimationState.getRotZ(partialTicks);
        state.headEquipment = HumanoidMobRenderer.getEquipmentIfRenderable(entity, EquipmentSlot.HEAD);
        state.chestEquipment = HumanoidMobRenderer.getEquipmentIfRenderable(entity, EquipmentSlot.CHEST);
        state.legsEquipment = HumanoidMobRenderer.getEquipmentIfRenderable(entity, EquipmentSlot.LEGS);
        state.feetEquipment = HumanoidMobRenderer.getEquipmentIfRenderable(entity, EquipmentSlot.FEET);
    }

    private static ItemStack getEquipmentIfRenderable(LivingEntity entity, EquipmentSlot slot) {
        ItemStack itemStack = entity.getItemBySlot(slot);
        return HumanoidArmorLayer.shouldRender(itemStack, slot) ? itemStack.copy() : ItemStack.EMPTY;
    }
}

