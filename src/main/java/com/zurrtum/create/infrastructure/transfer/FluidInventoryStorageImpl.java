package com.zurrtum.create.infrastructure.transfer;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Transactional adapter for Create-owned fluid inventories. A committed transaction
 * retains the original FluidStack objects, while an aborted one restores the exact
 * original references and contents.
 */
public class FluidInventoryStorageImpl extends SnapshotJournal<FluidInventoryStorageImpl.Snapshot>
    implements FluidInventoryStorage {
    private static final Map<FluidInventory, FluidInventoryStorageImpl> WRAPPERS = new MapMaker()
        .weakKeys()
        .weakValues()
        .makeMap();

    public static FluidInventoryStorage of(FluidInventory inventory) {
        return of(inventory, null);
    }

    public static FluidInventoryStorage of(FluidInventory inventory, @Nullable Direction direction) {
        FluidInventoryStorageImpl storage = WRAPPERS.computeIfAbsent(inventory, FluidInventoryStorageImpl::new);
        if (inventory instanceof com.zurrtum.create.infrastructure.fluids.SidedFluidInventory sided
            && direction != null) {
            return storage.getSidedWrapper(sided, direction);
        }
        return storage;
    }

    final FluidInventory inventory;
    private final Map<Direction, FluidInventoryStorage> sidedWrappers = new EnumMap<>(Direction.class);

    public FluidInventoryStorageImpl(FluidInventory inventory) {
        this.inventory = Objects.requireNonNull(inventory);
    }

    private FluidInventoryStorage getSidedWrapper(
        com.zurrtum.create.infrastructure.fluids.SidedFluidInventory sided,
        Direction direction
    ) {
        return sidedWrappers.computeIfAbsent(direction, key -> new SidedFluidInventoryStorageImpl(this, sided, key));
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, inventory.size());
        return FluidInventoryStorage.toResource(inventory.getStack(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, inventory.size());
        FluidStack stack = inventory.getStack(index);
        return stack.isEmpty() ? 0 : Math.max(0, stack.getAmount());
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        Objects.checkIndex(index, inventory.size());
        if (resource.isEmpty()) {
            return inventory.getMaxAmountPerStack();
        }

        FluidStack resourceStack = FluidInventoryStorage.toStack(resource, 1);
        if (!inventory.isValid(index, resourceStack)) {
            return 0;
        }
        Integer stackCapacity = resourceStack.get(AllDataComponents.FLUID_MAX_CAPACITY);
        if (stackCapacity != null) {
            return Math.min(inventory.getMaxAmountPerStack(), stackCapacity);
        }
        return inventory.getMaxAmount(resourceStack);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        if (resource.isEmpty()) {
            return false;
        }
        return inventory.isValid(index, FluidInventoryStorage.toStack(resource, 1));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !isValid(index, resource)) {
            return 0;
        }

        FluidStack current = inventory.getStack(index);
        if (!current.isEmpty() && !resource.equals(FluidInventoryStorage.toResource(current))) {
            return 0;
        }

        long space = getCapacityAsLong(index, resource) - (current.isEmpty() ? 0 : current.getAmount());
        int inserted = (int) Math.min(amount, Math.max(0, space));
        if (inserted == 0) {
            return 0;
        }

        updateSnapshots(transaction);
        current = inventory.getStack(index);
        if (current.isEmpty()) {
            inventory.setStack(index, FluidInventoryStorage.toStack(resource, inserted));
        } else {
            current.setAmount(current.getAmount() + inserted);
            inventory.setStack(index, current);
        }
        return inserted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            return 0;
        }

        FluidStack current = inventory.getStack(index);
        if (current.isEmpty() || !resource.equals(FluidInventoryStorage.toResource(current))) {
            return 0;
        }

        int extracted = Math.min(amount, current.getAmount());
        if (extracted == 0) {
            return 0;
        }

        updateSnapshots(transaction);
        current = inventory.getStack(index);
        int remaining = current.getAmount() - extracted;
        if (remaining == 0) {
            inventory.setStack(index, FluidStack.EMPTY);
        } else {
            current.setAmount(remaining);
            inventory.setStack(index, current);
        }
        return extracted;
    }

    @Override
    protected Snapshot createSnapshot() {
        int size = inventory.size();
        FluidStack[] originalStacks = new FluidStack[size];
        for (int i = 0; i < size; i++) {
            FluidStack original = inventory.getStack(i);
            originalStacks[i] = original;
            if (!original.isEmpty()) {
                inventory.setStack(i, original.copy());
            }
        }
        return new Snapshot(originalStacks);
    }

    @Override
    protected void revertToSnapshot(Snapshot snapshot) {
        int size = Math.min(inventory.size(), snapshot.originalStacks.length);
        for (int i = 0; i < size; i++) {
            inventory.setStack(i, snapshot.originalStacks[i]);
        }
    }

    @Override
    protected void onRootCommit(Snapshot snapshot) {
        int size = Math.min(inventory.size(), snapshot.originalStacks.length);
        for (int i = 0; i < size; i++) {
            FluidStack original = snapshot.originalStacks[i];
            if (original == null || original.isEmpty()) {
                continue;
            }

            FluidStack current = inventory.getStack(i);
            if (!current.isEmpty() && original.getFluid() == current.getFluid()) {
                if (!Objects.equals(original.getComponentChanges(), current.getComponentChanges())) {
                    original.copyComponentsFrom(current);
                }
                original.setAmount(current.getAmount());
                inventory.setStack(i, original);
            } else {
                // Keep any external references to the replaced stack observably empty.
                original.setAmount(0);
            }
        }
        inventory.markDirty();
    }

    @Override
    public String toString() {
        return "FluidInventoryResourceHandler[" + inventory + "]";
    }

    static final class Snapshot {
        final FluidStack[] originalStacks;

        Snapshot(FluidStack[] originalStacks) {
            this.originalStacks = originalStacks;
        }
    }
}
