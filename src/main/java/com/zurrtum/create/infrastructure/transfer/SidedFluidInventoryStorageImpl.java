package com.zurrtum.create.infrastructure.transfer;

import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Direction-filtered view over a Create fluid inventory's native resource handler. */
final class SidedFluidInventoryStorageImpl implements FluidInventoryStorage {
    private final FluidInventoryStorageImpl backingStorage;
    private final SidedFluidInventory inventory;
    private final Direction direction;

    SidedFluidInventoryStorageImpl(
        FluidInventoryStorageImpl backingStorage,
        SidedFluidInventory inventory,
        Direction direction
    ) {
        this.backingStorage = backingStorage;
        this.inventory = inventory;
        this.direction = direction;
    }

    private int toInventorySlot(int index) {
        int[] slots = inventory.getAvailableSlots(direction);
        if (index < 0 || index >= slots.length) {
            throw new IndexOutOfBoundsException("Fluid slot " + index + " out of bounds for " + slots.length);
        }
        return slots[index];
    }

    @Override
    public int size() {
        return inventory.getAvailableSlots(direction).length;
    }

    @Override
    public FluidResource getResource(int index) {
        return backingStorage.getResource(toInventorySlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return backingStorage.getAmountAsLong(toInventorySlot(index));
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        int slot = toInventorySlot(index);
        if (resource.isEmpty() || !inventory.canInsert(slot, FluidInventoryStorage.toStack(resource, 1), direction)) {
            return 0;
        }
        return backingStorage.getCapacityAsLong(slot, resource);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        if (resource.isEmpty()) {
            return false;
        }
        int slot = toInventorySlot(index);
        return inventory.canInsert(slot, FluidInventoryStorage.toStack(resource, 1), direction)
            && backingStorage.isValid(slot, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        int slot = toInventorySlot(index);
        FluidStack stack = FluidInventoryStorage.toStack(resource, 1);
        if (!inventory.canInsert(slot, stack, direction)) {
            return 0;
        }
        return backingStorage.insert(slot, resource, amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        int slot = toInventorySlot(index);
        FluidStack stack = FluidInventoryStorage.toStack(resource, 1);
        if (!inventory.canExtract(slot, stack, direction)) {
            return 0;
        }
        return backingStorage.extract(slot, resource, amount, transaction);
    }

    @Override
    public String toString() {
        return "SidedFluidInventoryResourceHandler[" + inventory + ", " + direction + "]";
    }
}
