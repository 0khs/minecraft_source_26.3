/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.debug;

import io.netty.buffer.ByteBuf;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import org.jspecify.annotations.Nullable;

public record DebugBrainDump(String name, String profession, int xp, float health, float maxHealth, String inventory, boolean wantsGolem, int angerLevel, List<String> activities, List<String> behaviors, List<String> memories, List<String> gossips, Set<BlockPos> pois, Set<BlockPos> potentialPois) {
    public static final StreamCodec<ByteBuf, DebugBrainDump> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, DebugBrainDump::name, ByteBufCodecs.STRING_UTF8, DebugBrainDump::profession, ByteBufCodecs.INT, DebugBrainDump::xp, ByteBufCodecs.FLOAT, DebugBrainDump::health, ByteBufCodecs.FLOAT, DebugBrainDump::maxHealth, ByteBufCodecs.STRING_UTF8, DebugBrainDump::inventory, ByteBufCodecs.BOOL, DebugBrainDump::wantsGolem, ByteBufCodecs.INT, DebugBrainDump::angerLevel, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DebugBrainDump::activities, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DebugBrainDump::behaviors, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DebugBrainDump::memories, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DebugBrainDump::gossips, BlockPos.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), DebugBrainDump::pois, BlockPos.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), DebugBrainDump::potentialPois, DebugBrainDump::new);

    public static DebugBrainDump takeBrainDump(ServerLevel serverLevel, LivingEntity entity) {
        List<String> list;
        int n;
        Villager villager;
        boolean wantsGolem;
        InventoryCarrier inventoryCarrier;
        SimpleContainer inventory;
        int xp;
        String profession;
        String name = DebugEntityNameGenerator.getEntityName(entity);
        if (entity instanceof Villager) {
            Villager villager2 = (Villager)entity;
            profession = villager2.getVillagerData().profession().getRegisteredName();
            xp = villager2.getVillagerXp();
        } else {
            profession = "";
            xp = 0;
        }
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        Brain<? extends LivingEntity> brain = entity.getBrain();
        long gameTime = entity.level().getGameTime();
        String inventoryStr = entity instanceof InventoryCarrier ? ((inventory = (inventoryCarrier = (InventoryCarrier)((Object)entity)).getInventory()).isEmpty() ? "" : ((Object)inventory).toString()) : "";
        boolean bl = wantsGolem = entity instanceof Villager && (villager = (Villager)entity).wantsToSpawnGolem(gameTime);
        if (entity instanceof Warden) {
            Warden warden = (Warden)entity;
            n = warden.getClientAngerLevel();
        } else {
            n = -1;
        }
        int angerLevel = n;
        List<String> activities = brain.getActiveActivities().stream().map(Activity::getName).toList();
        List<String> behaviors = brain.getRunningBehaviors().stream().map(BehaviorControl::debugString).toList();
        List<String> memories = DebugBrainDump.getMemoryDescriptions(serverLevel, entity, gameTime);
        Set<BlockPos> pois = DebugBrainDump.getKnownBlockPositions(brain, MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT);
        Set<BlockPos> potentialPois = DebugBrainDump.getKnownBlockPositions(brain, MemoryModuleType.POTENTIAL_JOB_SITE);
        if (entity instanceof Villager) {
            Villager villager3 = (Villager)entity;
            list = DebugBrainDump.getVillagerGossips(villager3);
        } else {
            list = List.of();
        }
        List<String> gossips = list;
        return new DebugBrainDump(name, profession, xp, health, maxHealth, inventoryStr, wantsGolem, angerLevel, activities, behaviors, memories, gossips, pois, potentialPois);
    }

    @SafeVarargs
    private static Set<BlockPos> getKnownBlockPositions(Brain<?> brain, MemoryModuleType<GlobalPos> ... memories) {
        return Stream.of(memories).filter(brain::hasMemoryValue).map(brain::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
    }

    private static List<String> getVillagerGossips(Villager villager) {
        ArrayList<String> gossips = new ArrayList<String>();
        villager.getGossips().getGossipEntries().forEach((uuid, entries) -> {
            String gossipeeName = DebugEntityNameGenerator.getEntityName(uuid);
            entries.forEach((gossipType, value) -> gossips.add(gossipeeName + ": " + String.valueOf(gossipType) + ": " + value));
        });
        return gossips;
    }

    private static List<String> getMemoryDescriptions(final ServerLevel level, LivingEntity body, final long timestamp) {
        final ArrayList<String> result = new ArrayList<String>();
        body.getBrain().forEach(new Brain.Visitor(){

            @Override
            public <U> void acceptEmpty(MemoryModuleType<U> type) {
                this.collectResult(type, Optional.empty(), OptionalLong.empty());
            }

            @Override
            public <U> void accept(MemoryModuleType<U> type, U value) {
                this.collectResult(type, Optional.of(value), OptionalLong.empty());
            }

            @Override
            public <U> void accept(MemoryModuleType<U> type, U value, long timeToLive) {
                this.collectResult(type, Optional.of(value), OptionalLong.of(timestamp));
            }

            private void collectResult(MemoryModuleType<?> memoryType, Optional<?> value, OptionalLong ttl) {
                String description = DebugBrainDump.getMemoryDescription(level, timestamp, memoryType, value, ttl);
                result.add(StringUtil.truncateStringIfNecessary(description, 255, true));
            }
        });
        Collections.sort(result);
        return result;
    }

    private static String getMemoryDescription(ServerLevel level, long timestamp, MemoryModuleType<?> memoryType, Optional<?> maybeValue, OptionalLong ttl) {
        Object description;
        if (maybeValue.isPresent()) {
            Object value = maybeValue.get();
            if (memoryType == MemoryModuleType.HEARD_BELL_TIME) {
                long timeSince = timestamp - (Long)value;
                description = timeSince + " ticks ago";
            } else {
                description = ttl.isPresent() ? DebugBrainDump.getShortDescription(level, value) + " (ttl: " + ttl.getAsLong() + ")" : DebugBrainDump.getShortDescription(level, value);
            }
        } else {
            description = "-";
        }
        return BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memoryType).getPath() + ": " + (String)description;
    }

    private static String getShortDescription(ServerLevel level, @Nullable Object obj) {
        Object object = obj;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{UUID.class, Entity.class, WalkTarget.class, EntityTracker.class, GlobalPos.class, BlockPosTracker.class, DamageSource.class, NearestVisibleLivingEntities.class, Collection.class}, (Object)object, n)) {
            case -1 -> "-";
            case 0 -> {
                UUID uuid = (UUID)object;
                yield DebugBrainDump.getShortDescription(level, level.getEntity(uuid));
            }
            case 1 -> {
                Entity entity = (Entity)object;
                yield DebugEntityNameGenerator.getEntityName(entity);
            }
            case 2 -> {
                WalkTarget walkTarget = (WalkTarget)object;
                yield DebugBrainDump.getShortDescription(level, walkTarget.getTarget());
            }
            case 3 -> {
                EntityTracker entityTracker = (EntityTracker)object;
                yield DebugBrainDump.getShortDescription(level, entityTracker.getEntity());
            }
            case 4 -> {
                GlobalPos globalPos = (GlobalPos)object;
                yield DebugBrainDump.getShortDescription(level, globalPos.pos());
            }
            case 5 -> {
                BlockPosTracker tracker = (BlockPosTracker)object;
                yield DebugBrainDump.getShortDescription(level, tracker.currentBlockPosition());
            }
            case 6 -> {
                DamageSource damageSource = (DamageSource)object;
                Entity entity = damageSource.getEntity();
                if (entity == null) {
                    yield obj.toString();
                }
                yield DebugBrainDump.getShortDescription(level, entity);
            }
            case 7 -> {
                NearestVisibleLivingEntities visibleEntities = (NearestVisibleLivingEntities)object;
                yield DebugBrainDump.getShortDescription(level, visibleEntities.nearbyEntities());
            }
            case 8 -> {
                Collection collection = (Collection)object;
                yield "[" + collection.stream().map(element -> DebugBrainDump.getShortDescription(level, element)).collect(Collectors.joining(", ")) + "]";
            }
            default -> obj.toString();
        };
    }

    public boolean hasPoi(BlockPos poiPos) {
        return this.pois.contains(poiPos);
    }

    public boolean hasPotentialPoi(BlockPos poiPos) {
        return this.potentialPois.contains(poiPos);
    }
}

