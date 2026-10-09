/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ProjectileDeflection {
    public static final ProjectileDeflection NONE = (projectile, entity, randomSource, vec3) -> {};
    public static final ProjectileDeflection REVERSE = (projectile, entity, random, power) -> {
        float rotation = 170.0f + random.nextFloat() * 20.0f;
        projectile.setDeltaMovement(projectile.getDeltaMovement().multiply(-power.x * 0.5, -power.y * 0.5, -power.z * 0.5));
        projectile.setYRot(projectile.getYRot() + rotation);
        projectile.yRotO += rotation;
        projectile.needsSync = true;
    };
    public static final ProjectileDeflection AIM_DEFLECT = (projectile, entity, randomSource, power) -> {
        if (entity != null) {
            Vec3 lookAngle = entity.getLookAngle();
            projectile.setDeltaMovement(lookAngle.multiply(power));
            projectile.needsSync = true;
        }
    };
    public static final ProjectileDeflection MOMENTUM_DEFLECT = (projectile, entity, randomSource, power) -> {
        if (entity != null) {
            Vec3 movement = entity.getKnownSpeed().normalize();
            projectile.setDeltaMovement(movement.multiply(power));
            projectile.needsSync = true;
        }
    };

    public void deflect(Projectile var1, @Nullable Entity var2, RandomSource var3, Vec3 var4);
}

