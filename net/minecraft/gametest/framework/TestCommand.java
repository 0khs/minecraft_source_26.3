/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceSelectorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FailedTestTracker;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchFactory;
import net.minecraft.gametest.framework.GameTestBatchListener;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.StructureGridSpawner;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFinder;
import net.minecraft.gametest.framework.TestInstanceFinder;
import net.minecraft.gametest.framework.TestPosFinder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundGameTestHighlightPosPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.InCommandFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;

public class TestCommand {
    public static final int TEST_NEARBY_SEARCH_RADIUS = 15;
    public static final int TEST_FULL_SEARCH_RADIUS = 250;
    public static final int VERIFY_TEST_GRID_AXIS_SIZE = 10;
    public static final int VERIFY_TEST_BATCH_SIZE = 100;
    private static final int DEFAULT_CLEAR_RADIUS = 250;
    private static final int MAX_CLEAR_RADIUS = 1024;
    private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
    private static final int DEFAULT_X_SIZE = 5;
    private static final int DEFAULT_Y_SIZE = 5;
    private static final int DEFAULT_Z_SIZE = 5;
    private static final SimpleCommandExceptionType CLEAR_NO_TESTS = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.clear.error.no_tests"));
    private static final SimpleCommandExceptionType RESET_NO_TESTS = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.reset.error.no_tests"));
    private static final SimpleCommandExceptionType TEST_INSTANCE_COULD_NOT_BE_FOUND = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.error.test_instance_not_found"));
    private static final SimpleCommandExceptionType NO_STRUCTURES_TO_EXPORT = new SimpleCommandExceptionType((Message)Component.literal("Could not find any structures to export"));
    private static final SimpleCommandExceptionType NO_TEST_INSTANCES = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.error.no_test_instances"));
    private static final Dynamic3CommandExceptionType NO_TEST_CONTAINING = new Dynamic3CommandExceptionType((x, y, z) -> Component.translatableEscape("commands.test.error.no_test_containing_pos", x, y, z));
    private static final DynamicCommandExceptionType TOO_LARGE = new DynamicCommandExceptionType(size -> Component.translatableEscape("commands.test.error.too_large", size));

    private static int reset(TestFinder finder) throws CommandSyntaxException {
        TestCommand.stopTests();
        int count = TestCommand.toGameTestInfos(finder.source(), RetryOptions.noRetries(), finder).map(info -> TestCommand.resetGameTestInfo(finder.source(), info)).toList().size();
        if (count == 0) {
            throw CLEAR_NO_TESTS.create();
        }
        finder.source().sendSuccess(() -> Component.translatable("commands.test.reset.success", count), true);
        return count;
    }

    private static int clear(TestFinder finder) throws CommandSyntaxException {
        TestCommand.stopTests();
        CommandSourceStack source = finder.source();
        MinecraftServer server = source.getServer();
        List tests = finder.findTestPos().flatMap(globalPos -> server.getLevel(globalPos.dimension()).getBlockEntity(globalPos.pos(), BlockEntityTypes.TEST_INSTANCE_BLOCK).stream()).toList();
        for (TestInstanceBlockEntity testInstanceBlockEntity : tests) {
            ServerLevel level = (ServerLevel)testInstanceBlockEntity.getLevel();
            StructureUtils.clearSpaceForStructure(testInstanceBlockEntity.getTestBoundingBox(), level);
            testInstanceBlockEntity.removeBarriers();
            level.destroyBlock(testInstanceBlockEntity.getBlockPos(), false);
        }
        if (tests.isEmpty()) {
            throw CLEAR_NO_TESTS.create();
        }
        source.sendSuccess(() -> Component.translatable("commands.test.clear.success", tests.size()), true);
        return tests.size();
    }

    private static int export(TestFinder finder) throws CommandSyntaxException {
        CommandSourceStack source = finder.source();
        int count = 0;
        boolean allGood = true;
        Iterator iterator = finder.findTestPos().iterator();
        while (iterator.hasNext()) {
            GlobalPos pos = (GlobalPos)iterator.next();
            ServerLevel level = source.getServer().getLevel(pos.dimension());
            BlockEntity blockEntity = level.getBlockEntity(pos.pos());
            if (blockEntity instanceof TestInstanceBlockEntity) {
                TestInstanceBlockEntity blockEntity2 = (TestInstanceBlockEntity)blockEntity;
                if (!blockEntity2.exportTest(source::sendSystemMessage)) {
                    allGood = false;
                }
                ++count;
                continue;
            }
            throw TEST_INSTANCE_COULD_NOT_BE_FOUND.create();
        }
        if (count == 0) {
            throw NO_STRUCTURES_TO_EXPORT.create();
        }
        String message = "Exported " + count + " structures";
        finder.source().sendSuccess(() -> Component.literal(message), true);
        return allGood ? 0 : 1;
    }

