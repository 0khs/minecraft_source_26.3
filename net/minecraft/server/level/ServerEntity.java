/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket;
import net.minecraft.network.protocol.game.ClientboundProjectilePowerPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.MovementPacket;
import net.minecraft.network.protocol.game.VecDelta;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.InterpolationTracker;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.UpdateInterval;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TOLERANCE_LEVEL_ROTATION = 1;
    private static final double TOLERANCE_LEVEL_POSITION = 7.62939453125E-6;
    public static final int FORCED_POS_UPDATE_PERIOD = 60;
    private static final int FORCED_TELEPORT_PERIOD = 400;
    private final ServerLevel level;
    private final Entity entity;
    private final UpdateInterval updateInterval;
    private final boolean trackDelta;
    private final Synchronizer synchronizer;
    private final InterpolationTracker interpolationTracker;
    private final VecDeltaCodec positionCodec = new VecDeltaCodec();
    private byte lastSentYRot;
    private byte lastSentXRot;
    private byte lastSentYHeadRot;
    private Vec3 lastSentMovement;
    private int tickCount;
    private int teleportDelay;
    private List<Entity> lastPassengers = Collections.emptyList();
    private boolean wasRiding;
    private boolean wasOnGround;
    private @Nullable List<SynchedEntityData.DataValue<?>> trackedDataValues;

    public ServerEntity(ServerLevel level, Entity entity, UpdateInterval updateInterval, boolean trackDelta, Synchronizer synchronizer) {
        this.level = level;
        this.synchronizer = synchronizer;
        this.entity = entity;
        this.updateInterval = updateInterval;
        this.trackDelta = trackDelta;
        this.interpolationTracker = entity.getInterpolation().interpolationTracker();
        this.positionCodec.setBase(entity.trackingPosition());
        this.lastSentMovement = entity.getDeltaMovement();
        this.lastSentYRot = Mth.packDegrees(entity.getYRot());
        this.lastSentXRot = Mth.packDegrees(entity.getXRot());
        this.lastSentYHeadRot = Mth.packDegrees(entity.getYHeadRot());
        this.wasOnGround = entity.onGround();
        this.trackedDataValues = entity.getEntityData().getNonDefaultValues();
    }

    public void sendChanges() {
        Object packet;
        Entity entity;
        this.entity.updateDataBeforeSync();
        List<Entity> passengers = this.entity.getPassengers();
        if (!passengers.equals(this.lastPassengers)) {
            this.synchronizer.sendToTrackingPlayers(new ClientboundSetPassengersPacket(this.entity));
            this.lastPassengers = passengers;
        }
        if ((entity = this.entity) instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame)entity;
            if (this.tickCount % 10 == 0) {
                MapId id;
                MapItemSavedData data;
                ItemStack itemStack = frame.getItem();
                if (itemStack.getItem() instanceof MapItem && (data = MapItem.getSavedData(id = itemStack.get(DataComponents.MAP_ID), (Level)this.level)) != null) {
                    for (ServerPlayer player : this.level.players()) {
                        data.tickCarriedBy(player, itemStack, frame);
                        packet = data.getUpdatePacket(id, player);
                        if (packet == null) continue;
                        player.connection.send((Packet<?>)packet);
                    }
                }
                this.sendDirtyEntityData();
            }
        }
        Vec3 currentPosition = this.entity.trackingPosition();
        this.interpolationTracker.updateTracking(currentPosition);
        if (this.entity.syncPosition) {
            this.tickCount = this.updateInterval.nextInterval(this.tickCount);
            this.entity.syncPosition = false;
        }
        if (this.entity.needsSync || this.updateInterval.test(this.tickCount) || this.entity.getEntityData().isDirty()) {
            boolean shouldSendRotation;
            byte yRotn = Mth.packDegrees(this.entity.getYRot());
            byte xRotn = Mth.packDegrees(this.entity.getXRot());
            boolean bl = shouldSendRotation = Math.abs(yRotn - this.lastSentYRot) >= 1 || Math.abs(xRotn - this.lastSentXRot) >= 1;
            if (this.entity.isPassenger()) {
                if (shouldSendRotation) {
                    this.synchronizer.sendToTrackingPlayers(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), yRotn, xRotn, this.entity.onGround()));
                    this.lastSentYRot = yRotn;
                    this.lastSentXRot = xRotn;
                }
                this.interpolationTracker.clear();
                this.positionCodec.setBase(currentPosition);
                this.sendDirtyEntityData();
                this.wasRiding = true;
            } else {
                AbstractMinecart minecart;
                packet = this.entity;
                if (packet instanceof AbstractMinecart && (packet = (minecart = (AbstractMinecart)packet).getBehavior()) instanceof NewMinecartBehavior) {
                    NewMinecartBehavior newMinecartBehavior = (NewMinecartBehavior)packet;
                    this.handleMinecartPosRot(newMinecartBehavior, yRotn, xRotn, shouldSendRotation);
                } else {
                    Vec3 movement;
                    double diff;
                    LivingEntity livingEntity;
                    Entity shouldSendPosition2;
                    MovementPacket<ClientGamePacketListener> packet2;
                    ++this.teleportDelay;
                    PositionPath position = this.interpolationTracker.getPositionPath(currentPosition);
                    this.interpolationTracker.clear();
                    if (this.entity instanceof AbstractArrow) {
                        packet2 = this.createMovePacket(position, yRotn, xRotn, true, true);
                    } else {
                        boolean positionChanged = this.positionCodec.delta(currentPosition).lengthSqr() >= 7.62939453125E-6;
                        boolean shouldSendPosition2 = positionChanged || this.tickCount % 60 == 0;
                        packet2 = this.createMovePacket(position, yRotn, xRotn, shouldSendPosition2, shouldSendRotation);
                    }
                    if ((this.entity.needsSync || this.trackDelta || (shouldSendPosition2 = this.entity) instanceof LivingEntity && (livingEntity = (LivingEntity)shouldSendPosition2).isFallFlying()) && ((diff = (movement = this.entity.getDeltaMovement()).distanceToSqr(this.lastSentMovement)) > 1.0E-7 || diff > 0.0 && movement.lengthSqr() == 0.0)) {
                        this.lastSentMovement = movement;
                        Entity entity2 = this.entity;
                        if (entity2 instanceof AbstractHurtingProjectile) {
                            AbstractHurtingProjectile projectile = (AbstractHurtingProjectile)entity2;
                            this.synchronizer.sendToTrackingPlayers(new ClientboundBundlePacket((Iterable<Packet<? super ClientGamePacketListener>>)List.of(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement), new ClientboundProjectilePowerPacket(projectile.getId(), projectile.accelerationPower))));
                        } else {
                            this.synchronizer.sendToTrackingPlayers(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement));
                        }
                    }
                    if (packet2 != null) {
                        this.synchronizer.sendToTrackingPlayers(packet2);
                        this.sendDirtyEntityData();
                        if (packet2.hasPosition()) {
                            this.positionCodec.setBase(currentPosition);
                        }
                        if (packet2.hasRotation()) {
                            this.lastSentYRot = yRotn;
                            this.lastSentXRot = xRotn;
                        }
                    } else {
                        this.sendDirtyEntityData();
                    }
                    this.wasRiding = false;
                }
            }
            byte yHeadRot = Mth.packDegrees(this.entity.getYHeadRot());
            if (Math.abs(yHeadRot - this.lastSentYHeadRot) >= 1) {
                this.synchronizer.sendToTrackingPlayers(new ClientboundRotateHeadPacket(this.entity, yHeadRot));
                this.lastSentYHeadRot = yHeadRot;
            }
            this.entity.needsSync = false;
        }
        ++this.tickCount;
        if (this.entity.syncVelocity) {
            this.entity.syncVelocity = false;
            this.synchronizer.sendToTrackingPlayersAndSelf(new ClientboundSetEntityMotionPacket(this.entity));
        }
    }

    private @Nullable MovementPacket<ClientGamePacketListener> createMovePacket(PositionPath position, byte yRotn, byte xRotn, boolean shouldSendPosition, boolean shouldSendRotation) {
        if (this.entity.getRequiresPrecisePosition() || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.onGround()) {
            this.wasOnGround = this.entity.onGround();
            this.teleportDelay = 0;
            return ClientboundEntityPositionSyncPacket.of(this.entity, position);
        }
        if (shouldSendPosition) {
            VecDelta encodedDelta = this.positionCodec.tryEncode(position);
            if (encodedDelta == null || this.isFullPrecisionEncodingRequired(position.endPosition(), encodedDelta)) {
                return ClientboundEntityPositionSyncPacket.of(this.entity, position);
            }
            if (shouldSendRotation) {
                return new ClientboundMoveEntityPacket.PosRot(this.entity.getId(), encodedDelta, yRotn, xRotn, this.entity.onGround());
            }
            return new ClientboundMoveEntityPacket.Pos(this.entity.getId(), encodedDelta, this.entity.onGround());
        }
        if (shouldSendRotation) {
            return new ClientboundMoveEntityPacket.Rot(this.entity.getId(), yRotn, xRotn, this.entity.onGround());
        }
        return null;
    }

    private boolean isFullPrecisionEncodingRequired(Vec3 pos, VecDelta delta) {
        if (this.entity instanceof ItemEntity) {
            if (this.entity.verticalCollision && (delta.hasDeltaX() && VecDeltaCodec.encodingPrecisionLoss(pos.x) != 0.0 || delta.hasDeltaZ() && VecDeltaCodec.encodingPrecisionLoss(pos.z) != 0.0)) {
                return true;
            }
            if (this.entity.horizontalCollision && (delta.hasDeltaX() && VecDeltaCodec.encodingPrecisionLoss(pos.z) != 0.0 || delta.hasDeltaZ() && VecDeltaCodec.encodingPrecisionLoss(pos.x) != 0.0)) {
                return true;
            }
        }
        return false;
    }

    private void handleMinecartPosRot(NewMinecartBehavior newMinecartBehavior, byte yRotn, byte xRotn, boolean shouldSendRotation) {
        this.sendDirtyEntityData();
        if (newMinecartBehavior.lerpSteps.isEmpty()) {
            boolean shouldSendPosition;
            Vec3 movement = this.entity.getDeltaMovement();
            double diff = movement.distanceToSqr(this.lastSentMovement);
            Vec3 currentPosition = this.entity.trackingPosition();
            boolean positionChanged = this.positionCodec.delta(currentPosition).lengthSqr() >= 7.62939453125E-6;
            boolean bl = shouldSendPosition = positionChanged || this.tickCount % 60 == 0;
            if (shouldSendPosition || shouldSendRotation || diff > 1.0E-7) {
                this.synchronizer.sendToTrackingPlayers(new ClientboundMoveMinecartPacket(this.entity.getId(), List.of(new NewMinecartBehavior.MinecartStep(this.entity.position(), this.entity.getDeltaMovement(), this.entity.getYRot(), this.entity.getXRot(), 1.0f))));
            }
        } else {
            this.synchronizer.sendToTrackingPlayers(new ClientboundMoveMinecartPacket(this.entity.getId(), List.copyOf(newMinecartBehavior.lerpSteps)));
            newMinecartBehavior.lerpSteps.clear();
        }
        this.lastSentYRot = yRotn;
        this.lastSentXRot = xRotn;
        this.positionCodec.setBase(this.entity.position());
    }

    public void removePairing(ServerPlayer player) {
        this.entity.stopSeenByPlayer(player);
        player.connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
    }

    public void addPairing(ServerPlayer player) {
        ArrayList<Packet<? super ClientGamePacketListener>> packets = new ArrayList<Packet<? super ClientGamePacketListener>>();
        this.sendPairingData(player, packets::add);
        player.connection.send(new ClientboundBundlePacket((Iterable<Packet<? super ClientGamePacketListener>>)packets));
        this.entity.startSeenByPlayer(player);
    }

    public void sendPairingData(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> broadcast) {
        Leashable leashable;
        LivingEntity livingEntity;
        Object attributes;
        Entity entity;
        this.entity.updateDataBeforeSync();
        if (this.entity.isRemoved()) {
            LOGGER.warn("Fetching packet for removed entity {}", (Object)this.entity);
        }
        Packet<ClientGamePacketListener> packet = this.entity.getAddEntityPacket(this);
        broadcast.accept(packet);
        if (this.trackedDataValues != null) {
            broadcast.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.trackedDataValues));
        }
        if ((entity = this.entity) instanceof LivingEntity && !(attributes = (livingEntity = (LivingEntity)entity).getAttributes().getSyncableAttributes()).isEmpty()) {
            broadcast.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), (Collection<AttributeInstance>)attributes));
        }
        if ((attributes = this.entity) instanceof LivingEntity) {
            livingEntity = (LivingEntity)attributes;
            ArrayList slots = Lists.newArrayList();
            for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                ItemStack itemStack = livingEntity.getItemBySlot(slot);
                if (itemStack.isEmpty()) continue;
                slots.add(Pair.of((Object)slot, (Object)itemStack.copy()));
            }
            if (!slots.isEmpty()) {
                broadcast.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), slots));
            }
        }
        if (!this.entity.getPassengers().isEmpty()) {
            broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
        }
        if (this.entity.isPassenger()) {
            broadcast.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
        }
        if ((entity = this.entity) instanceof Leashable && (leashable = (Leashable)((Object)entity)).isLeashed()) {
            broadcast.accept(new ClientboundSetEntityLinkPacket(this.entity, leashable.getLeashHolder()));
        }
    }

    public Vec3 getPositionBase() {
        return this.positionCodec.getBase();
    }

    public Vec3 getLastSentMovement() {
        return this.lastSentMovement;
    }

    public float getLastSentXRot() {
        return Mth.unpackDegrees(this.lastSentXRot);
    }

    public float getLastSentYRot() {
        return Mth.unpackDegrees(this.lastSentYRot);
    }

    public float getLastSentYHeadRot() {
        return Mth.unpackDegrees(this.lastSentYHeadRot);
    }

    private void sendDirtyEntityData() {
        Entity entity;
        SynchedEntityData entityData = this.entity.getEntityData();
        List<SynchedEntityData.DataValue<?>> packedValues = entityData.packDirty();
        if (packedValues != null) {
            this.trackedDataValues = entityData.getNonDefaultValues();
            this.synchronizer.sendToTrackingPlayersAndSelf(new ClientboundSetEntityDataPacket(this.entity.getId(), packedValues));
        }
        if ((entity = this.entity) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            Set<AttributeInstance> attributes = livingEntity.getAttributes().getAttributesToSync();
            if (!attributes.isEmpty()) {
                this.synchronizer.sendToTrackingPlayersAndSelf(new ClientboundUpdateAttributesPacket(this.entity.getId(), attributes));
            }
            attributes.clear();
        }
    }

    public static interface Synchronizer {
        public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> var1);

        public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> var1);

        public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> var1, Predicate<ServerPlayer> var2);
    }
}

