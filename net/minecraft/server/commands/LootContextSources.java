/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootContextSources {
    private static ArgumentBuilder<CommandSourceStack, ?> decorate(ArgumentBuilder<CommandSourceStack, ?> node, NodeVisitor nodeVisitor, ContextDecorator decorator) {
        nodeVisitor.visit(decorator, arg_0 -> node.then(arg_0));
        return node;
    }

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> T addContextSources(T node, NodeVisitor factory) {
        return (T)node.then(LootContextSources.decorate(Commands.literal("default"), factory, (commandContext, params) -> params.create(LootContextParamSets.COMMAND_COMPUTE_DEFAULT))).then(Commands.literal("block").then(LootContextSources.decorate(Commands.argument("computePos", BlockPosArgument.blockPos()), factory, (context, params) -> {
            BlockPos pos = BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)context, "computePos");
            CommandSourceStack source = (CommandSourceStack)context.getSource();
            ServerLevel level = source.getLevel();
            BlockState blockState = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            return params.withParameter(LootContextParams.BLOCK_STATE, blockState).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).create(LootContextParamSets.COMMAND_COMPUTE_POSITION);
        }))).then(Commands.literal("entity").then(LootContextSources.decorate(Commands.argument("computeTarget", EntityArgument.entity()), factory, (context, params) -> {
            Entity target = EntityArgument.getEntity((CommandContext<CommandSourceStack>)context, "computeTarget");
            return params.withParameter(LootContextParams.TARGET_ENTITY, target).create(LootContextParamSets.COMMAND_COMPUTE_ENTITY);
        })));
    }

    @FunctionalInterface
    public static interface NodeVisitor {
        public void visit(ContextDecorator var1, Consumer<ArgumentBuilder<CommandSourceStack, ?>> var2);
    }

    @FunctionalInterface
    public static interface ContextDecorator {
        public LootParams customize(CommandContext<CommandSourceStack> var1, LootParams.Builder var2) throws CommandSyntaxException;

        default public LootParams createParams(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            CommandSourceStack source = (CommandSourceStack)context.getSource();
            ServerLevel level = source.getLevel();
            LootParams.Builder commonContext = new LootParams.Builder(level).withOptionalParameter(LootContextParams.THIS_ENTITY, source.getEntity()).withParameter(LootContextParams.ORIGIN, source.getPosition());
            return this.customize(context, commonContext);
        }

        default public LootContext createContext(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            LootParams params = this.createParams(context);
            return new LootContext.Builder(params).create(Optional.empty());
        }
    }
}

