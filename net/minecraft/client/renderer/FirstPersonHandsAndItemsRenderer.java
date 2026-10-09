/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;

public class FirstPersonHandsAndItemsRenderer {
    private static final RenderType MAP_BACKGROUND = RenderTypes.text(Identifier.withDefaultNamespace("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderTypes.text(Identifier.withDefaultNamespace("textures/map/map_background_checkerboard.png"));
    private static final float ITEM_SWING_X_POS_SCALE = -0.4f;
    private static final float ITEM_SWING_Y_POS_SCALE = 0.2f;
    private static final float ITEM_SWING_Z_POS_SCALE = -0.2f;
    private static final float ITEM_HEIGHT_SCALE = -0.6f;
    private static final float ITEM_POS_X = 0.56f;
    private static final float ITEM_POS_Y = -0.52f;
    private static final float ITEM_POS_Z = -0.72f;
    private static final float ITEM_PRESWING_ROT_Y = 45.0f;
    private static final float ITEM_SWING_X_ROT_AMOUNT = -80.0f;
    private static final float ITEM_SWING_Y_ROT_AMOUNT = -20.0f;
    private static final float ITEM_SWING_Z_ROT_AMOUNT = -20.0f;
    private static final float EAT_JIGGLE_X_ROT_AMOUNT = 10.0f;
    private static final float EAT_JIGGLE_Y_ROT_AMOUNT = 90.0f;
    private static final float EAT_JIGGLE_Z_ROT_AMOUNT = 30.0f;
    private static final float EAT_JIGGLE_X_POS_SCALE = 0.6f;
    private static final float EAT_JIGGLE_Y_POS_SCALE = -0.5f;
    private static final float EAT_JIGGLE_Z_POS_SCALE = 0.0f;
    private static final double EAT_JIGGLE_EXPONENT = 27.0;
    private static final float EAT_EXTRA_JIGGLE_CUTOFF = 0.8f;
    private static final float EAT_EXTRA_JIGGLE_SCALE = 0.1f;
    private static final float ARM_SWING_X_POS_SCALE = -0.3f;
    private static final float ARM_SWING_Y_POS_SCALE = 0.4f;
    private static final float ARM_SWING_Z_POS_SCALE = -0.4f;
    private static final float ARM_SWING_Y_ROT_AMOUNT = 70.0f;
    private static final float ARM_SWING_Z_ROT_AMOUNT = -20.0f;
    private static final float ARM_HEIGHT_SCALE = -0.6f;
    private static final float ARM_POS_SCALE = 0.8f;
    private static final float ARM_POS_X = 0.8f;
    private static final float ARM_POS_Y = -0.75f;
    private static final float ARM_POS_Z = -0.9f;
    private static final float ARM_PRESWING_ROT_Y = 45.0f;
    private static final float ARM_PREROTATION_X_OFFSET = -1.0f;
    private static final float ARM_PREROTATION_Y_OFFSET = 3.6f;
    private static final float ARM_PREROTATION_Z_OFFSET = 3.5f;
    private static final float ARM_POSTROTATION_X_OFFSET = 5.6f;
    private static final int ARM_ROT_X = 200;
    private static final int ARM_ROT_Y = -135;
    private static final int ARM_ROT_Z = 120;
    private static final float MAP_SWING_X_POS_SCALE = -0.4f;
    private static final float MAP_SWING_Z_POS_SCALE = -0.2f;
    private static final float MAP_HANDS_POS_X = 0.0f;
    private static final float MAP_HANDS_POS_Y = 0.04f;
    private static final float MAP_HANDS_POS_Z = -0.72f;
    private static final float MAP_HANDS_HEIGHT_SCALE = -1.2f;
    private static final float MAP_HANDS_TILT_SCALE = -0.5f;
    private static final float MAP_PLAYER_PITCH_SCALE = 45.0f;
    private static final float MAP_HANDS_Z_ROT_AMOUNT = -85.0f;
    private static final float MAPHAND_X_ROT_AMOUNT = 45.0f;
    private static final float MAPHAND_Y_ROT_AMOUNT = 92.0f;
    private static final float MAPHAND_Z_ROT_AMOUNT = -41.0f;
    private static final float MAP_HAND_X_POS = 0.3f;
    private static final float MAP_HAND_Y_POS = -1.1f;
    private static final float MAP_HAND_Z_POS = 0.45f;
    private static final float MAP_SWING_X_ROT_AMOUNT = 20.0f;
    private static final float MAP_PRE_ROT_SCALE = 0.38f;
    private static final float MAP_GLOBAL_X_POS = -0.5f;
    private static final float MAP_GLOBAL_Y_POS = -0.5f;
    private static final float MAP_GLOBAL_Z_POS = 0.0f;
    private static final float MAP_FINAL_SCALE = 0.0078125f;
    private static final int MAP_BORDER = 7;
    private static final int MAP_HEIGHT = 128;
    private static final int MAP_WIDTH = 128;
    private static final float BOW_CHARGE_X_POS_SCALE = 0.0f;
    private static final float BOW_CHARGE_Y_POS_SCALE = 0.0f;
    private static final float BOW_CHARGE_Z_POS_SCALE = 0.04f;
    private static final float BOW_CHARGE_SHAKE_X_SCALE = 0.0f;
    private static final float BOW_CHARGE_SHAKE_Y_SCALE = 0.004f;
    private static final float BOW_CHARGE_SHAKE_Z_SCALE = 0.0f;
    private static final float BOW_CHARGE_Z_SCALE = 0.2f;
    private static final float BOW_MIN_SHAKE_CHARGE = 0.1f;
    private final Minecraft minecraft;

    public FirstPersonHandsAndItemsRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private float calculateMapTilt(float xRot) {
        float tilt = 1.0f - xRot / 45.0f + 0.1f;
        tilt = Mth.clamp(tilt, 0.0f, 1.0f);
        tilt = -Mth.cos(tilt * (float)Math.PI) * 0.5f + 0.5f;
        return tilt;
    }

    private void renderPlayerHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, HumanoidArm arm, PlayerRenderState playerState) {
        AvatarRenderState avatarRenderState = playerState.avatarRenderState;
        if (avatarRenderState == null) {
            return;
        }
        AvatarRenderer<?> avatarRenderer = this.minecraft.getEntityRenderDispatcher().getRenderer(avatarRenderState);
        Identifier skinTexture = avatarRenderState.skin.body().texturePath();
        if (arm == HumanoidArm.RIGHT) {
            avatarRenderer.renderRightHand(poseStack, submitNodeCollector, lightCoords, skinTexture, avatarRenderState.showRightSleeve);
        } else {
            avatarRenderer.renderLeftHand(poseStack, submitNodeCollector, lightCoords, skinTexture, avatarRenderState.showLeftSleeve);
        }
    }

    private void renderMapHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, HumanoidArm arm, PlayerRenderState playerState) {
        poseStack.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        poseStack.rotateDegrees(Axis.YP, 92.0f);
        poseStack.rotateDegrees(Axis.XP, 45.0f);
        poseStack.rotateDegrees(Axis.ZP, invert * -41.0f);
        poseStack.translate(invert * 0.3f, -1.1f, 0.45f);
        this.renderPlayerHand(poseStack, submitNodeCollector, lightCoords, arm, playerState);
        poseStack.popPose();
    }

