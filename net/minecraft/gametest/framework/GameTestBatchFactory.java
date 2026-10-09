/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Streams
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

public class GameTestBatchFactory {
    private static final int MAX_TESTS_PER_BATCH = 50;
    public static final TestDecorator DIRECT = (test, level) -> Stream.of(new GameTestInfo(test, Rotation.NONE, level, RetryOptions.noRetries()));

    public static List<GameTestBatch> divideIntoBatches(Collection<Holder.Reference<GameTestInstance>> allTests, TestDecorator decorator, MinecraftServer server) {
        Map<BatchKey, List<Holder.Reference>> testsPerBatch = allTests.stream().collect(Collectors.groupingBy(instance -> new BatchKey(((GameTestInstance)instance.value()).batch(), ((GameTestInstance)instance.value()).info().dimension())));
        return testsPerBatch.entrySet().stream().flatMap(e -> {
            BatchKey key = (BatchKey)e.getKey();
            Holder<TestEnvironmentDefinition<?>> batchKey = key.environment();
            ResourceKey<Level> dimensionKey = key.dimension();
            ServerLevel level = server.getLevel(dimensionKey);
            if (level == null) {
                throw new IllegalStateException("Missing level for dimension: " + String.valueOf(dimensionKey.identifier()));
            }
            List testsInBatch = ((List)e.getValue()).stream().flatMap(test -> decorator.decorate((Holder.Reference<GameTestInstance>)test, level)).toList();
            return Streams.mapWithIndex(Lists.partition(testsInBatch, (int)50).stream(), (tests, index) -> GameTestBatchFactory.toGameTestBatch(tests, batchKey, (int)index, dimensionKey));
        }).toList();
    }

    public static GameTestRunner.GameTestBatcher fromGameTestInfo() {
        return GameTestBatchFactory.fromGameTestInfo(50);
    }

    public static GameTestRunner.GameTestBatcher fromGameTestInfo(int maxTestsPerBatch) {
        return gameTestInfos -> {
            Map<BatchKey, List<GameTestInfo>> testsPerBatch = gameTestInfos.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(info -> new BatchKey(info.getTest().batch(), info.getTest().info().dimension())));
            return testsPerBatch.entrySet().stream().flatMap(e -> {
                BatchKey key = (BatchKey)e.getKey();
                List testsInBatch = (List)e.getValue();
                return Streams.mapWithIndex(Lists.partition((List)testsInBatch, (int)maxTestsPerBatch).stream(), (tests, index) -> GameTestBatchFactory.toGameTestBatch(List.copyOf(tests), key.environment(), (int)index, key.dimension()));
            }).toList();
        };
    }

    public static GameTestBatch toGameTestBatch(Collection<GameTestInfo> tests, Holder<TestEnvironmentDefinition<?>> batch, int counter, ResourceKey<Level> dimension) {
        return new GameTestBatch(counter, tests, batch, dimension);
    }

    @FunctionalInterface
    public static interface TestDecorator {
        public Stream<GameTestInfo> decorate(Holder.Reference<GameTestInstance> var1, ServerLevel var2);
    }

    private record BatchKey(Holder<TestEnvironmentDefinition<?>> environment, ResourceKey<Level> dimension) {
    }
}

