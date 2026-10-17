package com.zurrtum.create.infrastructure.items;

import net.minecraft.world.WorldlyContainer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface SidedItemInventory extends WorldlyContainer, ItemInventory, BaseSidedInventory {
    @Override
    default int[] create$getAvailableSlots(Direction side) {
        return getSlotsForFace(side);
    }

    @Override
    default boolean create$canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItemThroughFace(slot, stack, side);
    }

    @Override
    default boolean create$canExtract(int slot, ItemStack stack, Direction side) {
        return canTakeItemThroughFace(slot, stack, side);
    }
}
