package com.zurrtum.create.infrastructure.neoforge;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Native NeoForge view of Create Fly's existing {@link Container} inventories.
 *
 * <p>The Fabric port published these inventories through Transfer API wrappers.
 * Keeping the Container as the source of truth is important: several Create Fly
 * inventories mutate their contents directly while ticking.</p>
 */
public final class ContainerItemHandler implements IItemHandler {
    private final Container inventory;
    private final @Nullable Direction side;
    private final int[] slots;

    public ContainerItemHandler(Container inventory, @Nullable Direction side) {
        this.inventory = inventory;
        this.side = side;
        this.slots = inventory instanceof WorldlyContainer sided && side != null
            ? sided.getSlotsForFace(side)
            : allSlots(inventory.getContainerSize());
    }

    private static int[] allSlots(int size) {
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = i;
        }
        return slots;
    }

    private int containerSlot(int slot) {
        return slot >= 0 && slot < slots.length ? slots[slot] : -1;
    }

    private boolean canInsert(int slot, ItemStack stack) {
        return inventory.canPlaceItem(slot, stack)
            && (!(inventory instanceof WorldlyContainer sided) || side == null || sided.canPlaceItemThroughFace(slot, stack, side));
    }

    private boolean canExtract(int slot, ItemStack stack) {
        return !(inventory instanceof WorldlyContainer sided) || side == null || sided.canTakeItemThroughFace(slot, stack, side);
    }

    @Override
    public int getSlots() {
        return slots.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        int containerSlot = containerSlot(slot);
        return containerSlot < 0 ? ItemStack.EMPTY : inventory.getItem(containerSlot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int containerSlot = containerSlot(slot);
        if (containerSlot < 0 || !canInsert(containerSlot, stack)) {
            return stack;
        }

        ItemStack existing = inventory.getItem(containerSlot);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
            return stack;
        }
        int limit = Math.min(inventory.getMaxStackSize(stack), stack.getMaxStackSize());
        int accepted = Math.min(stack.getCount(), limit - existing.getCount());
        if (accepted <= 0) {
            return stack;
        }
        if (!simulate) {
            if (existing.isEmpty()) {
                inventory.setItem(containerSlot, stack.copyWithCount(accepted));
            } else {
                existing.grow(accepted);
                inventory.setItem(containerSlot, existing);
            }
            inventory.setChanged();
        }
        return stack.copyWithCount(stack.getCount() - accepted);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        int containerSlot = containerSlot(slot);
        if (containerSlot < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = inventory.getItem(containerSlot);
        if (stack.isEmpty() || !canExtract(containerSlot, stack)) {
            return ItemStack.EMPTY;
        }
        int extracted = Math.min(amount, stack.getCount());
        if (simulate) {
            return stack.copyWithCount(extracted);
        }
        return inventory.removeItem(containerSlot, extracted);
    }

    @Override
    public int getSlotLimit(int slot) {
        int containerSlot = containerSlot(slot);
        if (containerSlot < 0) {
            return 0;
        }
        ItemStack stack = inventory.getItem(containerSlot);
        return inventory.getMaxStackSize(stack.isEmpty() ? ItemStack.EMPTY : stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        int containerSlot = containerSlot(slot);
        return containerSlot >= 0 && canInsert(containerSlot, stack);
    }
}
