/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractFurnaceBlockEntity
extends BaseContainerBlockEntity
implements WorldlyContainer,
StackedContentsCompatible,
RecipeCraftingHolder {
    protected static final int SLOT_INPUT = 0;
    protected static final int SLOT_FUEL = 1;
    protected static final int SLOT_RESULT = 2;
    public static final int DATA_LIT_TIME = 0;
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{1};
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_COOKING_PROGRESS = 2;
    public static final int DATA_COOKING_TOTAL_TIME = 3;
    public static final int NUM_DATA_VALUES = 4;
    public static final int BURN_TIME_STANDARD = 200;
    public static final int BURN_COOL_SPEED = 2;
    private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, (Codec)Codec.INT);
    private static final int DEFAULT_COOKING_TIMER = 0;
    private static final int DEFAULT_COOKING_TOTAL_TIME = 0;
    private static final int DEFAULT_LIT_TIME_REMAINING = 0;
    private static final int DEFAULT_LIT_TOTAL_TIME = 0;
    private static final float DEFAULT_SPEED_MULTIPLIER = 1.0f;
    protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int litTimeRemaining;
    private int litTotalTime;
    private int cookingTimer;
    private int cookingTotalTime;
    private float speedMultiplier = 1.0f;
    protected final ContainerData dataAccess = new ContainerData(this){
        final /* synthetic */ AbstractFurnaceBlockEntity this$0;
        {
            AbstractFurnaceBlockEntity abstractFurnaceBlockEntity = this$0;
            Objects.requireNonNull(abstractFurnaceBlockEntity);
            this.this$0 = abstractFurnaceBlockEntity;
        }

        @Override
        public int get(int dataId) {
            switch (dataId) {
                case 0: {
                    return this.this$0.litTimeRemaining;
                }
                case 1: {
                    return this.this$0.litTotalTime;
                }
                case 2: {
                    return this.this$0.cookingTimer;
                }
                case 3: {
                    return this.this$0.cookingTotalTime;
                }
            }
            return 0;
        }

        @Override
        public void set(int dataId, int value) {
            switch (dataId) {
                case 0: {
                    this.this$0.litTimeRemaining = value;
                    break;
                }
                case 1: {
                    this.this$0.litTotalTime = value;
                    break;
                }
                case 2: {
                    this.this$0.cookingTimer = value;
                    break;
                }
                case 3: {
                    this.this$0.cookingTotalTime = value;
                    break;
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap();
    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;

    protected AbstractFurnaceBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType) {
        super(type, worldPosition, blockState);
        this.quickCheck = RecipeManager.createCheck(recipeType);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.cookingTimer = input.getIntOr("cooking_time_spent", 0);
        this.cookingTotalTime = input.getIntOr("cooking_total_time", 0);
        this.litTimeRemaining = input.getIntOr("lit_time_remaining", 0);
        this.litTotalTime = input.getIntOr("lit_total_time", 0);
        this.speedMultiplier = input.getFloatOr("speed_multiplier", 1.0f);
        this.recipesUsed.clear();
        this.recipesUsed.putAll(input.read("RecipesUsed", RECIPES_USED_CODEC).orElse(Map.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("cooking_time_spent", this.cookingTimer);
        output.putInt("cooking_total_time", this.cookingTotalTime);
        output.putInt("lit_time_remaining", this.litTimeRemaining);
        output.putInt("lit_total_time", this.litTotalTime);
        output.putFloat("speed_multiplier", this.speedMultiplier);
        ContainerHelper.saveAllItems(output, this.items);
        output.store("RecipesUsed", RECIPES_USED_CODEC, this.recipesUsed);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity entity) {
        boolean hasFuel;
        boolean isLit;
        boolean wasLit;
        boolean changed = false;
        if (entity.litTimeRemaining > 0) {
            wasLit = true;
            --entity.litTimeRemaining;
            isLit = entity.litTimeRemaining > 0;
        } else {
            wasLit = false;
            isLit = false;
        }
        ItemStack fuel = entity.items.get(1);
        ItemStack ingredient = entity.items.get(0);
        boolean hasIngredient = !ingredient.isEmpty();
        boolean bl = hasFuel = !fuel.isEmpty();
        if (isLit || hasFuel && hasIngredient) {
            if (hasIngredient) {
                SingleRecipeInput input = new SingleRecipeInput(ingredient);
                RecipeHolder recipe = entity.quickCheck.getRecipeFor(input, level).orElse(null);
                if (recipe != null) {
                    int maxStackSize = entity.getMaxStackSize();
                    ItemStack burnResult = ((AbstractCookingRecipe)recipe.value()).assemble(input);
                    if (!burnResult.isEmpty() && AbstractFurnaceBlockEntity.canBurn(entity.items, maxStackSize, burnResult)) {
                        if (!isLit) {
                            int newLitTime = entity.getBurnDuration(level, fuel);
                            float newSpeedMultiplier = entity.getSpeedMultiplier(level, fuel);
                            entity.litTimeRemaining = newLitTime;
                            entity.litTotalTime = newLitTime;
                            entity.speedMultiplier = newSpeedMultiplier;
                            if (entity.cookingTotalTime > 0 && entity.cookingTimer < entity.cookingTotalTime) {
                                float completionRatio = (float)entity.cookingTimer / (float)entity.cookingTotalTime;
                                entity.cookingTotalTime = AbstractFurnaceBlockEntity.getTotalCookTime(recipe, entity);
                                entity.cookingTimer = (int)Math.ceil(completionRatio * (float)entity.cookingTotalTime);
                            }
                            if (newLitTime > 0) {
                                AbstractFurnaceBlockEntity.consumeFuel(level, pos, entity.items, fuel);
                                isLit = true;
                                changed = true;
                            }
                        }
                        if (isLit) {
                            ++entity.cookingTimer;
                            if (entity.cookingTimer >= entity.cookingTotalTime) {
                                entity.cookingTimer = 0;
                                entity.cookingTotalTime = AbstractFurnaceBlockEntity.getTotalCookTime(recipe, entity);
                                AbstractFurnaceBlockEntity.burn(entity.items, ingredient, burnResult);
                                entity.setRecipeUsed(recipe);
                                changed = true;
                            }
                        } else {
                            entity.cookingTimer = 0;
                        }
                    } else {
                        entity.cookingTimer = 0;
                    }
                }
            } else {
                entity.cookingTimer = 0;
            }
        } else if (entity.cookingTimer > 0) {
            entity.cookingTimer = Mth.clamp(entity.cookingTimer - 2, 0, entity.cookingTotalTime);
        }
        if (wasLit != isLit) {
            changed = true;
            state = (BlockState)state.setValue(AbstractFurnaceBlock.LIT, isLit);
            level.setBlockAndUpdate(pos, state);
        }
        if (changed) {
            AbstractFurnaceBlockEntity.setChanged(level, pos, state);
        }
    }

    private static void consumeFuel(ServerLevel level, BlockPos pos, NonNullList<ItemStack> items, ItemStack fuel) {
        Item fuelItem = fuel.getItem();
        ItemStackTemplate remainder = fuelItem.getCraftingRemainder();
        ItemStack newFuel = fuel;
        fuel.shrink(1);
        if (remainder != null) {
            if (fuel.isEmpty()) {
                newFuel = remainder.create();
            } else {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder.create());
            }
        }
        items.set(1, newFuel);
    }

    private static boolean canBurn(NonNullList<ItemStack> items, int maxStackSize, ItemStack burnResult) {
        int maxResultCount;
        ItemStack resultItemStack = items.get(2);
        if (resultItemStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(resultItemStack, burnResult)) {
            return false;
        }
        int resultCount = resultItemStack.getCount() + burnResult.count();
        return resultCount <= (maxResultCount = Math.min(maxStackSize, burnResult.getMaxStackSize()));
    }

    private static void burn(NonNullList<ItemStack> items, ItemStack inputItemStack, ItemStack result) {
        ItemStack resultItemStack = items.get(2);
        if (resultItemStack.isEmpty()) {
            items.set(2, result.copy());
        } else {
            resultItemStack.grow(result.getCount());
        }
        if (inputItemStack.is(Items.WET_SPONGE) && !items.get(1).isEmpty() && items.get(1).is(Items.BUCKET)) {
            items.set(1, new ItemStack(Items.WATER_BUCKET));
        }
        inputItemStack.shrink(1);
    }

    protected int getBurnDuration(ServerLevel level, ItemStack fuelItem) {
        return ResolvableInt.getFromItem(fuelItem, DataComponents.COOKING_FUEL, CookingFuel::burnTime, this.getLootContext(level), 0);
    }

    protected float getSpeedMultiplier(ServerLevel level, ItemStack fuelItem) {
        return ResolvableFloat.getFromItem(fuelItem, DataComponents.COOKING_FUEL, CookingFuel::speedMultiplier, this.getLootContext(level), 1.0f);
    }

    private static int getTotalCookTime(RecipeHolder<? extends AbstractCookingRecipe> recipe, AbstractFurnaceBlockEntity entity) {
        int cookingTotalTime = recipe.value().cookingTime();
        return entity.speedMultiplier > 0.0f ? (int)Math.ceil((float)cookingTotalTime / entity.speedMultiplier) : cookingTotalTime;
    }

    private static int getTotalCookTime(ServerLevel level, AbstractFurnaceBlockEntity entity) {
        SingleRecipeInput input = new SingleRecipeInput(entity.getItem(0));
        return entity.quickCheck.getRecipeFor(input, level).map(recipeHolder -> AbstractFurnaceBlockEntity.getTotalCookTime(recipeHolder, entity)).orElse(200);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        }
        if (direction == Direction.UP) {
            return SLOTS_FOR_UP;
        }
        return SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        if (direction == Direction.DOWN && slot == 1) {
            return itemStack.is(ItemTags.FURNACE_FUEL_BOTTOM_TAKEABLE);
        }
        return true;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        Level level;
        ItemStack oldStack = this.items.get(slot);
        boolean same = !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(oldStack, itemStack);
        this.items.set(slot, itemStack);
        itemStack.limitSize(this.getMaxStackSize(itemStack));
        if (slot == 0 && !same && (level = this.level) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.cookingTotalTime = AbstractFurnaceBlockEntity.getTotalCookTime(serverLevel, this);
            this.cookingTimer = 0;
            this.setChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        if (slot == 2) {
            return false;
        }
        if (slot == 1) {
            ItemStack fuelSlot = this.items.get(1);
            return itemStack.has(DataComponents.COOKING_FUEL) || itemStack.is(Items.BUCKET) && !fuelSlot.is(Items.BUCKET);
        }
        return true;
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipeUsed) {
        if (recipeUsed != null) {
            ResourceKey<Recipe<?>> id = recipeUsed.id();
            this.recipesUsed.addTo(id, 1);
        }
    }

    @Override
    public @Nullable RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player player, List<ItemStack> itemStacks) {
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        List<RecipeHolder<?>> recipesToAward = this.getRecipesToAwardAndPopExperience(player.level(), player.position());
        player.awardRecipes(recipesToAward);
        for (RecipeHolder<?> recipe : recipesToAward) {
            player.triggerRecipeCrafted(recipe, this.items);
        }
        this.recipesUsed.clear();
    }

    public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel level, Vec3 position) {
        ArrayList recipesToAward = Lists.newArrayList();
        for (Reference2IntMap.Entry entry : this.recipesUsed.reference2IntEntrySet()) {
            level.recipeAccess().byKey((ResourceKey)entry.getKey()).ifPresent(recipe -> {
                recipesToAward.add(recipe);
                AbstractFurnaceBlockEntity.createExperience(level, position, entry.getIntValue(), ((AbstractCookingRecipe)recipe.value()).experience());
            });
        }
        return recipesToAward;
    }

    private static void createExperience(ServerLevel level, Vec3 position, int amount, float value) {
        int xpReward = Mth.floor((float)amount * value);
        float xpFraction = Mth.frac((float)amount * value);
        if (xpFraction != 0.0f && level.getRandom().nextFloat() < xpFraction) {
            ++xpReward;
        }
        ExperienceOrb.award(level, position, xpReward);
    }

    @Override
    public void fillStackedContents(StackedItemContents contents) {
        for (ItemStack itemStack : this.items) {
            contents.accountStack(itemStack);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.getRecipesToAwardAndPopExperience(serverLevel, Vec3.atCenterOf(pos));
        }
    }
}

