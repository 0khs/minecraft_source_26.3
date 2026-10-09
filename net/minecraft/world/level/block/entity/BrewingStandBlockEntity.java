/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BrewingFuel;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BrewingStandBlockEntity
extends BaseContainerBlockEntity
implements WorldlyContainer {
    private static final int INGREDIENT_SLOT = 3;
    private static final int FUEL_SLOT = 4;
    private static final int[] SLOTS_FOR_UP = new int[]{3};
    private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
    private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};
    public static final int DATA_BREW_TIME = 0;
    public static final int DATA_FUEL_USES = 1;
    public static final int DATA_TOTAL_BREW_TIME = 2;
    public static final int DATA_TOTAL_FUEL_USES = 3;
    public static final int NUM_DATA_VALUES = 4;
    private static final int DEFAULT_BREW_TIME = 0;
    public static final int BREWING_TIME_SECONDS = 20;
    private static final int DEFAULT_FUEL = 0;
    private static final float DEFAULT_SPEED_MULTIPLIER = 1.0f;
    private static final int DEFAULT_FUEL_USES = 20;
    private static final Component DEFAULT_NAME = Component.translatable("container.brewing");
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int brewTime;
    private int totalBrewTime;
    private boolean[] lastPotionCount;
    private Item ingredient;
    private int fuel;
    private int totalFuel;
    private float speedMultiplier = 1.0f;
    protected final ContainerData dataAccess = new ContainerData(this){
        final /* synthetic */ BrewingStandBlockEntity this$0;
        {
            BrewingStandBlockEntity brewingStandBlockEntity = this$0;
            Objects.requireNonNull(brewingStandBlockEntity);
            this.this$0 = brewingStandBlockEntity;
        }

        @Override
        public int get(int dataId) {
            return switch (dataId) {
                case 0 -> this.this$0.brewTime;
                case 2 -> this.this$0.totalBrewTime;
                case 3 -> this.this$0.totalFuel;
                case 1 -> this.this$0.fuel;
                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {
            switch (dataId) {
                case 0: {
                    this.this$0.brewTime = value;
                    break;
                }
                case 2: {
                    this.this$0.totalBrewTime = value;
                    break;
                }
                case 1: {
                    this.this$0.fuel = value;
                    break;
                }
                case 3: {
                    this.this$0.totalFuel = value;
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private final RecipeManager.CachedCheck<BrewingInput, BrewingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.BREWING);

    public BrewingStandBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityTypes.BREWING_STAND, worldPosition, blockState);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
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

    protected int getUses(ServerLevel level, BrewingFuel brewingFuel) {
        return brewingFuel.uses().get(this.getLootContext(level), 0);
    }

    protected float getSpeedMultiplier(ServerLevel level, BrewingFuel brewingFuel) {
        return brewingFuel.speedMultiplier().get(this.getLootContext(level), 1.0f);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState selfState, BrewingStandBlockEntity entity) {
        NonNullList<ItemStack> items = entity.getItems();
        ItemStack fuel = items.get(4);
        BrewingFuel brewingFuel = fuel.get(DataComponents.BREWING_FUEL);
        if (entity.fuel <= 0 && brewingFuel != null) {
            entity.totalFuel = entity.fuel = entity.getUses(level, brewingFuel);
            entity.speedMultiplier = entity.getSpeedMultiplier(level, brewingFuel);
            ItemStackTemplate fuelRemainder = fuel.getItem().getCraftingRemainder();
            ItemStack newFuel = fuel;
            fuel.shrink(1);
            if (fuelRemainder != null) {
                if (fuel.isEmpty()) {
                    newFuel = fuelRemainder.create();
                } else {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), fuelRemainder.create());
                }
            }
            items.set(4, newFuel);
            BrewingStandBlockEntity.setChanged(level, pos, selfState);
        }
        boolean brewable = BrewingStandBlockEntity.isBrewable(level, entity);
        boolean isBrewing = entity.brewTime > 0;
        ItemStack ingredient = entity.items.get(3);
        if (isBrewing) {
            boolean isDoneBrewing;
            --entity.brewTime;
            boolean bl = isDoneBrewing = entity.brewTime == 0;
            if (isDoneBrewing && brewable) {
                BrewingStandBlockEntity.doBrew(level, pos, entity);
            } else if (!brewable || !ingredient.is(entity.ingredient)) {
                entity.brewTime = 0;
            }
            BrewingStandBlockEntity.setChanged(level, pos, selfState);
        } else if (brewable && entity.fuel > 0) {
            float speedMutliplier = entity.speedMultiplier > 0.0f ? entity.speedMultiplier : 1.0f;
            --entity.fuel;
            entity.totalBrewTime = entity.brewTime = (int)Math.ceil(400.0f / speedMutliplier);
            entity.ingredient = ingredient.getItem();
            BrewingStandBlockEntity.setChanged(level, pos, selfState);
        }
        boolean[] newCount = entity.getPotionBits();
        if (!Arrays.equals(newCount, entity.lastPotionCount)) {
            entity.lastPotionCount = newCount;
            BlockState state = selfState;
            if (!(state.getBlock() instanceof BrewingStandBlock)) {
                return;
            }
            for (int i = 0; i < BrewingStandBlock.HAS_BOTTLE.length; ++i) {
                state = (BlockState)state.setValue(BrewingStandBlock.HAS_BOTTLE[i], newCount[i]);
            }
            level.setBlock(pos, state, 2);
        }
    }

    private boolean[] getPotionBits() {
        boolean[] result = new boolean[3];
        for (int potion = 0; potion < 3; ++potion) {
            if (this.items.get(potion).isEmpty()) continue;
            result[potion] = true;
        }
        return result;
    }

    private static boolean isBrewable(ServerLevel serverLevel, BrewingStandBlockEntity entity) {
        NonNullList<ItemStack> items = entity.getItems();
        ItemStack ingredient = items.get(3);
        if (ingredient.isEmpty()) {
            return false;
        }
        RecipeManager recipeManager = serverLevel.recipeAccess();
        if (!recipeManager.propertySet(RecipePropertySet.BREWING_REAGENTS).test(ingredient)) {
            return false;
        }
        for (int dest = 0; dest < 3; ++dest) {
            Optional<RecipeHolder<BrewingRecipe>> recipe;
            ItemStack itemStack = items.get(dest);
            if (itemStack.isEmpty() || !(recipe = entity.quickCheck.getRecipeFor(new BrewingInput(itemStack, ingredient), serverLevel)).isPresent()) continue;
            return true;
        }
        return false;
    }

    private static void doBrew(ServerLevel level, BlockPos pos, BrewingStandBlockEntity entity) {
        NonNullList<ItemStack> items = entity.getItems();
        ItemStack ingredient = items.get(3);
        for (int dest = 0; dest < 3; ++dest) {
            ItemStack container = items.get(dest);
            BrewingInput input = new BrewingInput(container, ingredient);
            Optional<RecipeHolder<BrewingRecipe>> recipe = entity.quickCheck.getRecipeFor(input, level);
            items.set(dest, recipe.isPresent() ? recipe.get().value().assemble(input) : container);
        }
        ItemStackTemplate remainder = ingredient.getItem().getCraftingRemainder();
        ingredient.shrink(1);
        if (remainder != null) {
            if (ingredient.isEmpty()) {
                ingredient = remainder.create();
            } else {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder.create());
            }
        }
        items.set(3, ingredient);
        level.levelEvent(1035, pos, 0);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.brewTime = input.getIntOr("BrewTime", 0);
        this.totalBrewTime = input.getIntOr("total_brew_time", 400);
        if (this.brewTime > 0) {
            this.ingredient = this.items.get(3).getItem();
        }
        this.fuel = input.getIntOr("Fuel", 0);
        this.totalFuel = input.getIntOr("total_fuel", 20);
        this.speedMultiplier = input.getFloatOr("speed_multiplier", 1.0f);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("BrewTime", this.brewTime);
        output.putInt("total_brew_time", this.totalBrewTime);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Fuel", this.fuel);
        output.putInt("total_fuel", this.totalFuel);
        output.putFloat("speed_multiplier", this.speedMultiplier);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        if (slot == 4) {
            return itemStack.has(DataComponents.BREWING_FUEL);
        }
        if (this.level == null) {
            return false;
        }
        RecipeAccess recipeAccess = this.level.recipeAccess();
        if (slot == 3) {
            return recipeAccess.propertySet(RecipePropertySet.BREWING_REAGENTS).test(itemStack);
        }
        return PotionIngredient.isPotionInput(itemStack, recipeAccess) && this.getItem(slot).isEmpty();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.UP) {
            return SLOTS_FOR_UP;
        }
        if (direction == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        }
        return SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        if (slot == 3) {
            return itemStack.is(Items.GLASS_BOTTLE);
        }
        return true;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new BrewingStandMenu(containerId, inventory, this, this.dataAccess);
    }
}

