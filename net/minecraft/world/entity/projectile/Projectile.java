/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoveSimulationType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class Projectile
extends Entity
implements TraceableEntity {
    private static final boolean DEFAULT_LEFT_OWNER = false;
    private static final boolean DEFAULT_HAS_BEEN_SHOT = false;
    private static final String TAG_OWNER = "Owner";
    private static final String TAG_LEFT_OWNER = "LeftOwner";
    private static final String TAG_HAS_BEEN_SHOT = "HasBeenShot";
    private static final String TAG_CAN_BREAK = "can_break";
    protected @Nullable EntityReference<Entity> owner;
    private @Nullable AdventureModePredicate canBreak;
    private boolean leftOwner = false;
    private boolean leftOwnerChecked;
    private boolean hasBeenShot = false;
    private @Nullable Entity lastDeflectedBy;

    protected Projectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        this.applyImplicitComponentIfPresent(components, DataComponents.CAN_BREAK);
        super.applyImplicitComponents(components);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> type, T value) {
        if (type == DataComponents.CAN_BREAK) {
            this.canBreak = Projectile.castComponentValue(DataComponents.CAN_BREAK, value);
            return true;
        }
        return super.applyImplicitComponent(type, value);
    }

    protected void setOwner(@Nullable EntityReference<Entity> owner) {
        this.owner = owner;
    }

    public void setOwner(@Nullable Entity owner) {
        this.setOwner(EntityReference.of(owner));
    }

    @Override
    public @Nullable Entity getOwner() {
        return EntityReference.getEntity(this.owner, this.level());
    }

    public Entity getEffectSource() {
        return (Entity)MoreObjects.firstNonNull((Object)this.getOwner(), (Object)this);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        EntityReference.store(this.owner, output, TAG_OWNER);
        if (this.leftOwner) {
            output.putBoolean(TAG_LEFT_OWNER, true);
        }
        output.putBoolean(TAG_HAS_BEEN_SHOT, this.hasBeenShot);
        if (this.canBreak != null) {
            output.store(TAG_CAN_BREAK, AdventureModePredicate.CODEC, this.canBreak);
        }
    }

    protected boolean ownedBy(Entity entity) {
        return this.owner != null && this.owner.matches(entity);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.setOwner(EntityReference.read(input, TAG_OWNER));
        this.leftOwner = input.getBooleanOr(TAG_LEFT_OWNER, false);
        this.hasBeenShot = input.getBooleanOr(TAG_HAS_BEEN_SHOT, false);
        this.canBreak = input.read(TAG_CAN_BREAK, AdventureModePredicate.CODEC).orElse(null);
    }

    @Override
    public void restoreFrom(Entity oldEntity) {
        super.restoreFrom(oldEntity);
        if (oldEntity instanceof Projectile) {
            Projectile projectile = (Projectile)oldEntity;
            this.owner = projectile.owner;
        }
    }

    @Override
    public void tick() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }
        this.checkLeftOwner();
        super.tick();
        this.leftOwnerChecked = false;
    }

    protected void checkLeftOwner() {
        if (!this.leftOwner && !this.leftOwnerChecked) {
            this.leftOwner = this.isOutsideOwnerCollisionRange();
            this.leftOwnerChecked = true;
        }
    }

    private boolean isOutsideOwnerCollisionRange() {
        Entity owner = this.getOwner();
        if (owner != null) {
            AABB aabb = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
            return owner.getRootVehicle().getSelfAndPassengers().filter(EntitySelector.CAN_BE_PICKED).noneMatch(entity -> aabb.intersects(entity.getBoundingBox()));
        }
        return true;
    }

    public Vec3 getMovementToShoot(double xd, double yd, double zd, float pow, float uncertainty) {
        return new Vec3(xd, yd, zd).normalize().add(this.random.triangle(0.0, 0.0172275 * (double)uncertainty), this.random.triangle(0.0, 0.0172275 * (double)uncertainty), this.random.triangle(0.0, 0.0172275 * (double)uncertainty)).scale(pow);
    }

    public void shoot(double xd, double yd, double zd, float pow, float uncertainty) {
        Vec3 movement = this.getMovementToShoot(xd, yd, zd, pow, uncertainty);
        this.setDeltaMovement(movement);
        this.needsSync = true;
        double sd = movement.horizontalDistance();
        this.setYRot((float)(Mth.atan2(movement.x, movement.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2(movement.y, sd) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void shootFromRotation(Entity source, float xRot, float yRot, float yOffset, float pow, float uncertainty) {
        float xd = -Mth.sin(yRot * ((float)Math.PI / 180)) * Mth.cos(xRot * ((float)Math.PI / 180));
        float yd = -Mth.sin((xRot + yOffset) * ((float)Math.PI / 180));
        float zd = Mth.cos(yRot * ((float)Math.PI / 180)) * Mth.cos(xRot * ((float)Math.PI / 180));
        this.shoot(xd, yd, zd, pow, uncertainty);
        Vec3 sourceMovement = source.getKnownMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(sourceMovement.x, source.onGround() ? 0.0 : sourceMovement.y, sourceMovement.z));
    }

    @Override
    public void onAboveBubbleColumn(boolean dragDown, BlockPos pos) {
        double yd = dragDown ? -0.03 : 0.1;
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, yd, 0.0));
        Projectile.sendBubbleColumnParticles(this.level(), pos);
    }

    @Override
    public void onInsideBubbleColumn(boolean dragDown) {
        double yd = dragDown ? -0.03 : 0.06;
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, yd, 0.0));
        this.resetFallDistance();
    }

    public boolean canBreakBlockInAdventureMode(ServerLevel level, BlockInWorld blockInWorld) {
        Player player;
        Entity owner = this.getOwner();
        GameType worldGameMode = level.getServer().getDefaultGameType();
        if (owner == null && worldGameMode != GameType.ADVENTURE || owner instanceof Player && (player = (Player)owner).gameMode() != GameType.ADVENTURE) {
            return true;
        }
        return this.canBreak != null && this.canBreak.test(blockInWorld);
    }

    public static <T extends Projectile> T spawnProjectileFromRotation(ProjectileFactory<T> creator, ServerLevel serverLevel, ItemStack itemStack, LivingEntity source, float yOffset, float pow, float uncertainty) {
        return (T)Projectile.spawnProjectile(creator.create(serverLevel, source, itemStack), serverLevel, itemStack, projectile -> projectile.shootFromRotation(source, source.getXRot(), source.getYRot(), yOffset, pow, uncertainty));
    }

    public static <T extends Projectile> T spawnProjectileUsingShoot(ProjectileFactory<T> creator, ServerLevel serverLevel, ItemStack itemStack, LivingEntity source, double targetX, double targetY, double targetZ, float pow, float uncertainty) {
        return (T)Projectile.spawnProjectile(creator.create(serverLevel, source, itemStack), serverLevel, itemStack, projectile -> projectile.shoot(targetX, targetY, targetZ, pow, uncertainty));
    }

    public static <T extends Projectile> T spawnProjectileUsingShoot(T projectile, ServerLevel serverLevel, ItemStack itemStack, double targetX, double targetY, double targetZ, float pow, float uncertainty) {
        return (T)Projectile.spawnProjectile(projectile, serverLevel, itemStack, i -> projectile.shoot(targetX, targetY, targetZ, pow, uncertainty));
    }

    public static <T extends Projectile> T spawnProjectile(T projectile, ServerLevel serverLevel, ItemStack itemStack) {
        return (T)Projectile.spawnProjectile(projectile, serverLevel, itemStack, ignored -> {});
    }

    public static <T extends Projectile> T spawnProjectile(T projectile, ServerLevel serverLevel, ItemStack itemStack, Consumer<T> shootFunction) {
        shootFunction.accept(projectile);
        serverLevel.addFreshEntity(projectile);
        projectile.applyOnProjectileSpawned(serverLevel, itemStack);
        return projectile;
    }

    public void applyOnProjectileSpawned(ServerLevel serverLevel, ItemStack pickupItemStack) {
        AbstractArrow arrow;
        ItemStack weapon;
        EnchantmentHelper.onProjectileSpawned(serverLevel, pickupItemStack, this, item -> {});
        Projectile projectile = this;
        if (projectile instanceof AbstractArrow && (weapon = (arrow = (AbstractArrow)projectile).getWeaponItem()) != null && !weapon.isEmpty() && !pickupItemStack.getItem().equals(weapon.getItem())) {
            EnchantmentHelper.onProjectileSpawned(serverLevel, weapon, this, arrow::onItemBreak);
        }
    }

    protected ProjectileDeflection hitTargetOrDeflectSelf(HitResult hitResult) {
        if (hitResult instanceof EntityHitResult) {
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            Entity entity = entityHitResult.getEntity();
            ProjectileDeflection deflection = entity.deflection(this);
            if (deflection != ProjectileDeflection.NONE) {
                if (entity != this.lastDeflectedBy && this.deflect(deflection, entity, this.owner, false, 1.0)) {
                    this.lastDeflectedBy = entity;
                }
                return deflection;
            }
        } else if (hitResult instanceof BlockHitResult) {
            ProjectileDeflection deflection;
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            if (!blockHitResult.isWorldBorderHit()) {
                BlockState collidedWith = this.level().getBlockState(blockHitResult.getBlockPos());
                if (collidedWith.isAir()) {
                    return ProjectileDeflection.NONE;
                }
            } else if (this.shouldBounceOnWorldBorder() && this.deflect(deflection = ProjectileDeflection.REVERSE, null, this.owner, false, 0.2)) {
                return deflection;
            }
        }
        this.onHit(hitResult);
        return ProjectileDeflection.NONE;
    }

    protected boolean shouldBounceOnWorldBorder() {
        return false;
    }

    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflectingEntity, @Nullable EntityReference<Entity> newOwner, boolean byAttack, Vec3 power) {
        if (!this.level().isClientSide()) {
            deflection.deflect(this, deflectingEntity, this.random, power);
            this.setOwner(newOwner);
            this.onDeflection(byAttack);
        }
        return true;
    }

    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflectingEntity, @Nullable EntityReference<Entity> newOwner, boolean byAttack, double power) {
        return this.deflect(deflection, deflectingEntity, newOwner, byAttack, new Vec3(power, power, power));
    }

    protected void onDeflection(boolean byAttack) {
    }

    protected void onItemBreak(ItemStack item) {
    }

    protected void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            Entity entityHit = entityHitResult.getEntity();
            if (entityHit.is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entityHit instanceof Projectile) {
                Projectile projectile = (Projectile)entityHit;
                this.onRedirectProjectile(projectile);
            }
            this.onHitEntity(entityHitResult);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, null));
        } else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult)hitResult;
            this.onHitBlock(blockHit);
            BlockPos target = blockHit.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, target, GameEvent.Context.of(this, this.level().getBlockState(target)));
        }
    }

    protected void onHitEntity(EntityHitResult hitResult) {
    }

    protected void onHitBlock(BlockHitResult hitResult) {
        BlockState state = this.level().getBlockState(hitResult.getBlockPos());
        state.onProjectileHit(this.level(), state, hitResult, this);
    }

    protected void onRedirectProjectile(Projectile hitProjectile) {
        hitProjectile.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.owner, true, 1.0);
    }

    protected boolean canHitEntity(Entity entity) {
        if (!entity.canBeHitByProjectile()) {
            return false;
        }
        Entity owner = this.getOwner();
        return owner == null || this.leftOwner || !owner.isPassengerOfSameVehicle(entity);
    }

    protected void updateRotation() {
        Vec3 movement = this.getDeltaMovement();
        double sd = movement.horizontalDistance();
        this.setXRot(Projectile.lerpRotation(this.xRotO, (float)(Mth.atan2(movement.y, sd) * 57.2957763671875)));
        this.setYRot(Projectile.lerpRotation(this.yRotO, (float)(Mth.atan2(movement.x, movement.z) * 57.2957763671875)));
    }

    protected static float lerpRotation(float rotO, float rot) {
        while (rot - rotO < -180.0f) {
            rotO -= 360.0f;
        }
        while (rot - rotO >= 180.0f) {
            rotO += 360.0f;
        }
        return Mth.lerp(0.2f, rotO, rot);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        Entity owner = this.getOwner();
        return new ClientboundAddEntityPacket((Entity)this, serverEntity, owner == null ? 0 : owner.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        Entity owner = this.level().getEntity(packet.getData());
        if (owner != null) {
            this.setOwner(owner);
        }
    }

    @Override
    public boolean mayInteract(ServerLevel level, BlockPos pos) {
        Entity owner = this.getOwner();
        if (owner instanceof Player) {
            return owner.mayInteract(level, pos);
        }
        return owner == null || level.getGameRules().get(GameRules.MOB_GRIEFING) != false;
    }

    public boolean mayBreak(ServerLevel level, BlockPos pos) {
        return this.is(EntityTypeTags.IMPACT_PROJECTILES) && level.getGameRules().get(GameRules.PROJECTILES_CAN_BREAK_BLOCKS) != false && this.canBreakBlockInAdventureMode(level, new BlockInWorld(level, pos, false));
    }

    @Override
    public boolean isPickable() {
        return this.is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    @Override
    public float getPickRadius() {
        return this.isPickable() ? 1.0f : 0.0f;
    }

    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity hurtEntity, DamageSource damageSource) {
        double dx = this.getDeltaMovement().x;
        double dz = this.getDeltaMovement().z;
        return DoubleDoubleImmutablePair.of((double)dx, (double)dz);
    }

    @Override
    public int getDimensionChangingDelay() {
        return 2;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!this.isInvulnerableToBase(source)) {
            this.markHurt();
        }
        return false;
    }

    @Override
    public MoveSimulationType getMoveSimulationType() {
        return MoveSimulationType.SERVER_AND_CLIENT;
    }

    @FunctionalInterface
    public static interface ProjectileFactory<T extends Projectile> {
        public T create(ServerLevel var1, LivingEntity var2, ItemStack var3);
    }
}