    private void renderOneHandedMap(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, HumanoidArm arm, float attackValue, ItemStack map, PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state) {
        float invert = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        poseStack.translate(invert * 0.125f, -0.125f, 0.0f);
        AvatarRenderState avatarRenderState = playerState.avatarRenderState;
        if (avatarRenderState == null) {
            return;
        }
        if (!avatarRenderState.isInvisible) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.ZP, invert * 10.0f);
            this.renderPlayerArm(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attackValue, arm, playerState);
            poseStack.popPose();
        }
        poseStack.pushPose();
        poseStack.translate(invert * 0.51f, -0.08f + inverseArmHeight * -1.2f, -0.75f);
        float sqrtAttackValue = Mth.sqrt(attackValue);
        float xSwing = Mth.sin(sqrtAttackValue * (float)Math.PI);
        float xSwingPosition = -0.5f * xSwing;
        float ySwingPosition = 0.4f * Mth.sin(sqrtAttackValue * ((float)Math.PI * 2));
        float zSwingPosition = -0.3f * Mth.sin(attackValue * (float)Math.PI);
        poseStack.translate(invert * xSwingPosition, ySwingPosition - 0.3f * xSwing, zSwingPosition);
        poseStack.rotateDegrees(Axis.XP, xSwing * -45.0f);
        poseStack.rotateDegrees(Axis.YP, invert * xSwing * -30.0f);
        this.renderMap(poseStack, submitNodeCollector, lightCoords, map, arm == avatarRenderState.mainArm, state);
        poseStack.popPose();
    }

    private void renderTwoHandedMap(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float xRot, float inverseArmHeight, float attackValue, PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state) {
        float sqrtAttackValue = Mth.sqrt(attackValue);
        float ySwingPosition = -0.2f * Mth.sin(attackValue * (float)Math.PI);
        float zSwingPosition = -0.4f * Mth.sin(sqrtAttackValue * (float)Math.PI);
        poseStack.translate(0.0f, -ySwingPosition / 2.0f, zSwingPosition);
        float mapTilt = this.calculateMapTilt(xRot);
        poseStack.translate(0.0f, 0.04f + inverseArmHeight * -1.2f + mapTilt * -0.5f, -0.72f);
        poseStack.rotateDegrees(Axis.XP, mapTilt * -85.0f);
        AvatarRenderState avatarRenderState = playerState.avatarRenderState;
        if (avatarRenderState == null) {
            return;
        }
        if (!avatarRenderState.isInvisible) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.YP, 90.0f);
            this.renderMapHand(poseStack, submitNodeCollector, lightCoords, HumanoidArm.RIGHT, playerState);
            this.renderMapHand(poseStack, submitNodeCollector, lightCoords, HumanoidArm.LEFT, playerState);
            poseStack.popPose();
        }
        float xzSwingRotation = Mth.sin(sqrtAttackValue * (float)Math.PI);
        poseStack.rotateDegrees(Axis.XP, xzSwingRotation * 20.0f);
        poseStack.scale(2.0f, 2.0f, 2.0f);
        this.renderMap(poseStack, submitNodeCollector, lightCoords, state.mainHandItem, true, state);
    }

    private void renderMap(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ItemStack itemStack, boolean mainHand, FirstPersonHandsAndItemsRenderState state) {
        poseStack.rotateDegrees(Axis.YP, 180.0f);
        poseStack.rotateDegrees(Axis.ZP, 180.0f);
        poseStack.scale(0.38f, 0.38f, 0.38f);
        poseStack.translate(-0.5f, -0.5f, 0.0f);
        poseStack.scale(0.0078125f, 0.0078125f, 0.0078125f);
        boolean hasMapData = mainHand ? state.hasMainHandMapData : state.hasOffHandMapData;
        MapRenderState mapRenderState = mainHand ? state.mainHandMapRenderState : state.offHandMapRenderState;
        RenderType renderType = hasMapData ? MAP_BACKGROUND_CHECKERBOARD : MAP_BACKGROUND;
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            buffer.addVertex(pose, -7.0f, 135.0f, 0.0f).setColor(-1).setUv(0.0f, 1.0f).setLight(lightCoords);
            buffer.addVertex(pose, 135.0f, 135.0f, 0.0f).setColor(-1).setUv(1.0f, 1.0f).setLight(lightCoords);
            buffer.addVertex(pose, 135.0f, -7.0f, 0.0f).setColor(-1).setUv(1.0f, 0.0f).setLight(lightCoords);
            buffer.addVertex(pose, -7.0f, -7.0f, 0.0f).setColor(-1).setUv(0.0f, 0.0f).setLight(lightCoords);
        });
        if (hasMapData) {
            this.minecraft.getMapRenderer().render(mapRenderState, poseStack, submitNodeCollector, false, lightCoords);
        }
    }

    private void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm, PlayerRenderState playerState) {
        boolean isRightArm = arm != HumanoidArm.LEFT;
        float invert = isRightArm ? 1.0f : -1.0f;
        float sqrtAttackValue = Mth.sqrt(attackValue);
        float xSwingPosition = -0.3f * Mth.sin(sqrtAttackValue * (float)Math.PI);
        float ySwingPosition = 0.4f * Mth.sin(sqrtAttackValue * ((float)Math.PI * 2));
        float zSwingPosition = -0.4f * Mth.sin(attackValue * (float)Math.PI);
        poseStack.translate(invert * (xSwingPosition + 0.64000005f), ySwingPosition + -0.6f + inverseArmHeight * -0.6f, zSwingPosition + -0.71999997f);
        poseStack.rotateDegrees(Axis.YP, invert * 45.0f);
        float zSwingRotation = Mth.sin(attackValue * attackValue * (float)Math.PI);
        float ySwingRotation = Mth.sin(sqrtAttackValue * (float)Math.PI);
        poseStack.rotateDegrees(Axis.YP, invert * ySwingRotation * 70.0f);
        poseStack.rotateDegrees(Axis.ZP, invert * zSwingRotation * -20.0f);
        poseStack.translate(invert * -1.0f, 3.6f, 3.5f);
        poseStack.rotateDegrees(Axis.ZP, invert * 120.0f);
        poseStack.rotateDegrees(Axis.XP, 200.0f);
        poseStack.rotateDegrees(Axis.YP, invert * -135.0f);
        poseStack.translate(invert * 5.6f, 0.0f, 0.0f);
        this.renderPlayerHand(poseStack, submitNodeCollector, lightCoords, arm, playerState);
    }

    private void applyEatTransform(PoseStack poseStack, float partialTicks, HumanoidArm arm, float useItemRemainingTicks, int useDuration) {
        float currUsageTime = useItemRemainingTicks - partialTicks + 1.0f;
        float scaledUsageTime = currUsageTime / (float)useDuration;
        if (scaledUsageTime < 0.8f) {
            float extraHeightOffset = Mth.abs(Mth.cos(currUsageTime / 4.0f * (float)Math.PI) * 0.1f);
            poseStack.translate(0.0f, extraHeightOffset, 0.0f);
        }
        float eatJiggle = 1.0f - (float)Math.pow(scaledUsageTime, 27.0);
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(eatJiggle * 0.6f * (float)invert, eatJiggle * -0.5f, eatJiggle * 0.0f);
        poseStack.rotateDegrees(Axis.YP, (float)invert * eatJiggle * 90.0f);
        poseStack.rotateDegrees(Axis.XP, eatJiggle * 10.0f);
        poseStack.rotateDegrees(Axis.ZP, (float)invert * eatJiggle * 30.0f);
    }

    private void applyBrushTransform(PoseStack poseStack, float partialTicks, HumanoidArm arm, float useItemRemainingTicks) {
        float brushAnimationRemainingTicks = useItemRemainingTicks % 10.0f;
        float deltaSinceLastUpdate = brushAnimationRemainingTicks - partialTicks + 1.0f;
        float scaledUsageTime = 1.0f - deltaSinceLastUpdate / 10.0f;
        float minSwipeAngle = -90.0f;
        float maxSwipeAngle = 60.0f;
        float swipeRange = 150.0f;
        float swipeCenter = -15.0f;
        int swipeSpeed = 2;
        float currentSwipeAngle = -15.0f + 75.0f * Mth.cos(scaledUsageTime * 2.0f * (float)Math.PI);
        if (arm != HumanoidArm.RIGHT) {
            poseStack.translate(0.1, 0.83, 0.35);
            poseStack.rotateDegrees(Axis.XP, -80.0f);
            poseStack.rotateDegrees(Axis.YP, -90.0f);
            poseStack.rotateDegrees(Axis.XP, currentSwipeAngle);
            poseStack.translate(-0.3, 0.22, 0.35);
        } else {
            poseStack.translate(-0.25, 0.22, 0.35);
            poseStack.rotateDegrees(Axis.XP, -80.0f);
            poseStack.rotateDegrees(Axis.YP, 90.0f);
            poseStack.rotateDegrees(Axis.ZP, 0.0f);
            poseStack.rotateDegrees(Axis.XP, currentSwipeAngle);
        }
    }

    private void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float attackValue) {
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        float ySwingRotation = Mth.sin(attackValue * attackValue * (float)Math.PI);
        poseStack.rotateDegrees(Axis.YP, (float)invert * (45.0f + ySwingRotation * -20.0f));
        float xzSwingRotation = Mth.sin(Mth.sqrt(attackValue) * (float)Math.PI);
        poseStack.rotateDegrees(Axis.ZP, (float)invert * xzSwingRotation * -20.0f);
        poseStack.rotateDegrees(Axis.XP, xzSwingRotation * -80.0f);
        poseStack.rotateDegrees(Axis.YP, (float)invert * -45.0f);
    }

    private void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float inverseArmHeight) {
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float)invert * 0.56f, -0.52f + inverseArmHeight * -0.6f, -0.72f);
    }

    public void submitHandsWithItems(float partialTicks, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state) {
        AvatarRenderState avatarRenderState = playerState.avatarRenderState;
        if (avatarRenderState == null || state.handRenderSelection == null) {
            return;
        }
        float attackValue = avatarRenderState.swingAnimation;
        InteractionHand attackHand = state.attackHand;
        float xRot = avatarRenderState.xRot;
        poseStack.rotateDegrees(Axis.XP, (state.viewXRot - state.xBob) * 0.1f);
        poseStack.rotateDegrees(Axis.YP, (state.viewYRot - state.yBob) * 0.1f);
        if (state.handRenderSelection.renderMainHand) {
            float mainHandAttack = attackHand == InteractionHand.MAIN_HAND ? attackValue : 0.0f;
            float mainhandInverseArmHeight = state.mainHandSwapScale * (1.0f - Mth.lerp(partialTicks, state.oldMainHandHeight, state.mainHandHeight));
            this.submitArmWithItem(playerState, state, partialTicks, xRot, InteractionHand.MAIN_HAND, mainHandAttack, state.mainHandItem, mainhandInverseArmHeight, poseStack, submitNodeCollector, playerState.avatarRenderState.lightCoords);
        }
        if (state.handRenderSelection.renderOffHand) {
            float offHandAttack = attackHand == InteractionHand.OFF_HAND ? attackValue : 0.0f;
            float offhandInverseArmHeight = state.offHandSwapScale * (1.0f - Mth.lerp(partialTicks, state.oldOffHandHeight, state.offHandHeight));
            this.submitArmWithItem(playerState, state, partialTicks, xRot, InteractionHand.OFF_HAND, offHandAttack, state.offHandItem, offhandInverseArmHeight, poseStack, submitNodeCollector, playerState.avatarRenderState.lightCoords);
        }
    }

    private void submitArmWithItem(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        if (state.isScoping) {
            return;
        }
        AvatarRenderState avatarRenderState = playerState.avatarRenderState;
        if (avatarRenderState == null) {
            return;
        }
        boolean isMainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = isMainHand ? avatarRenderState.mainArm : avatarRenderState.mainArm.getOpposite();
        int useDuration = isMainHand ? state.mainHandUseDuration : state.offHandUseDuration;
        int chargeDuration = isMainHand ? state.mainHandChargeDuration : state.offHandChargeDuration;
        poseStack.pushPose();
        if (itemStack.isEmpty()) {
            if (isMainHand && !avatarRenderState.isInvisible) {
                this.renderPlayerArm(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attack, arm, playerState);
            }
        } else if (itemStack.has(DataComponents.MAP_ID)) {
            if (isMainHand && state.offHandItem.isEmpty()) {
                this.renderTwoHandedMap(poseStack, submitNodeCollector, lightCoords, xRot, inverseArmHeight, attack, playerState, state);
            } else {
                this.renderOneHandedMap(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, arm, attack, itemStack, playerState, state);
            }
        } else if (itemStack.is(Items.CROSSBOW)) {
            int invert;
            this.applyItemArmTransform(poseStack, arm, inverseArmHeight);
            boolean charged = CrossbowItem.isCharged(itemStack);
            boolean isRightArm = arm == HumanoidArm.RIGHT;
            int n = invert = isRightArm ? 1 : -1;
            if (avatarRenderState.isUsingItem && state.useItemRemainingTicks > 0 && avatarRenderState.useItemHand == hand && !charged) {
                poseStack.translate((float)invert * -0.4785682f, -0.094387f, 0.05731531f);
                poseStack.rotateDegrees(Axis.XP, -11.935f);
                poseStack.rotateDegrees(Axis.YP, (float)invert * 65.3f);
                poseStack.rotateDegrees(Axis.ZP, (float)invert * -9.785f);
                float timeHeld = (float)useDuration - ((float)state.useItemRemainingTicks - partialTicks + 1.0f);
                float power = timeHeld / (float)chargeDuration;
                if (power > 1.0f) {
                    power = 1.0f;
                }
                if (power > 0.1f) {
                    float shakeOffset = Mth.sin((timeHeld - 0.1f) * 1.3f);
                    float shakeIntensity = power - 0.1f;
                    float shake = shakeOffset * shakeIntensity;
                    poseStack.translate(shake * 0.0f, shake * 0.004f, shake * 0.0f);
                }
                poseStack.translate(power * 0.0f, power * 0.0f, power * 0.04f);
                poseStack.scale(1.0f, 1.0f, 1.0f + power * 0.2f);
                poseStack.rotateDegrees(Axis.YN, (float)invert * 45.0f);
            } else {
                this.swingArm(attack, poseStack, invert, arm);
                if (charged && attack < 0.001f && isMainHand) {
                    poseStack.translate((float)invert * -0.641864f, 0.0f, 0.0f);
                    poseStack.rotateDegrees(Axis.YP, (float)invert * 10.0f);
                }
            }
            (isMainHand ? state.mainHandRenderState : state.offHandRenderState).submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
        } else {
            int invert;
            boolean isRightArm = arm == HumanoidArm.RIGHT;
            int n = invert = isRightArm ? 1 : -1;
            if (avatarRenderState.isUsingItem && state.useItemRemainingTicks > 0 && avatarRenderState.useItemHand == hand) {
                ItemUseAnimation useAnimation = itemStack.getUseAnimation();
                if (!useAnimation.hasCustomArmTransform()) {
                    this.applyItemArmTransform(poseStack, arm, inverseArmHeight);
                }
                switch (useAnimation) {
                    case NONE: {
                        break;
                    }
                    case EAT: 
                    case DRINK: {
                        this.applyEatTransform(poseStack, partialTicks, arm, state.useItemRemainingTicks, useDuration);
                        this.applyItemArmTransform(poseStack, arm, inverseArmHeight);
                        break;
                    }
                    case BLOCK: {
                        if (itemStack.getItem() instanceof ShieldItem) break;
                        poseStack.translate((float)invert * -0.14142136f, 0.08f, 0.14142136f);
                        poseStack.rotateDegrees(Axis.XP, -102.25f);
                        poseStack.rotateDegrees(Axis.YP, (float)invert * 13.365f);
                        poseStack.rotateDegrees(Axis.ZP, (float)invert * 78.05f);
                        break;
                    }
                    case BOW: {
                        poseStack.translate((float)invert * -0.2785682f, 0.18344387f, 0.15731531f);
                        poseStack.rotateDegrees(Axis.XP, -13.935f);
                        poseStack.rotateDegrees(Axis.YP, (float)invert * 35.3f);
                        poseStack.rotateDegrees(Axis.ZP, (float)invert * -9.785f);
                        float timeHeld = (float)useDuration - ((float)state.useItemRemainingTicks - partialTicks + 1.0f);
                        float power = timeHeld / 20.0f;
                        power = (power * power + power * 2.0f) / 3.0f;
                        if (power > 1.0f) {
                            power = 1.0f;
                        }
                        if (power > 0.1f) {
                            float shakeOffset = Mth.sin((timeHeld - 0.1f) * 1.3f);
                            float shakeIntensity = power - 0.1f;
                            float shake = shakeOffset * shakeIntensity;
                            poseStack.translate(shake * 0.0f, shake * 0.004f, shake * 0.0f);
                        }
                        poseStack.translate(power * 0.0f, power * 0.0f, power * 0.04f);
                        poseStack.scale(1.0f, 1.0f, 1.0f + power * 0.2f);
                        poseStack.rotateDegrees(Axis.YN, (float)invert * 45.0f);
                        break;
                    }
                    case TRIDENT: {
                        poseStack.translate((float)invert * -0.5f, 0.7f, 0.1f);
                        poseStack.rotateDegrees(Axis.XP, -55.0f);
                        poseStack.rotateDegrees(Axis.YP, (float)invert * 35.3f);
                        poseStack.rotateDegrees(Axis.ZP, (float)invert * -9.785f);
                        float timeHeld = (float)useDuration - ((float)state.useItemRemainingTicks - partialTicks + 1.0f);
                        float power = timeHeld / 10.0f;
                        if (power > 1.0f) {
                            power = 1.0f;
                        }
                        if (power > 0.1f) {
                            float shakeOffset = Mth.sin((timeHeld - 0.1f) * 1.3f);
                            float shakeIntensity = power - 0.1f;
                            float shake = shakeOffset * shakeIntensity;
                            poseStack.translate(shake * 0.0f, shake * 0.004f, shake * 0.0f);
                        }
                        poseStack.translate(0.0f, 0.0f, power * 0.2f);
                        poseStack.scale(1.0f, 1.0f, 1.0f + power * 0.2f);
                        poseStack.rotateDegrees(Axis.YN, (float)invert * 45.0f);
                        break;
                    }
                    case BRUSH: {
                        this.applyBrushTransform(poseStack, partialTicks, arm, state.useItemRemainingTicks);
                        break;
                    }
                    case BUNDLE: {
                        this.swingArm(attack, poseStack, invert, arm);
                        break;
                    }
                    case SPEAR: {
                        poseStack.translate((float)invert * 0.56f, -0.52f, -0.72f);
                        float timeHeld = (float)useDuration - ((float)state.useItemRemainingTicks - partialTicks + 1.0f);
                        SpearAnimations.firstPersonUse(avatarRenderState.ticksSinceKineticHitFeedback, poseStack, timeHeld, arm, itemStack);
                    }
                }
            } else if (avatarRenderState.isAutoSpinAttack) {
                this.applyItemArmTransform(poseStack, arm, inverseArmHeight);
                poseStack.translate((float)invert * -0.4f, 0.8f, 0.3f);
                poseStack.rotateDegrees(Axis.YP, (float)invert * 65.0f);
                poseStack.rotateDegrees(Axis.ZP, (float)invert * -85.0f);
            } else {
                this.applyItemArmTransform(poseStack, arm, inverseArmHeight);
                LivingEntity.SwingDescription currentSwing = avatarRenderState.currentSwing;
                if (currentSwing != null && hand == currentSwing.hand()) {
                    switch (currentSwing.animation().type()) {
                        case NONE: {
                            break;
                        }
                        case WHACK: {
                            this.swingArm(attack, poseStack, invert, arm);
                            break;
                        }
                        case STAB: {
                            SpearAnimations.firstPersonAttack(attack, poseStack, invert, arm);
                        }
                    }
                }
            }
            (isMainHand ? state.mainHandRenderState : state.offHandRenderState).submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }
        poseStack.popPose();
    }

    private void swingArm(float animation, PoseStack poseStack, int invert, HumanoidArm arm) {
        float xSwingPosition = -0.4f * Mth.sin(Mth.sqrt(animation) * (float)Math.PI);
        float ySwingPosition = 0.2f * Mth.sin(Mth.sqrt(animation) * ((float)Math.PI * 2));
        float zSwingPosition = -0.2f * Mth.sin(animation * (float)Math.PI);
        poseStack.translate((float)invert * xSwingPosition, ySwingPosition, zSwingPosition);
        this.applyItemArmAttackTransform(poseStack, arm, animation);
    }
}

