/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.joml.Quaternionf
 *  org.joml.Vector3d
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public record ApplyEntityImpulse(Vec3 direction, Vec3 coordinateScale, LevelBasedValue magnitude) implements EnchantmentEntityEffect
{
    public static final MapCodec<ApplyEntityImpulse> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Vec3.CODEC.fieldOf("direction").forGetter(ApplyEntityImpulse::direction), (App)Vec3.CODEC.fieldOf("coordinate_scale").forGetter(ApplyEntityImpulse::coordinateScale), (App)LevelBasedValue.CODEC.fieldOf("magnitude").forGetter(ApplyEntityImpulse::magnitude)).apply((Applicative)i, ApplyEntityImpulse::new));
    private static final int POST_IMPULSE_CONTEXT_RESET_GRACE_TIME_TICKS = 10;

    @Override
    public void apply(ServerLevel serverLevel, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position) {
        Quaternionf look = entity.getLookQuaternion();
        Vector3d direction = look.transform(this.direction.x, this.direction.y, this.direction.z, new Vector3d()).mul(this.coordinateScale.x, this.coordinateScale.y, this.coordinateScale.z).mul((double)this.magnitude.calculate(enchantmentLevel));
        entity.addDeltaMovement(new Vec3(direction.x, direction.y, direction.z));
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            player.connection.send(new ClientboundSetEntityMotionPacket(entity));
            entity.syncVelocity = false;
        } else {
            entity.syncVelocity = true;
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.applyPostImpulseGraceTime(10);
        }
    }

    public MapCodec<ApplyEntityImpulse> codec() {
        return CODEC;
    }
}

