/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import org.jspecify.annotations.Nullable;

public interface BlockStateProvider {
    public static final Codec<BlockStateProvider> TYPED_CODEC = BuiltInRegistries.BLOCK_STATE_PROVIDER_TYPE.byNameCodec().dispatch(BlockStateProvider::codec, c -> c);
    public static final Codec<Either<BlockState, BlockStateProvider>> STATE_OR_PROVIDER_CODEC = Codec.xor(BlockState.FULL_CODEC, TYPED_CODEC);
    public static final Codec<BlockStateProvider> DIRECT_CODEC = STATE_OR_PROVIDER_CODEC.xmap(e -> (BlockStateProvider)e.map(SimpleStateProvider::new, s -> s), provider -> {
        BlockState state;
        Either either;
        if (!(provider instanceof SimpleStateProvider)) {
            either = Either.right((Object)provider);
            return either;
        }
        SimpleStateProvider $b$0 = (SimpleStateProvider)provider;
        try {
            BlockState patt1$temp;
            state = patt1$temp = $b$0.state();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        either = Either.left((Object)state);
        return either;
    });
    public static final Codec<Holder<BlockStateProvider>> CODEC = RegistryCodecs.holder(Registries.BLOCK_STATE_PROVIDER, DIRECT_CODEC);

    public static SimpleStateProvider of(BlockState state) {
        return new SimpleStateProvider(state);
    }

    public static SimpleStateProvider of(Block block) {
        return new SimpleStateProvider(block.defaultBlockState());
    }

    public static Holder<BlockStateProvider> holderOf(BlockState state) {
        return Holder.direct(BlockStateProvider.of(state));
    }

    public static Holder<BlockStateProvider> holderOf(Block block) {
        return Holder.direct(BlockStateProvider.of(block));
    }

    public MapCodec<? extends BlockStateProvider> codec();

    public BlockState getState(LevelAccessor var1, RandomSource var2, BlockPos var3);

    default public @Nullable BlockState getOptionalState(LevelAccessor level, RandomSource random, BlockPos pos) {
        return this.getState(level, random, pos);
    }
}

