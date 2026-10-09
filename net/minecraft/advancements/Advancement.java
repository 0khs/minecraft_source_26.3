/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

public record Advancement(Optional<Identifier> parent, Optional<DisplayInfo> display, AdvancementRewards rewards, Map<String, Criterion<?>> criteria, AdvancementRequirements requirements, boolean sendsTelemetryEvent, Optional<Component> name) {
    private static final Codec<Map<String, Criterion<?>>> CRITERIA_CODEC = Codec.unboundedMap((Codec)Codec.STRING, Criterion.CODEC).validate((T criteria) -> criteria.isEmpty() ? DataResult.error(() -> "Advancement criteria cannot be empty") : DataResult.success((Object)criteria));
    public static final Codec<Advancement> CODEC = RecordCodecBuilder.create(i -> i.group((App)Identifier.CODEC.optionalFieldOf("parent").forGetter(Advancement::parent), (App)DisplayInfo.CODEC.optionalFieldOf("display").forGetter(Advancement::display), (App)AdvancementRewards.CODEC.optionalFieldOf("rewards", (Object)AdvancementRewards.EMPTY).forGetter(Advancement::rewards), (App)CRITERIA_CODEC.fieldOf("criteria").forGetter(Advancement::criteria), (App)AdvancementRequirements.CODEC.optionalFieldOf("requirements").forGetter(a -> Optional.of(a.requirements())), (App)Codec.BOOL.optionalFieldOf("sends_telemetry_event", (Object)false).forGetter(Advancement::sendsTelemetryEvent)).apply((Applicative)i, (parent, display, rewards, criteria, requirementsOpt, sendsTelemetryEvent) -> {
        AdvancementRequirements requirements = requirementsOpt.orElseGet(() -> AdvancementRequirements.allOf(criteria.keySet()));
        return new Advancement((Optional<Identifier>)parent, (Optional<DisplayInfo>)display, (AdvancementRewards)rewards, (Map<String, Criterion<?>>)criteria, requirements, (boolean)sendsTelemetryEvent);
    })).validate(Advancement::validate);
    public static final StreamCodec<RegistryFriendlyByteBuf, Advancement> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional), Advancement::parent, DisplayInfo.STREAM_CODEC.apply(ByteBufCodecs::optional), Advancement::display, AdvancementRequirements.STREAM_CODEC, Advancement::requirements, ByteBufCodecs.BOOL, Advancement::sendsTelemetryEvent, (parent, display, requirements, sendsTelemetryEvent) -> new Advancement((Optional<Identifier>)parent, (Optional<DisplayInfo>)display, AdvancementRewards.EMPTY, Map.of(), (AdvancementRequirements)requirements, (boolean)sendsTelemetryEvent));

    public Advancement(Optional<Identifier> parent, Optional<DisplayInfo> display, AdvancementRewards rewards, Map<String, Criterion<?>> criteria, AdvancementRequirements requirements, boolean sendsTelemetryEvent) {
        this(parent, display, rewards, Map.copyOf(criteria), requirements, sendsTelemetryEvent, display.map(Advancement::decorateName));
    }

    private static DataResult<Advancement> validate(Advancement advancement) {
        boolean hasBackground;
        boolean isRoot = advancement.parent.isEmpty();
        boolean isVisible = advancement.display.isPresent();
        boolean bl = hasBackground = isVisible && advancement.display.get().background().isPresent();
        if (isRoot && isVisible && !hasBackground) {
            return DataResult.error(() -> "Visible advancement roots must have background");
        }
        if (!isRoot && hasBackground) {
            return DataResult.error(() -> "Only advancement roots can have background");
        }
        DataResult<AdvancementRequirements> requirementValidation = advancement.requirements().validate(advancement.criteria().keySet());
        if (requirementValidation.isError()) {
            return requirementValidation.map(advancementRequirements -> advancement);
        }
        return DataResult.success((Object)advancement);
    }

    private static Component decorateName(DisplayInfo display) {
        Component displayTitle = display.title();
        ChatFormatting color = display.type().getChatColor();
        MutableComponent tooltip = ComponentUtils.mergeStyles(displayTitle.copy(), Style.EMPTY.withColor(color)).append("\n").append(display.description());
        MutableComponent title = displayTitle.copy().withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(tooltip)));
        return ComponentUtils.wrapInSquareBrackets(title).withStyle(color);
    }

    public static Component name(AdvancementHolder holder) {
        return holder.value().name().orElseGet(() -> Component.literal(holder.id().toString()));
    }

    public boolean isRoot() {
        return this.parent.isEmpty();
    }

    public void validate(ProblemReporter reporter, HolderGetter.Provider lootData) {
        this.criteria.forEach((name, criterion) -> {
            ValidationContextSource validator = new ValidationContextSource(reporter.forChild(new ProblemReporter.RootFieldPathElement((String)name)), lootData);
            criterion.triggerInstance().validate(validator);
        });
    }

    public static class Builder {
        private Optional<Identifier> parent = Optional.empty();
        private Optional<DisplayInfo> display = Optional.empty();
        private AdvancementRewards rewards = AdvancementRewards.EMPTY;
        private final ImmutableMap.Builder<String, Criterion<?>> criteria = ImmutableMap.builder();
        private Optional<AdvancementRequirements> requirements = Optional.empty();
        private AdvancementRequirements.Strategy requirementsStrategy = AdvancementRequirements.Strategy.AND;
        private boolean sendsTelemetryEvent;

        public static Builder advancement() {
            return new Builder().sendsTelemetryEvent();
        }

        public static Builder recipeAdvancement() {
            return new Builder();
        }

        public Builder parent(AdvancementHolder parent) {
            this.parent = Optional.of(parent.id());
            return this;
        }

        @Deprecated(forRemoval=true)
        public Builder parent(Identifier parent) {
            this.parent = Optional.of(parent);
            return this;
        }

        public Builder rootDisplay(ItemStackTemplate icon, Component title, Component description, Identifier background, AdvancementType frame, boolean showToast, boolean announceChat, boolean hidden) {
            return this.display(new DisplayInfo(icon, title, description, Optional.of(new ClientAsset.ResourceTexture(background)), frame, showToast, announceChat, hidden));
        }

        public Builder rootDisplay(Item icon, Component title, Component description, Identifier background, AdvancementType frame, boolean showToast, boolean announceChat, boolean hidden) {
            return this.rootDisplay(new ItemStackTemplate(icon), title, description, background, frame, showToast, announceChat, hidden);
        }

        public Builder display(ItemStackTemplate icon, Component title, Component description, AdvancementType frame, boolean showToast, boolean announceChat, boolean hidden) {
            return this.display(new DisplayInfo(icon, title, description, Optional.empty(), frame, showToast, announceChat, hidden));
        }

        public Builder display(Item icon, Component title, Component description, AdvancementType frame, boolean showToast, boolean announceChat, boolean hidden) {
            return this.display(new ItemStackTemplate(icon), title, description, frame, showToast, announceChat, hidden);
        }

        public Builder display(DisplayInfo display) {
            this.display = Optional.of(display);
            return this;
        }

        public Builder rewards(AdvancementRewards.Builder rewards) {
            return this.rewards(rewards.build());
        }

        public Builder rewards(AdvancementRewards rewards) {
            this.rewards = rewards;
            return this;
        }

        public Builder addCriterion(String name, Criterion<?> criterion) {
            this.criteria.put((Object)name, criterion);
            return this;
        }

        public Builder requirements(AdvancementRequirements.Strategy strategy) {
            this.requirementsStrategy = strategy;
            return this;
        }

        public Builder requirements(AdvancementRequirements requirements) {
            this.requirements = Optional.of(requirements);
            return this;
        }

        public Builder sendsTelemetryEvent() {
            this.sendsTelemetryEvent = true;
            return this;
        }

        public AdvancementHolder build(Identifier id) {
            ImmutableMap criteria = this.criteria.buildOrThrow();
            AdvancementRequirements requirements = this.requirements.orElseGet(() -> this.lambda$build$0((Map)criteria));
            return new AdvancementHolder(id, new Advancement(this.parent, this.display, this.rewards, (Map<String, Criterion<?>>)criteria, requirements, this.sendsTelemetryEvent));
        }

        public AdvancementHolder save(BootstrapContext<Advancement> output, String name) {
            AdvancementHolder advancement = this.build(Identifier.parse(name));
            advancement.register(output);
            return advancement;
        }

        private /* synthetic */ AdvancementRequirements lambda$build$0(Map criteria) {
            return this.requirementsStrategy.create(criteria.keySet());
        }
    }
}

