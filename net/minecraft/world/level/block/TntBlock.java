/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class TntBlock
extends Block {
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

    public TntBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(UNSTABLE, false));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (oldState.is(state.getBlock())) {
            return;
        }
        if (level.hasNeighborSignal(pos) && TntBlock.prime(level, pos)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.hasNeighborSignal(pos) && TntBlock.prime(level, pos)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.getAbilities().instabuild && state.getValue(UNSTABLE).booleanValue()) {
            TntBlock.prime(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
        if (!level.getGameRules().get(GameRules.TNT_EXPLODES).booleanValue()) {
            return;
        }
        PrimedTnt primed = new PrimedTnt(level, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, explosion.getIndirectSourceEntity());
        primed.setFuse(PrimedTnt.getRandomShortFuse(primed.getFuse(), level.getRandom()));
        level.addFreshEntity(primed);
    }

    public static boolean prime(Level level, BlockPos pos) {
        return TntBlock.prime(level, pos, null, ItemStack.EMPTY);
    }

    public static boolean prime(Level level, BlockPos pos, @Nullable LivingEntity source, ItemStack itemStack) {
        Player player;
        ServerLevel serverLevel;
        if (!(level instanceof ServerLevel) || !(serverLevel = (ServerLevel)level).getGameRules().get(GameRules.TNT_EXPLODES).booleanValue()) {
            return false;
        }
        if (source instanceof Player && (player = (Player)source).gameMode() == GameType.ADVENTURE && !itemStack.canBreakBlockInAdventureMode(new BlockInWorld(level, pos, false))) {
            return false;
        }
        PrimedTnt tnt = new PrimedTnt(level, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, source);
        level.addFreshEntity(tnt);
        level.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.gameEvent((Entity)source, GameEvent.PRIME_FUSE, pos);
        return true;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ServerLevel serverLevel;
        if (!itemStack.is(Items.FLINT_AND_STEEL) && !itemStack.is(Items.FIRE_CHARGE)) {
            return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
        }
        if (TntBlock.prime(level, pos, player, itemStack)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            Item item = itemStack.getItem();
            if (itemStack.is(Items.FLINT_AND_STEEL)) {
                itemStack.hurtAndBreak(1, (LivingEntity)player, hand.asEquipmentSlot());
            } else {
                itemStack.consume(1, player);
            }
            player.awardStat(Stats.ITEM_USED.get(item));
        } else if (level instanceof ServerLevel && !(serverLevel = (ServerLevel)level).getGameRules().get(GameRules.TNT_EXPLODES).booleanValue()) {
            player.sendOverlayMessage(Component.translatable("block.minecraft.tnt.disabled"));
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult blockHit, Projectile projectile) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            BlockPos pos = blockHit.getBlockPos();
            Entity owner = projectile.getOwner();
            if (projectile.isOnFire() && projectile.mayInteract(serverLevel, pos)) {
                ItemStack itemStack;
                LivingEntity livingEntity;
                LivingEntity livingEntity2 = owner instanceof LivingEntity ? (livingEntity = (LivingEntity)owner) : null;
                if (projectile instanceof AbstractArrow) {
                    AbstractArrow arr = (AbstractArrow)projectile;
                    itemStack = arr.getPickupItemStackOrigin();
                } else {
                    itemStack = ItemStack.EMPTY;
                }
                if (TntBlock.prime(level, pos, livingEntity2, itemStack)) {
                    level.removeBlock(pos, false);
                }
            }
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UNSTABLE);
    }
}

