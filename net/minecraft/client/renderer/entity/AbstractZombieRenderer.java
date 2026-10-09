/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.Zombie;

public abstract class AbstractZombieRenderer<T extends Zombie, S extends ZombieRenderState, M extends HumanoidModel<S>>
extends HumanoidMobRenderer<T, S, M> {
    private static final Identifier ZOMBIE_LOCATION = Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");
    private static final Identifier BABY_ZOMBIE_LOCATION = Identifier.withDefaultNamespace("textures/entity/zombie/zombie_baby.png");

    protected AbstractZombieRenderer(EntityRendererProvider.Context context, M model, M babyModel, ArmorModelSet<M> armorSet, ArmorModelSet<M> babyArmorSet) {
        super(context, model, babyModel, 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, armorSet, babyArmorSet, context.getEquipmentRenderer()));
    }

    @Override
    public Identifier getTextureLocation(S state) {
        return ((ZombieRenderState)state).isBaby ? BABY_ZOMBIE_LOCATION : ZOMBIE_LOCATION;
    }

    @Override
    public void extractRenderState(T entity, S state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ((ZombieRenderState)state).isAggressive = ((Mob)entity).isAggressive();
        ((ZombieRenderState)state).isConverting = ((Zombie)entity).isUnderWaterConverting();
    }

    @Override
    protected boolean isShaking(S state) {
        return super.isShaking(state) || ((ZombieRenderState)state).isConverting;
    }
}

