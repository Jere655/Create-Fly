package com.zurrtum.create.infrastructure.items;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ItemInventory extends Container, BaseInventory {
    @Override
    @SuppressWarnings("deprecation")
    default ItemStack removeItem(int slot, int amount) {
        if (slot >= getContainerSize() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = getItem(slot);
        int count = stack.getCount();
        if (count == 0) {
            return stack;
        }
        if (amount >= count) {
            setItem(slot, ItemStack.EMPTY);
            return onExtract(stack);
        }
        ItemStack extract = stack.copyWithCount(amount);
        stack.setCount(count - amount);
        return onExtract(extract);
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        if (slot >= getContainerSize()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = getItem(slot);
        if (stack.isEmpty()) {
            return stack;
        }
        setItem(slot, ItemStack.EMPTY);
        return onExtract(stack);
    }

    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            if (!getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default void clearContent() {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            setItem(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Override
    default void setChanged() {
    }

    @Override
    default void create$setStack(int slot, ItemStack stack) {
        setItem(slot, stack);
    }

    @Override
    default int create$size() {
        return getContainerSize();
    }

    @Override
    default int create$getMaxCount(ItemStack stack) {
        return getMaxStackSize(stack);
    }

    @Override
    default ItemStack create$getStack(int slot) {
        return getItem(slot);
    }

    @Override
    default boolean create$isValid(int slot, ItemStack stack) {
        return canPlaceItem(slot, stack);
    }

    @Override
    default void create$markDirty() {
        setChanged();
    }
}