    private static int verify(TestFinder finder) {
        TestCommand.stopTests();
        CommandSourceStack source = finder.source();
        Function<ResourceKey<Level>, BlockPos> testPos = dimension -> TestCommand.createTestPositionAround(source, dimension);
        List<GameTestInfo> infos = Stream.concat(TestCommand.toGameTestInfos(source, RetryOptions.noRetries(), finder), TestCommand.toGameTestInfo(source, RetryOptions.noRetries(), finder, 0)).toList();
        FailedTestTracker.forgetFailedTests();
        ArrayList<GameTestBatch> batches = new ArrayList<GameTestBatch>();
        for (GameTestInfo info : infos) {
            for (Rotation rotation : Rotation.values()) {
                ArrayList<GameTestInfo> transformedInfos = new ArrayList<GameTestInfo>();
                for (int i = 0; i < 100; ++i) {
                    GameTestInfo copyInfo = new GameTestInfo(info.getTestHolder(), rotation, info.getLevel(), new RetryOptions(1, true));
                    copyInfo.setTestBlockPos(info.getTestBlockPos());
                    transformedInfos.add(copyInfo);
                }
                GameTestBatch batch = GameTestBatchFactory.toGameTestBatch(transformedInfos, info.getTest().batch(), rotation.ordinal(), info.getLevel().dimension());
                batches.add(batch);
            }
        }
        StructureGridSpawner spawner = new StructureGridSpawner(testPos, 10, true);
        GameTestRunner runner = GameTestRunner.Builder.fromBatches(batches, source.getServer()).batcher(GameTestBatchFactory.fromGameTestInfo(100)).newStructureSpawner(spawner).existingStructureSpawner(spawner).haltOnError().clearBetweenBatches().build();
        return TestCommand.trackAndStartRunner(source, runner);
    }

    private static int run(TestFinder finder, RetryOptions retryOptions, int extraRotationSteps, int testsPerRow) {
        TestCommand.stopTests();
        CommandSourceStack source = finder.source();
        List<GameTestInfo> infos = Stream.concat(TestCommand.toGameTestInfos(source, retryOptions, finder), TestCommand.toGameTestInfo(source, retryOptions, finder, extraRotationSteps)).toList();
        if (infos.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.test.no_tests"), false);
            return 0;
        }
        FailedTestTracker.forgetFailedTests();
        source.sendSuccess(() -> Component.translatable("commands.test.run.running", infos.size()), false);
        Function<ResourceKey<Level>, BlockPos> testPos = dimension -> TestCommand.createTestPositionAround(source, dimension);
        GameTestRunner runner = GameTestRunner.Builder.fromInfo(infos, source.getServer()).newStructureSpawner(new StructureGridSpawner(testPos, testsPerRow, false)).build();
        return TestCommand.trackAndStartRunner(source, runner);
    }

    private static Stream<GlobalPos> findSelectedTestPositionsAcrossDimensions(TestFinder finder) {
        Set selectedTests = finder.findTests().map(Holder.Reference::key).collect(Collectors.toSet());
        if (selectedTests.isEmpty()) {
            return Stream.empty();
        }
        CommandSourceStack source = finder.source();
        MinecraftServer server = source.getServer();
        BlockPos sourcePos = BlockPos.containing(source.getPosition());
        ArrayList matches = new ArrayList();
        for (ServerLevel level : server.getAllLevels()) {
            StructureUtils.findTestBlocks(sourcePos, 250, level).flatMap(pos -> level.getBlockEntity((BlockPos)pos, BlockEntityTypes.TEST_INSTANCE_BLOCK).stream()).filter(blockEntity -> blockEntity.test().filter(selectedTests::contains).isPresent()).map(blockEntity -> new GlobalPos(level.dimension(), blockEntity.getBlockPos())).forEach(matches::add);
        }
        return matches.stream();
    }

