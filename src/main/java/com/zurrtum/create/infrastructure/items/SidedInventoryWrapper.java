package com.zurrtum.create.infrastructure.items;

import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

public record SidedInventoryWrapper(Container inventory, int[] slots) implements SidedItemInventory {
    public SidedInventoryWrapper(Container inventory) {
        this(inventory, SlotRangeCache.get(inventory.getContainerSize()));
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return inventory.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return inventory.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return inventory.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.setItem(slot, stack);
    }

    @Override
    public int getMaxStackSize() {
        return inventory.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return inventory.getMaxStackSize(stack);
    }

    @Override
    public void setChanged() {
        inventory.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public void startOpen(ContainerUser player) {
        inventory.startOpen(player);
    }

    @Override
    public void stopOpen(ContainerUser player) {
        inventory.stopOpen(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return inventory.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItem(Container hopperInventory, int slot, ItemStack stack) {
        return inventory.canTakeItem(hopperInventory, slot, stack);
    }

    @Override
    public int countItem(Item item) {
        return inventory.countItem(item);
    }

    @Override
    public boolean hasAnyOf(Set<Item> items) {
        return inventory.hasAnyOf(items);
    }

    @Override
    public boolean hasAnyMatching(Predicate<ItemStack> predicate) {
        return inventory.hasAnyMatching(predicate);
    }

}