    private static int locate(TestFinder finder) throws CommandSyntaxException {
        CommandSourceStack source = finder.source();
        source.sendSystemMessage(Component.translatable("commands.test.locate.started"));
        MutableInt structuresFound = new MutableInt(0);
        MinecraftServer server = source.getServer();
        BlockPos sourcePos = BlockPos.containing(source.getPosition());
        ResourceKey<Level> sourceDimension = source.getLevel().dimension();
        ReferenceArraySet foundTestDimensions = new ReferenceArraySet();
        Stream.concat(finder.findTestPos(), TestCommand.findSelectedTestPositionsAcrossDimensions(finder)).distinct().forEach(arg_0 -> TestCommand.lambda$locate$0(server, (Set)foundTestDimensions, sourcePos, sourceDimension, source, structuresFound, arg_0));
        int structures = structuresFound.intValue();
        if (structures == 0) {
            throw NO_TEST_INSTANCES.create();
        }
        TestCommand.outputPlayerCoordinates(source, (Set<ResourceKey<Level>>)foundTestDimensions);
        source.sendSuccess(() -> Component.translatable("commands.test.locate.done", structures), true);
        return structures;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(ArgumentBuilder<CommandSourceStack, ?> runArgument, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> finder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> then) {
        return runArgument.executes(c -> TestCommand.run((TestFinder)finder.apply(c), RetryOptions.noRetries(), 0, 8)).then(((RequiredArgumentBuilder)Commands.argument("numberOfTimes", IntegerArgumentType.integer((int)0)).executes(c -> TestCommand.run((TestFinder)finder.apply(c), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)c, (String)"numberOfTimes"), false), 0, 8))).then(then.apply(Commands.argument("untilFailed", BoolArgumentType.bool()).executes(c -> TestCommand.run((TestFinder)finder.apply(c), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)c, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)c, (String)"untilFailed")), 0, 8)))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(ArgumentBuilder<CommandSourceStack, ?> runArgument, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> finder) {
        return TestCommand.runWithRetryOptions(runArgument, finder, a -> a);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptionsAndBuildInfo(ArgumentBuilder<CommandSourceStack, ?> runArgument, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> finder) {
        return TestCommand.runWithRetryOptions(runArgument, finder, then -> then.then(((RequiredArgumentBuilder)Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes(c -> TestCommand.run((TestFinder)finder.apply(c), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)c, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)c, (String)"untilFailed")), IntegerArgumentType.getInteger((CommandContext)c, (String)"rotationSteps"), 8))).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes(c -> TestCommand.run((TestFinder)finder.apply(c), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)c, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)c, (String)"untilFailed")), IntegerArgumentType.getInteger((CommandContext)c, (String)"rotationSteps"), IntegerArgumentType.getInteger((CommandContext)c, (String)"testsPerRow"))))));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        ArgumentBuilder<CommandSourceStack, ?> runFailedWithRequiredTestsFlag = TestCommand.runWithRetryOptionsAndBuildInfo(Commands.argument("onlyRequiredTests", BoolArgumentType.bool()), c -> TestFinder.builder().failedTests((CommandContext<CommandSourceStack>)c, BoolArgumentType.getBool((CommandContext)c, (String)"onlyRequiredTests")));
        LiteralArgumentBuilder testCommand = (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("test").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("run").then(TestCommand.runWithRetryOptionsAndBuildInfo(Commands.argument("tests", ResourceSelectorArgument.resourceSelector(context, Registries.TEST_INSTANCE)).suggests(TestCommand::suggestTestInstanceIds), c -> TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)c, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)c, "tests")))))).then(Commands.literal("runmultiple").then(((RequiredArgumentBuilder)Commands.argument("tests", ResourceSelectorArgument.resourceSelector(context, Registries.TEST_INSTANCE)).suggests(TestCommand::suggestTestInstanceIds).executes(c -> TestCommand.run(TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)c, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)c, "tests")), RetryOptions.noRetries(), 0, 8))).then(Commands.argument("amount", IntegerArgumentType.integer()).executes(c -> TestCommand.run(TestFinder.builder().createMultipleCopies(IntegerArgumentType.getInteger((CommandContext)c, (String)"amount")).byResourceSelection((CommandContext<CommandSourceStack>)c, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)c, "tests")), RetryOptions.noRetries(), 0, 8)))))).then(TestCommand.runWithRetryOptions(Commands.literal("runthese"), TestFinder.builder()::allNearby))).then(TestCommand.runWithRetryOptions(Commands.literal("runclosest"), TestFinder.builder()::nearest))).then(TestCommand.runWithRetryOptions(Commands.literal("runthat"), TestFinder.builder()::lookedAt))).then(TestCommand.runWithRetryOptionsAndBuildInfo(Commands.literal("runfailed").then(runFailedWithRequiredTestsFlag), TestFinder.builder()::failedTests))).then(Commands.literal("verify").then(Commands.argument("tests", ResourceSelectorArgument.resourceSelector(context, Registries.TEST_INSTANCE)).suggests(TestCommand::suggestTestInstanceIds).executes(c -> TestCommand.verify(TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)c, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)c, "tests"))))))).then(Commands.literal("locate").then(Commands.argument("tests", ResourceSelectorArgument.resourceSelector(context, Registries.TEST_INSTANCE)).suggests(TestCommand::suggestTestInstanceIds).executes(c -> TestCommand.locate(TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)c, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)c, "tests"))))))).then(Commands.literal("resetclosest").executes(c -> TestCommand.reset(TestFinder.builder().nearest((CommandContext<CommandSourceStack>)c))))).then(Commands.literal("resetthese").executes(c -> TestCommand.reset(TestFinder.builder().allNearby((CommandContext<CommandSourceStack>)c))))).then(Commands.literal("resetthat").executes(c -> TestCommand.reset(TestFinder.builder().lookedAt((CommandContext<CommandSourceStack>)c))))).then(Commands.literal("clearthat").executes(c -> TestCommand.clear(TestFinder.builder().lookedAt((CommandContext<CommandSourceStack>)c))))).then(Commands.literal("clearthese").executes(c -> TestCommand.clear(TestFinder.builder().allNearby((CommandContext<CommandSourceStack>)c))))).then(((LiteralArgumentBuilder)Commands.literal("clearall").executes(c -> TestCommand.clear(TestFinder.builder().radius((CommandContext<CommandSourceStack>)c, 250)))).then(Commands.argument("radius", IntegerArgumentType.integer()).executes(c -> TestCommand.clear(TestFinder.builder().radius((CommandContext<CommandSourceStack>)c, Mth.clamp(IntegerArgumentType.getInteger((CommandContext)c, (String)"radius"), 0, 1024))))))).then(Commands.literal("stop").executes(c -> TestCommand.stopTests()))).then(((LiteralArgumentBuilder)Commands.literal("pos").executes(c -> TestCommand.showPos((CommandSourceStack)c.getSource(), "pos"))).then(Commands.argument("var", StringArgumentType.word()).executes(c -> TestCommand.showPos((CommandSourceStack)c.getSource(), StringArgumentType.getString((CommandContext)c, (String)"var")))))).then(Commands.literal("create").then(((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id()).executes(c -> TestCommand.createNewStructure((CommandSourceStack)c.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)c, "id"), 5, 5, 5))).then(((RequiredArgumentBuilder)Commands.argument("width", IntegerArgumentType.integer()).executes(c -> TestCommand.createNewStructure((CommandSourceStack)c.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)c, "id"), IntegerArgumentType.getInteger((CommandContext)c, (String)"width"), IntegerArgumentType.getInteger((CommandContext)c, (String)"width"), IntegerArgumentType.getInteger((CommandContext)c, (String)"width")))).then(Commands.argument("height", IntegerArgumentType.integer()).then(Commands.argument("depth", IntegerArgumentType.integer()).executes(c -> TestCommand.createNewStructure((CommandSourceStack)c.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)c, "id"), IntegerArgumentType.getInteger((CommandContext)c, (String)"width"), IntegerArgumentType.getInteger((CommandContext)c, (String)"height"), IntegerArgumentType.getInteger((CommandContext)c, (String)"depth"))))))));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            testCommand = (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)testCommand.then(Commands.literal("export").then(Commands.argument("test", ResourceArgument.resource(context, Registries.TEST_INSTANCE)).suggests(TestCommand::suggestTestInstanceIds).executes(c -> TestCommand.exportTestStructure((CommandSourceStack)c.getSource(), ResourceArgument.getResource((CommandContext<CommandSourceStack>)c, "test", Registries.TEST_INSTANCE)))))).then(Commands.literal("exportclosest").executes(c -> TestCommand.export(TestFinder.builder().nearest((CommandContext<CommandSourceStack>)c))))).then(Commands.literal("exportthese").executes(c -> TestCommand.export(TestFinder.builder().allNearby((CommandContext<CommandSourceStack>)c))))).then(Commands.literal("exportthat").executes(c -> TestCommand.export(TestFinder.builder().lookedAt((CommandContext<CommandSourceStack>)c))));
        }
        dispatcher.register(testCommand);
    }

    public static CompletableFuture<Suggestions> suggestTestFunction(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Stream<String> testNamesStream = ((CommandSourceStack)context.getSource()).registryAccess().lookupOrThrow(Registries.TEST_FUNCTION).listElements().map(Holder::getRegisteredName);
        return SharedSuggestionProvider.suggest(testNamesStream, builder);
    }

    private static String displayTestId(Identifier id) {
        return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    private static CompletableFuture<Suggestions> suggestTestInstanceIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<Identifier> testIds = ((CommandSourceStack)context.getSource()).registryAccess().lookupOrThrow(Registries.TEST_INSTANCE).listElementIds().map(ResourceKey::identifier).toList();
        SharedSuggestionProvider.filterResources(testIds, builder.getRemaining().toLowerCase(Locale.ROOT), id -> id, id -> builder.suggest(TestCommand.displayTestId(id)));
        return builder.buildFuture();
    }

    private static int resetGameTestInfo(CommandSourceStack source, GameTestInfo testInfo) {
        TestInstanceBlockEntity blockEntity = testInfo.getTestInstanceBlockEntity();
        blockEntity.resetTest(source::sendSystemMessage);
        return 1;
    }

    private static Stream<GameTestInfo> toGameTestInfos(CommandSourceStack source, RetryOptions retryOptions, TestPosFinder finder) {
        return finder.findTestPos().map(pos -> TestCommand.createGameTestInfo(pos, source, retryOptions)).flatMap(Optional::stream);
    }

    private static Stream<GameTestInfo> toGameTestInfo(CommandSourceStack source, RetryOptions retryOptions, TestInstanceFinder finder, int rotationSteps) {
        return finder.findTests().filter(test -> TestCommand.verifyStructureExists(source, ((GameTestInstance)test.value()).structure())).flatMap(test -> {
            ResourceKey<Level> dimension = ((GameTestInstance)test.value()).info().dimension();
            ServerLevel level = source.getServer().getLevel(dimension);
            if (level == null) {
                source.sendFailure(Component.literal("Could not resolve level for dimension: " + String.valueOf(dimension.identifier())));
                return Stream.empty();
            }
            return Stream.of(new GameTestInfo((Holder.Reference<GameTestInstance>)test, StructureUtils.getRotationForRotationSteps(rotationSteps), level, retryOptions));
        });
    }

    private static Optional<GameTestInfo> createGameTestInfo(GlobalPos testBlockPos, CommandSourceStack source, RetryOptions retryOptions) {
        ServerLevel level = source.getServer().getLevel(testBlockPos.dimension());
        BlockEntity blockEntity = level.getBlockEntity(testBlockPos.pos());
        if (!(blockEntity instanceof TestInstanceBlockEntity)) {
            source.sendFailure(Component.translatable("commands.test.error.test_instance_not_found.position", testBlockPos.pos().getX(), testBlockPos.pos().getY(), testBlockPos.pos().getZ()));
            return Optional.empty();
        }
        TestInstanceBlockEntity blockEntity2 = (TestInstanceBlockEntity)blockEntity;
        Optional maybeTest = blockEntity2.test().flatMap(((Registry)source.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE))::get);
        if (maybeTest.isEmpty()) {
            source.sendFailure(Component.translatable("commands.test.error.non_existant_test", blockEntity2.getTestName()));
            return Optional.empty();
        }
        Holder.Reference test = (Holder.Reference)maybeTest.get();
        GameTestInfo testInfo = new GameTestInfo(test, blockEntity2.getRotation(), level, retryOptions);
        testInfo.setTestBlockPos(testBlockPos.pos());
        if (!TestCommand.verifyStructureExists(source, testInfo.getStructure())) {
            return Optional.empty();
        }
        return Optional.of(testInfo);
    }

    private static int createNewStructure(CommandSourceStack source, Identifier id, int xSize, int ySize, int zSize) throws CommandSyntaxException {
        if (xSize > 48 || ySize > 48 || zSize > 48) {
            throw TOO_LARGE.create((Object)48);
        }
        ServerLevel level = source.getLevel();
        BlockPos testPos = TestCommand.createTestPositionAround(source, level.dimension());
        TestInstanceBlockEntity test = StructureUtils.createNewEmptyTest(id, testPos, new Vec3i(xSize, ySize, zSize), Rotation.NONE, level);
        BlockPos low = test.getStructurePos();
        BlockPos high = low.offset(xSize - 1, 0, zSize - 1);
        BlockPos.betweenClosedStream(low, high).forEach(blockPos -> level.setBlockAndUpdate((BlockPos)blockPos, Blocks.BEDROCK.defaultBlockState()));
        source.sendSuccess(() -> Component.translatable("commands.test.create.success", test.getTestName()), true);
        return 1;
    }

    private static int showPos(CommandSourceStack source, String varName) throws CommandSyntaxException {
        ServerLevel level;
        ServerPlayer player = source.getPlayerOrException();
        BlockHitResult pick = (BlockHitResult)player.pick(10.0, 1.0f, false);
        BlockPos targetPosAbsolute = pick.getBlockPos();
        Optional<BlockPos> testBlockPos = StructureUtils.findTestContainingPos(targetPosAbsolute, 15, level = source.getLevel());
        if (testBlockPos.isEmpty()) {
            testBlockPos = StructureUtils.findTestContainingPos(targetPosAbsolute, 250, level);
        }
        if (testBlockPos.isEmpty()) {
            throw NO_TEST_CONTAINING.create((Object)targetPosAbsolute.getX(), (Object)targetPosAbsolute.getY(), (Object)targetPosAbsolute.getZ());
        }
        BlockEntity blockEntity = level.getBlockEntity(testBlockPos.get());
        if (!(blockEntity instanceof TestInstanceBlockEntity)) {
            throw TEST_INSTANCE_COULD_NOT_BE_FOUND.create();
        }
        TestInstanceBlockEntity testBlockEntity = (TestInstanceBlockEntity)blockEntity;
        BlockPos testOrigin = testBlockEntity.getStructurePos();
        BlockPos targetPosRelative = targetPosAbsolute.subtract(testOrigin);
        String targetPosDescription = targetPosRelative.getX() + ", " + targetPosRelative.getY() + ", " + targetPosRelative.getZ();
        String testName = testBlockEntity.getTestName().getString();
        MutableComponent coords = Component.translatable("commands.test.coordinates", targetPosRelative.getX(), targetPosRelative.getY(), targetPosRelative.getZ()).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withHoverEvent(new HoverEvent.ShowText(Component.translatable("commands.test.coordinates.copy"))).withClickEvent(new ClickEvent.CopyToClipboard("final BlockPos " + varName + " = new BlockPos(" + targetPosDescription + ");")));
        source.sendSuccess(() -> Component.translatable("commands.test.relative_position", testName, coords), false);
        player.connection.send(new ClientboundGameTestHighlightPosPacket(targetPosAbsolute, targetPosRelative));
        return 1;
    }

    private static int stopTests() {
        GameTestTicker.SINGLETON.clear();
        return 1;
    }

    public static int trackAndStartRunner(CommandSourceStack source, GameTestRunner runner) {
        runner.addListener(new TestBatchSummaryDisplayer(source));
        MultipleTestTracker tracker = new MultipleTestTracker(runner.getTestInfos());
        tracker.addListener(new TestSummaryDisplayer(source, tracker));
        tracker.addFailureListener(testInfo -> FailedTestTracker.rememberFailedTest(testInfo.getTestHolder()));
        runner.start();
        return 1;
    }

    private static int exportTestStructure(CommandSourceStack source, Holder<GameTestInstance> test) {
        if (!TestInstanceBlockEntity.export(source.getLevel(), test.value().structure(), source::sendSystemMessage)) {
            return 0;
        }
        return 1;
    }

    private static boolean verifyStructureExists(CommandSourceStack source, Identifier structure) {
        if (source.getLevel().getStructureTemplateManager().get(structure).isEmpty()) {
            source.sendFailure(Component.translatable("commands.test.error.structure_not_found", Component.translationArg(structure)));
            return false;
        }
        return true;
    }

    private static BlockPos createTestPositionAround(CommandSourceStack source, ResourceKey<Level> dimension) {
        Info info = TestCommand.playerAndTestInfo(source, dimension);
        return new BlockPos(info.playerPos.getX(), info.surfaceY, info.playerPos.getZ() + 3);
    }

    private static Info playerAndTestInfo(CommandSourceStack source, ResourceKey<Level> dimension) {
        ServerPlayer serverPlayer = source.getPlayer();
        BlockPos playerPos = BlockPos.containing(serverPlayer == null ? source.getPosition() : serverPlayer.position());
        BlockPos testPos = new BlockPos(playerPos.getX(), 384, playerPos.getZ());
        ServerLevel level = source.getServer().getLevel(dimension);
        level.getChunk(testPos);
        int surfaceY = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, testPos).getY();
        return new Info(source.getPlayer(), playerPos, testPos, surfaceY);
    }

    private static void outputTestCoordinates(CommandSourceStack source, Set<ResourceKey<Level>> testDimensions) {
        for (ResourceKey<Level> testDimensionKey : testDimensions) {
            Info testInfo = TestCommand.playerAndTestInfo(source, testDimensionKey);
            Info playerInfo = TestCommand.playerAndTestInfo(source, source.getLevel().dimension());
            if (TestCommand.isPlayerInCurrentTestDimension(playerInfo, testDimensions)) {
                return;
            }
            String testDimension = testDimensionKey.identifier().toString();
            MutableComponent testCoordinates = ComponentUtils.wrapInSquareBrackets(Component.translatable("test.run.coordinates", testInfo.playerPos.getX(), testInfo.surfaceY, testInfo.playerPos.getZ(), testDimension.substring(testDimension.indexOf(58) + 1))).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withClickEvent(new ClickEvent.SuggestCommand("/execute in " + testDimension + " run tp @s " + testInfo.playerPos.getX() + " " + testInfo.surfaceY + " " + testInfo.playerPos.getZ() + " 0 0")).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip"))));
            source.sendSuccess(() -> testCoordinates, false);
        }
    }

    private static void outputPlayerCoordinates(CommandSourceStack source, Set<ResourceKey<Level>> testDimensions) {
        Info playerInfo = TestCommand.playerAndTestInfo(source, source.getLevel().dimension());
        if (TestCommand.isPlayerInCurrentTestDimension(playerInfo, testDimensions)) {
            return;
        }
        ServerPlayer player = playerInfo.player;
        if (player == null) {
            return;
        }
        float playerRotation = player.getYRot();
        float playerPitch = player.getXRot();
        ResourceKey<Level> playerDimensionKey = player.level().dimension();
        String playerDimension = playerDimensionKey.identifier().toString();
        MutableComponent playerCoordinates = ComponentUtils.wrapInSquareBrackets(Component.translatable("test.player.coordinates", playerInfo.playerPos.getX(), playerInfo.playerPos.getY(), playerInfo.playerPos.getZ(), playerDimension.substring(playerDimension.indexOf(58) + 1))).withStyle(s -> s.withColor(ChatFormatting.GOLD).withClickEvent(new ClickEvent.SuggestCommand("/execute in " + playerDimension + " run tp @s " + playerInfo.playerPos.getX() + " " + playerInfo.playerPos.getY() + " " + playerInfo.playerPos.getZ() + " " + Mth.floor(playerRotation) + " " + Mth.floor(playerPitch))).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip"))));
        source.sendSuccess(() -> playerCoordinates, false);
    }

    private static boolean isPlayerInCurrentTestDimension(Info playerInfo, Set<ResourceKey<Level>> testDimensions) {
        ServerPlayer player = playerInfo.player;
        if (player == null) {
            return false;
        }
        ResourceKey<Level> playerDimensionKey = player.level().dimension();
        return testDimensions.size() <= 1 && testDimensions.contains(playerDimensionKey) && testDimensions.contains(Level.OVERWORLD);
    }

    private static /* synthetic */ void lambda$locate$0(MinecraftServer server, Set foundTestDimensions, BlockPos sourcePos, ResourceKey sourceDimension, CommandSourceStack source, MutableInt structuresFound, GlobalPos structurePos) {
        BlockEntity patt0$temp = server.getLevel(structurePos.dimension()).getBlockEntity(structurePos.pos());
        if (!(patt0$temp instanceof TestInstanceBlockEntity)) {
            return;
        }
        TestInstanceBlockEntity testBlock = (TestInstanceBlockEntity)patt0$temp;
        foundTestDimensions.add(structurePos.dimension());
        Direction facingDirection = testBlock.getRotation().rotate(Direction.NORTH);
        BlockPos teleportPosition = testBlock.getBlockPos().relative(facingDirection, 3);
        int teleportYRot = (int)facingDirection.getOpposite().toYRot();
        String dimensionId = structurePos.dimension().identifier().toString();
        String tpCommand = String.format(Locale.ROOT, "/execute in %s run tp @s %d %d %d %d 0", dimensionId, teleportPosition.getX(), teleportPosition.getY(), teleportPosition.getZ(), teleportYRot);
        int dx = sourcePos.getX() - structurePos.pos().getX();
        int dz = sourcePos.getZ() - structurePos.pos().getZ();
        String distance = structurePos.dimension().equals(sourceDimension) ? Integer.toString(Mth.floor(Mth.sqrt(dx * dx + dz * dz))) : "N/A";
        MutableComponent coordinates = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", structurePos.pos().getX(), structurePos.pos().getY(), structurePos.pos().getZ()).append(Component.literal(" ->[" + structurePos.dimension().identifier().getPath() + "]"))).withStyle(s -> s.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent.SuggestCommand(tpCommand)).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip"))));
        source.sendSuccess(() -> Component.translatable("commands.test.locate.found", coordinates, distance), false);
        structuresFound.increment();
    }

    private record TestBatchSummaryDisplayer(CommandSourceStack source) implements GameTestBatchListener
    {
        @Override
        public void testBatchStarting(GameTestBatch batch) {
            this.source.sendSuccess(() -> Component.translatable("commands.test.batch.starting", batch.environment().getRegisteredName(), batch.index()), true);
        }

        @Override
        public void testBatchFinished(GameTestBatch batch) {
        }
    }

    public static final class TestSummaryDisplayer
    implements GameTestListener {
        private final CommandSourceStack source;
        private final MultipleTestTracker tracker;
        private final Set<ResourceKey<Level>> dimensions = new ReferenceArraySet();

        public TestSummaryDisplayer(CommandSourceStack source, MultipleTestTracker tracker) {
            this.source = source;
            this.tracker = tracker;
        }

        @Override
        public void testStructureLoaded(GameTestInfo testInfo) {
        }

        @Override
        public void testPassed(GameTestInfo testInfo, GameTestRunner runner) {
            this.dimensions.add(testInfo.getTest().dimension());
            this.showTestSummaryIfAllDone();
        }

        @Override
        public void testFailed(GameTestInfo testInfo, GameTestRunner runner) {
            this.dimensions.add(testInfo.getTest().dimension());
            this.showTestSummaryIfAllDone();
        }

        @Override
        public void testAddedForRerun(GameTestInfo original, GameTestInfo copy, GameTestRunner runner) {
            this.tracker.addTestToTrack(copy);
        }

        private void showTestSummaryIfAllDone() {
            if (this.tracker.isDone()) {
                this.source.sendSuccess(() -> Component.translatable("commands.test.summary", this.tracker.getTotalCount()).withStyle(ChatFormatting.WHITE), true);
                if (this.tracker.hasFailedRequired()) {
                    this.source.sendFailure(Component.translatable("commands.test.summary.failed", this.tracker.getFailedRequiredCount()));
                } else {
                    this.source.sendSuccess(() -> Component.translatable("commands.test.summary.all_required_passed").withStyle(ChatFormatting.GREEN), true);
                }
                if (this.tracker.hasFailedOptional()) {
                    this.source.sendSystemMessage(Component.translatable("commands.test.summary.optional_failed", this.tracker.getFailedOptionalCount()));
                }
                TestCommand.outputPlayerCoordinates(this.source, this.dimensions);
                TestCommand.outputTestCoordinates(this.source, this.dimensions);
            }
        }
    }

    private record Info(@Nullable ServerPlayer player, BlockPos playerPos, BlockPos testPos, int surfaceY) {
    }
}

